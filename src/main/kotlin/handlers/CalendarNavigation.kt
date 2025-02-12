package org.example.handlers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import org.example.keyboards.generateCalendar

fun handleCalendarNavigation(params: List<String>, callbackQuery: CallbackQuery, bot: Bot) {
    if (params.size < 3) {
        println("⚠️ Ошибка: некорректные параметры callback-запроса: ${callbackQuery.data}")
        return
    }

    val year = params[1].toIntOrNull() ?: return
    val month = params[2].toIntOrNull() ?: return

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