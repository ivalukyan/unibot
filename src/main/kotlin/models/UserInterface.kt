package org.example.models

import io.lettuce.core.api.sync.RedisCommands
import org.example.service.redisService


interface UserInterface {

    val commands: RedisCommands<String, String>
        get() = redisService()

    fun addUser(user: User){
        commands.hset("user:${user.id}", "name", user.firstName)
        commands.hset("user:${user.id}", "username", user.username)
        commands.hset("user:${user.id}", "username", user.dateJoined.toString())
    }

    fun getUser(userId: Long): MutableMap<String, String>? {
        val user = commands.hgetall("user:$userId")
        return user
    }

    fun getAllUsers(): MutableList<MutableMap<String, String>> {
        val users = commands.keys("users:*")
        val listUsers: MutableList<MutableMap<String, String>> = mutableListOf()
        for (u in users) {
            val user = commands.hgetall(u)
            listUsers.add(user)
        }
        return listUsers
    }

    fun getAllKeys(): MutableList<String>? {
        val keys = commands.keys("users:*")
        return keys
    }
}
