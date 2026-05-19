package com.karthik.nammakelsa

data class Request(
    val requestId: String = "",
    val workerId: String = "",

    val workerName: String = "",
    val workerImage: String = "",
    val workerSkill: String = "",
    val workerLocation: String = "",
    val workerAvailability: String = "Available",

    val hirerId: String = "",
    val hirerName: String = "",
    val hirerImage: String = "",
    val hirerLocation: String = "",
    val hirerPhone: String = "",
    val hirerWhatsapp: String = "",

    val workDetails: String = "",
    val status: String = "Pending",
    val timestamp: Long = 0L
)