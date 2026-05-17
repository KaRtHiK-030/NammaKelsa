package com.karthik.nammakelsa

data class Request(
    val requestId: String = "",
    val workerId: String = "",
    val hirerId: String = "",
    val hirerName: String = "",
    val hirerImage: String = "",
    val hirerLocation: String = "",
    val hirerPhone: String = "",
    val hirerWhatsapp: String = "",
    val workDetails: String = "",
    /** Always store `pending` / `accepted` / `declined` / `completed` in lowercase. */
    val status: String = RequestStatus.PENDING.value,
    val createdAt: Long = System.currentTimeMillis()
)
