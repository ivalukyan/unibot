package org.example.keyboards

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

fun accessKeyboard(userId: Long): InlineKeyboardMarkup {
    return InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(text = "✅", callbackData = "approved_yes_${userId}"),
            InlineKeyboardButton.CallbackData(text = "❌", callbackData = "approved_no_${userId}")
            )
    )
}