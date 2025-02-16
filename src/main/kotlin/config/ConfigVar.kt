package org.example.config

import io.github.cdimascio.dotenv.Dotenv


class ConfigVar {
    private val dotenv: Dotenv = Dotenv.load()

    val botToken: String = dotenv["BOT_TOKEN"]
        ?: throw IllegalStateException("BOT_TOKEN not found in .env file")
    val rootsIds: List<Long> = dotenv["ROOTS"]
        ?.split(",")  // Разделяем строку по запятой
        ?.map { it.trim().toLong() }  // Преобразуем каждую часть в Long
        ?: throw IllegalStateException("ROOTS not found in .env file")


    val redisUrl = dotenv["REDIS_URL"] ?: throw IllegalStateException("REDIS_URL not found in .env file")

    val admin = dotenv["ADMIN"] ?: throw IllegalStateException("ADMIN not found in .env file")
}