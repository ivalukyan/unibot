package org.example.config

import io.github.cdimascio.dotenv.Dotenv

class ConfigVar {
    val dotenv = Dotenv.load()
    val BOT_TOKEN = dotenv["BOT_TOKEN"]

    companion object {
        val BOT_TOKEN: String
            get() {
                TODO()
            }
    }
}