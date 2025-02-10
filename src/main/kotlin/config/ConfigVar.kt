package org.example.config

import io.github.cdimascio.dotenv.Dotenv


class ConfigVar {
    val dotenv = Dotenv.load()

    val BOT_TOKEN: String = dotenv["BOT_TOKEN"]
        ?: throw IllegalStateException("BOT_TOKEN not found in .env file")
}