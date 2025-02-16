package org.example.keyboards

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

fun accessKeyboard(userId: Long): InlineKeyboardMarkup {
    return InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(text = "✅", callbackData = "access_yes_${userId}"),
            InlineKeyboardButton.CallbackData(text = "❌", callbackData = "access_no_${userId}")
            )
    )
}