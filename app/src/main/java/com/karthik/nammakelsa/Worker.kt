package com.karthik.nammakelsa

data class Worker(
    val userId: String = "",
    val name: String = "",
    val skill: String = "",
    val location: String = "",
    val chargePerDay: String = "",
    val phoneNumber: String = "",
    val whatsappNumber: String = "",
    val imageUrl: String = "",
    val availability: String = "Available"
)