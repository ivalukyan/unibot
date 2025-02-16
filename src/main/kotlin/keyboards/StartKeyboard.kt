package org.example.keyboards

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

fun startKeyboard(userId: Long): InlineKeyboardMarkup {
    return InlineKeyboardMarkup.create(
        listOf(InlineKeyboardButton.CallbackData(text = "Запросить", callbackData = "access_${userId}"))
    )
}