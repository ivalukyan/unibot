package org.example.config

import io.github.cdimascio.dotenv.Dotenv


class ConfigVar {
    val dotenv = Dotenv.load()

    val botToken: String = dotenv["BOT_TOKEN"]
        ?: throw IllegalStateException("BOT_TOKEN not found in .env file")
    val rootsIds = dotenv["ROOTS"] ?: throw IllegalStateException("ROOTS not found in .env file")

    val redisUrl = dotenv["REDIS_URL"]

    val admin = dotenv["ADMIN"] ?: throw IllegalStateException("ADMIN not found in .env file")
}