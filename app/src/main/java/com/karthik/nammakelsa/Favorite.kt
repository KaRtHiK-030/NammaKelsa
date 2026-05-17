package com.karthik.nammakelsa

data class Favorite(
    val userId: String = "",
    val workerId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
