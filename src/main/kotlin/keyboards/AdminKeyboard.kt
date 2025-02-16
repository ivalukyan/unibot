package org.example.keyboards

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

fun adminKeyboard(): InlineKeyboardMarkup{
    return InlineKeyboardMarkup.create(
        listOf(InlineKeyboardButton.Url(text = "Календарь событий", url = "https://clck.ru/3GRKZK")),
        listOf(InlineKeyboardButton.CallbackData(text = "Рассылка", callbackData = "mailing"))
    )
}