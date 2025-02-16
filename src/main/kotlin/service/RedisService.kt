package org.example.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import org.example.config.ConfigVar

fun redisService(): RedisCommands<String, String>{
    val redisClient = RedisClient.create(ConfigVar().redisUrl)
    val connection = redisClient.connect()
    val commands: RedisCommands<String, String> = connection.sync()
    return commands
}