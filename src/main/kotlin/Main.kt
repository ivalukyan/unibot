package org.example

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import org.example.config.ConfigVar
import java.time.LocalDate
import java.util.logging.Logger
import org.example.handlers.handleCalendarNavigation
import org.example.handlers.handleDateSelection
import org.example.keyboards.generateCalendar


fun main() {
    val service = GoogleCalendarService.getCalendarService()
    val bot = bot {
        token = ConfigVar().BOT_TOKEN
        timeout = 30
        logLevel = LogLevel.Network.Basic
        val logger = Logger.getLogger("MainKt")

        dispatch {
            command("start") {
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
            }

            callbackQuery {
                val data = callbackQuery.data
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
    bot.startPolling()
}