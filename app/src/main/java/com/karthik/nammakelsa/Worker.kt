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
    /** Additional skills as `[{"skill":"Plumber","charge":"500"}, ...]` */
    val skillsList: List<Map<String, String>> = emptyList(),
    /** "Available" | "Busy" | "Offline" */
    val availability: String = "Available",
    val online: Boolean = false,
    /** Persisted average rating (recomputed server-/client-side on review CRUD). */
    val averageRating: Double = 0.0,
    val totalReviews: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
