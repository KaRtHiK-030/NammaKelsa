package com.karthik.nammakelsa

data class Review(
    val reviewId: String = "",
    val workerId: String = "",
    val userId: String = "",
    val reviewerName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = 0L,
    val reply: String = "",
    val reactionCount: Int = 0,
    val reactedUsers: List<String> = emptyList()
)