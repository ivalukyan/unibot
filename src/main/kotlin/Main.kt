package org.example

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import org.example.config.ConfigVar
import java.time.LocalDate
import java.util.logging.Logger
import org.example.handlers.handleCalendarNavigation
import org.example.handlers.handleDateSelection
import org.example.keyboards.accessKeyboard
import org.example.keyboards.adminKeyboard
import org.example.keyboards.generateCalendar
import org.example.keyboards.startKeyboard
import org.example.models.BotState
import java.time.LocalDateTime
import org.example.models.User
import org.example.service.RedisService


fun main() {
    val service = GoogleCalendarService.getCalendarService()
    val userStates = mutableMapOf<Long, BotState>()

    val bot = bot {
        token = ConfigVar().botToken
        timeout = 30
        logLevel = LogLevel.Network.Basic
        val logger = Logger.getLogger("MainKt")
        val userService = RedisService()

        dispatch {
            command("start") {
                val chatId = message.from?.id
                if (chatId != null) {
                    val user = userService.getUser(chatId)
                    if (!user.isNullOrEmpty()) {
                        if (user["approved"].toBoolean()){
                            val today = LocalDate.now()
                            val year = today.year
                            val month = today.monthValue

                            logger.info("Генерация календаря...")
                            val calendarInlineKeyboard = generateCalendar(year, month)
                            logger.info("Календарь получен - $calendarInlineKeyboard")

                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Здравствуйте, ${message.from?.firstName}!\nВыберите дату на календаре:",
                                replyMarkup = calendarInlineKeyboard,
                                parseMode = ParseMode.MARKDOWN
                            )
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "У вас нет доступа к боту, запросите доступ.",
                                replyMarkup = startKeyboard(chatId)
                            )
                        }
                    } else {
                        val firstName = message.from?.firstName
                        val username = message.from?.username

                        if (firstName != null && username != null) {
                            val createUser = User(
                                id = chatId,
                                firstName = firstName,
                                username = username,
                                dateJoined = LocalDateTime.now()
                            )
                            userService.addUser(createUser)
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "У вас нет доступа к боту, запросите доступ.",
                                replyMarkup = startKeyboard(chatId)
                            )
                        }
                    }
                }
            }

            command("admin"){
                val roots = ConfigVar().rootsIds
                if (message.from?.id in roots){
                    logger.info("Админ ${message.from?.id} в сети")
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = "Здравствуйте, ${message.from?.firstName}",
                        replyMarkup = adminKeyboard()
                    )
                }
            }

            callbackQuery {
                when (val data = callbackQuery.data) {
                    "mailing" -> {
                        val chatId = callbackQuery.message?.chat?.id
                        if (chatId != null) {
                            userStates[chatId] = BotState.WAITING_FOR_MAILING
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Напишите сообщение для рассылки:",
                            )
                        }
                    }
                    else -> {
                        val params = data.split("_")
                        when (params[0]) {
                            "access" -> {
                                val adminId = ConfigVar().admin.toLong()
                                val user = userService.getUser(params[1].toLong())
                                logger.info("Пользователь с ID ${params[1]} запросил доступ")
                                bot.sendMessage(
                                    chatId = ChatId.fromId(adminId),
                                    text = "Запрошен доступ\n" +
                                            "${user?.get("name")}\n" +
                                            "${user?.get("username")}\n" +
                                            "${user?.get("dateJoined")}"
                                    ,
                                    replyMarkup = accessKeyboard(params[1].toLong())
                                )
                            }
                            "approved" -> {
                                if (params[1] == "yes") {

                                    logger.info("Пользователь с ID ${params[2]} получил доступ")
                                    userService.updateUser(
                                        id = params[2].toLong(), approved = true
                                    )
                                    bot.deleteMessage(
                                        chatId = ChatId.fromId(callbackQuery.message!!.chat.id),
                                        messageId = callbackQuery.message!!.messageId
                                    )
                                } else {
                                    logger.info("Пользователь с ID ${params[2]} не получил доступ")
                                    userService.updateUser(
                                        id = params[2].toLong(), approved = false
                                    )
                                    bot.deleteMessage(
                                        chatId = ChatId.fromId(callbackQuery.message!!.chat.id),
                                        messageId = callbackQuery.message!!.messageId
                                    )
                                }
                            }
                            "date" -> handleDateSelection(params, service, callbackQuery, bot)
                            "calendar", "next", "back" -> handleCalendarNavigation(params, callbackQuery, bot)
                            else -> println("⚠️ Ошибка: неизвестный callback-запрос: $data")
                        }
                    }
                }
            }

            message {
                val chatId = message.from?.id
                when (userStates[chatId]) {
                    BotState.WAITING_FOR_MAILING -> {
                        val mailingText = message.text
                        userStates.remove(chatId)
                        logger.info("✅Сообщение для рассылки принято $mailingText")
                        val users = userService.getAllKeys()
                        if (users != null && mailingText != null) {
                            for (user in users) {
                                val userid = user.split(":")[1].toLong()
                                bot.sendMessage(
                                    chatId = ChatId.fromId(userid),
                                    text = mailingText
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
    bot.startPolling()
}