package com.karthik.nammakelsa

data class WorkRequest(
    val requestId: String = "",
    val workerId: String = "",
    val hirerId: String = "",
    val hirerName: String = "",
    val status: String = "Pending"
)