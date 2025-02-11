package org.example

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import org.example.config.ConfigVar
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.LocalDate
import java.util.*
import java.util.logging.Logger


fun main() {
    val bot = bot {

        token = ConfigVar().BOT_TOKEN
        timeout = 30
        logLevel = LogLevel.Network.Body
        val logger = Logger.getLogger("MainKt")

        val today = LocalDate.now()
        val year = today.year
        val mon = today.monthValue

        logger.info("Генерация календаря...")
        val calendarInlineKeyboard = generateCalendar(year, mon)
        logger.info("Календарь получен - $calendarInlineKeyboard")

        dispatch {
            command("start") {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Здравствуйте, ${message.from?.firstName}",
                    replyMarkup = calendarInlineKeyboard
                )
            }
        }
    }
    bot.startPolling()
}


fun generateCalendar(year: Int, month: Int): InlineKeyboardMarkup? {

    val logger = Logger.getLogger("genCalendar")
    val sdf = SimpleDateFormat("dd-MM-yyyy")
    val currentDate = sdf.format(Date())
    var calendar: InlineKeyboardMarkup? = null

    if (year % 100 == 0 && year % 400 == 0) {
        when (month) {
            2 -> {
                val dates: MutableList<InlineKeyboardButton> = mutableListOf()
                for (i in 0..31) {
                    dates.add(
                        i,
                        InlineKeyboardButton.CallbackData(text = "${i + 1}", callbackData = "${i + 1}-${month}")
                    )
                }

                calendar = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData("Календарь", " ")),
                    listOf(
                        InlineKeyboardButton.CallbackData("<", "back_month"),
                        InlineKeyboardButton.CallbackData(currentDate, " "),
                        InlineKeyboardButton.CallbackData(">", "next_month"),
                    ),
                    dates
                )
            }

            else -> {
                logger.info("Такого месяца не существует")
            }
        }
    } else {
        when (month) {
            2 -> {
                val dates: MutableList<InlineKeyboardButton> = mutableListOf()
                for (i in 0..31) {
                    dates.add(
                        i,
                        InlineKeyboardButton.CallbackData(text = "${i + 1}", callbackData = "${i + 1}-${month}")
                    )
                }

                calendar = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData("Календарь", " ")),
                    listOf(
                        InlineKeyboardButton.CallbackData("<", "back_month"),
                        InlineKeyboardButton.CallbackData(currentDate, " "),
                        InlineKeyboardButton.CallbackData(">", "next_month"),
                    ),
                    dates
                )
            }

            else -> {
                logger.info("Такого месяца не существует")
            }
        }
    }
    return calendar
}