package org.example.models

import java.security.Timestamp
import java.time.LocalDateTime

data class User(
    val id: Long,
    val firstName: String,
    val username: String,
    val approved: Boolean = false,
    val dateJoined: LocalDateTime
)
