package org.example.handlers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.google.api.services.calendar.Calendar
import getEventsForDate

fun handleDateSelection(params: List<String>, service: Calendar, callbackQuery: CallbackQuery, bot: Bot) {
    if (params.size < 4) {
        println("⚠️ Ошибка: некорректные параметры callback-запроса: ${callbackQuery.data}")
        return
    }

    val year = params[1]
    val month = params[2].padStart(2, '0')
    val day = params[3].padStart(2, '0')

    val res = getEventsForDate(service, "$year-$month-$day")
    val chatId = callbackQuery.message?.chat?.id
    val messageId = callbackQuery.message?.messageId

    println(res)

    if (chatId != null && messageId != null) {
        bot.editMessageText(
            chatId = ChatId.fromId(chatId),
            messageId = messageId,
            text = res,
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(InlineKeyboardButton.CallbackData("Назад", "calendar_${year}_$month"))
            )
        )
    }
}