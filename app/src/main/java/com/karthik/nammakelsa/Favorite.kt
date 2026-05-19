package com.karthik.nammakelsa

data class Favorite(
    val userId: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val workerSkill: String = "",
    val workerLocation: String = "",
    val workerCharge: String = "",
    val savedAt: Long = 0L
)