package com.karthik.nammakelsa

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val edited: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
