package com.karthik.nammakelsa

data class ChatUser(
    val userId: String = "",

    val name: String = "",

    val imageUrl: String = "",

    val lastMessage: String = "",

    val lastMessageTime: Long = 0L,

    val online: Boolean = false,

    val lastSeen: Long = 0L
)