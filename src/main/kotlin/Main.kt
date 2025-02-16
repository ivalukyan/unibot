package org.example

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.text
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
import org.example.models.UserInterface
import java.time.LocalDateTime
import org.example.models.User


fun main() {
    val service = GoogleCalendarService.getCalendarService()

    class UserService : UserInterface

    val bot = bot {
        token = ConfigVar().botToken
        timeout = 30
        logLevel = LogLevel.Network.Basic
        val logger = Logger.getLogger("MainKt")
        val userStates = mutableMapOf<Long, String>()
        val userService = UserService()

        dispatch {
            command("start") {
                val user = userService.getUser(message.from!!.id)

                if (user != null) {
                    if (user["approved"].toBoolean()){
                        val today = LocalDate.now()
                        val year = today.year
                        val month = today.monthValue

                        logger.info("Генерация календаря...")
                        val calendarInlineKeyboard = generateCalendar(year, month)
                        logger.info("Календарь получен - $calendarInlineKeyboard")

                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = "Здравствуйте, ${message.from?.firstName}!\nВыберите дату на календаре:",
                            replyMarkup = calendarInlineKeyboard,
                            parseMode = ParseMode.MARKDOWN
                        )
                    } else {
                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = "У вас нет доступа к боту, запросите доступ.",
                            replyMarkup = startKeyboard()
                        )
                    }
                } else {
                    val chatId = message.from?.id
                    val firstName = message.from?.firstName
                    val username = message.from?.username

                    if (chatId != null && firstName != null && username != null) {
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
                            replyMarkup = startKeyboard()
                        )
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
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Напишите сообщение для рассылки:"
                            )
                        }
                    }
                    "access" -> {
                        val chatId = ConfigVar().admin.toLong()
                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = "Запрошен доступ",
                            replyMarkup = accessKeyboard()
                        )
                    }
                    "access_yes" -> {

                    }
                    "access_no" -> {

                    }
                    else -> {
                        val params = data.split("_")
                        println(params)

                        when (params[0]) {
                            "date" -> handleDateSelection(params, service, callbackQuery, bot)
                            "calendar", "next", "back" -> handleCalendarNavigation(params, callbackQuery, bot)
                            else -> println("⚠️ Ошибка: неизвестный callback-запрос: $data")
                        }
                    }
                }
            }

            message {
                val chatId = message.chat.id
                when (userStates[chatId]) {
                    "waiting_for_mailing_message" -> {
                        val mailingText = message.text
                        userStates.remove(chatId)
                        logger.info("Сообщение для рассылки принято $mailingText")
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
                    else -> {
                        bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Неизвестная команда.")
                    }
                }
            }
        }
    }
    bot.startPolling()
}