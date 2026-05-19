package com.karthik.nammakelsa

data class Message(
    val messageId: String = "",
    val chatId: String = "",

    val senderId: String = "",
    val receiverId: String = "",

    val message: String = "",
    val timestamp: Long = 0L,

    val deliveredTo: List<String> = emptyList(),

    val seenBy: List<String> = emptyList(),

    val deletedFor: List<String> = emptyList()
)