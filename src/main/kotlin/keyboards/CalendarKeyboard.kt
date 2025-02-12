package org.example.keyboards

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import java.time.LocalDate

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
    repeat(firstDayOfMonth - 1) {
        week.add(InlineKeyboardButton.CallbackData(" ", " "))
    }

    for (day in 1..daysInMonth) {
        week.add(InlineKeyboardButton.CallbackData("$day", "date_${year}_${month}_$day"))
        if (week.size == 7) {
            dates.add(week)
            week = mutableListOf()
        }
    }

    // Добавляем пустые кнопки в конце месяца
    if (week.isNotEmpty()) {
        while (week.size < 7) {
            week.add(InlineKeyboardButton.CallbackData(" ", " "))
        }
        dates.add(week)
    }

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