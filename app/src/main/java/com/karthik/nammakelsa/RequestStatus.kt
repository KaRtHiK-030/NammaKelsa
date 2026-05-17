package com.karthik.nammakelsa

/** Canonical status values for hire requests. Always store the lowercase `value` in Firestore. */
enum class RequestStatus(val value: String, val displayName: String) {
    PENDING("pending", "Pending"),
    ACCEPTED("accepted", "Accepted"),
    DECLINED("declined", "Declined"),
    COMPLETED("completed", "Completed");

    companion object {
        fun from(raw: String?): RequestStatus =
            entries.firstOrNull { it.value.equals(raw, ignoreCase = true) } ?: PENDING
    }
}
