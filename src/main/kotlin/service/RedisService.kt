package org.example.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import org.example.config.ConfigVar
import org.example.models.User

class RedisService {
    private val redisClient: RedisClient = RedisClient.create(ConfigVar().redisUrl)
    private val connection = redisClient.connect()
    private val commands: RedisCommands<String, String> = connection.sync()

    fun addUser(user: User){
        commands.hset("user:${user.id}", "name", user.firstName)
        commands.hset("user:${user.id}", "username", user.username)
        commands.hset("user:${user.id}", "approved", false.toString())
        commands.hset("user:${user.id}", "dateJoined", user.dateJoined.toString())
    }

    fun getUser(userId: Long): MutableMap<String, String>? {
        val user = commands.hgetall("user:$userId")
        return user
    }

    fun getAllUsers(): MutableList<MutableMap<String, String>> {
        val users = commands.keys("user:*")
        val listUsers: MutableList<MutableMap<String, String>> = mutableListOf()
        for (u in users) {
            val user = commands.hgetall(u)
            listUsers.add(user)
        }
        return listUsers
    }

    fun getAllKeys(): MutableList<String>? {
        val keys = commands.keys("user:*")
        return keys
    }

    fun updateUser(id: Long,
                   firstname: String? = null,
                   username: String? = null,
                   approved: Boolean? = null,
                   dateJoined: String? = null
    ) {
        val user = commands.hgetall("user:$id")
        val updatedUser = mapOf(
            "firstName" to (firstname ?: user["firstName"]),
            "username" to (username ?: user["username"]),
            "approved" to ((approved ?: user["approved"].toBoolean()).toString()),
            "dateJoined" to (dateJoined ?: user["dateJoined"])
        )
        commands.hmset("user:$id", updatedUser)
    }
}