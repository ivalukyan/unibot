package org.example

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import getEventsForDate
import org.example.config.ConfigVar
import java.time.LocalDate
import java.util.logging.Logger

fun main() {

    val service = GoogleCalendarService.getCalendarService()
    val events = getEventsForDate(service, "2025-02-14")
    println(events)

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

                if (params.size < 3) {
                    println("⚠️ Ошибка: некорректные параметры callback-запроса: $data")
                    return@callbackQuery
                }

                val year = params[1].toIntOrNull() ?: return@callbackQuery
                val month = params[2].toIntOrNull() ?: return@callbackQuery


                val chatId = callbackQuery.message?.chat?.id
                val messageId = callbackQuery.message?.messageId
                val calendarInlineKeyboard = generateCalendar(year, month)

                if (chatId != null && messageId != null) {
                    try {
                        bot.editMessageText(
                            chatId = ChatId.fromId(chatId),
                            messageId = messageId,
                            text = "📅 Календарь",
                            replyMarkup = calendarInlineKeyboard,
                            parseMode = ParseMode.MARKDOWN
                        )
                        println("✅ Сообщение успешно обновлено!")
                    } catch (e: Exception) {
                        println("❌ Ошибка при обновлении сообщения: ${e.message}")
                    }
                } else {
                    println("⚠️ Ошибка: chatId или messageId = null")
                }
            }
        }
    }
    bot.startPolling()
}

fun generateCalendar(year: Int, month: Int): InlineKeyboardMarkup {
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> return InlineKeyboardMarkup.create()
    }

    val nameMonth = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )[month - 1]

    val firstDayOfMonth = LocalDate.of(year, month, 1).dayOfWeek.value % 7 // 0 = Monday, 6 = Sunday
    val dates: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    var week: MutableList<InlineKeyboardButton> = mutableListOf()

    // Добавляем пустые кнопки перед первым днем месяца
    for (i in 1 until firstDayOfMonth) {
        week.add(InlineKeyboardButton.CallbackData(" ", " "))
    }

    for (i in 1..daysInMonth) {
        week.add(InlineKeyboardButton.CallbackData("$i", "date_${year}_${month}_$i"))
        if (week.size == 7) {
            dates.add(week)
            week = mutableListOf()
        }
    }

    if (week.isNotEmpty()) {
        dates.add(week)
    }

    val lastDayOfMonth = LocalDate.of(year, month, daysInMonth).dayOfWeek.value % 7
    val lastWeekOfMonth = dates.last().toMutableList()
    val lastIndexDates = dates.lastIndex
    for (i in 0  until   7 - lastDayOfMonth){
        lastWeekOfMonth.add(InlineKeyboardButton.CallbackData(" ", " "))
    }

    dates[lastIndexDates] = lastWeekOfMonth

    val prevMonth = if (month == 1) 12 else month - 1
    val prevYear = if (month == 1) year - 1 else year
    val nextMonth = if (month == 12) 1 else month + 1
    val nextYear = if (month == 12) year + 1 else year

    return InlineKeyboardMarkup.create(
        listOf(InlineKeyboardButton.CallbackData("📅 $nameMonth $year", " ")),
        listOf(
            InlineKeyboardButton.CallbackData("⬅️", "back_${prevYear}_${prevMonth}"),
            InlineKeyboardButton.CallbackData("➡️", "next_${nextYear}_${nextMonth}")
        ),
        listOf(
            InlineKeyboardButton.CallbackData("Пн", " "),
            InlineKeyboardButton.CallbackData("Вт", " "),
            InlineKeyboardButton.CallbackData("Ср", " "),
            InlineKeyboardButton.CallbackData("Чт", " "),
            InlineKeyboardButton.CallbackData("Пт", " "),
            InlineKeyboardButton.CallbackData("Сб", " "),
            InlineKeyboardButton.CallbackData("Вс", " ")
        ),
        *dates.toTypedArray(),
        listOf(InlineKeyboardButton.Url("Файлы", "https://clck.ru/3GM3AV"))
    )
}

