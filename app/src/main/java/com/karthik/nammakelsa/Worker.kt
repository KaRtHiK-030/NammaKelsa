package com.karthik.nammakelsa

data class Worker(

    val name: String = "",

    val skill: String = "",

    val location: String = "",

    val chargePerDay: String = "",

    val phoneNumber: String = "",

    val whatsappNumber: String = "",

    val imageUrl: String = "",

    val userId: String = "",

    // MULTIPLE SKILLS
    val skillsList: List<Map<String, String>> = emptyList(),

    // AVAILABILITY STATUS
    val availability: String = "Available"
)