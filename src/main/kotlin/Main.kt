package org.example

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import org.example.config.ConfigVar


fun main() {
    val bot = bot {

        token = ConfigVar().BOT_TOKEN
        timeout = 30
        logLevel = LogLevel.Network.Body

        dispatch {
            command("start") {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hello World!"
                )
            }
        }
    }
    bot.startPolling()
}