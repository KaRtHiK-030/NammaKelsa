package com.karthik.nammakelsa

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Safely launches an external Intent. Shows a Toast if no app can handle it. */
fun Context.safeStartActivity(intent: Intent, fallbackMessage: String = "No app found to handle this action") {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, fallbackMessage, Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(this, fallbackMessage, Toast.LENGTH_SHORT).show()
    }
}

fun Context.dialPhone(phone: String) {
    if (phone.isBlank()) {
        Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show()
        return
    }
    safeStartActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")), "No dialer app found")
}

fun Context.openWhatsApp(number: String, message: String? = null) {
    if (number.isBlank()) {
        Toast.makeText(this, "No WhatsApp number available", Toast.LENGTH_SHORT).show()
        return
    }
    val cleaned = number.filter { it.isDigit() }
    val url = if (message.isNullOrBlank()) "https://wa.me/$cleaned"
    else "https://wa.me/$cleaned?text=${Uri.encode(message)}"
    safeStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)), "WhatsApp not installed")
}

private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
private val dateFormatter = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())

fun formatChatTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val now = System.currentTimeMillis()
    val sameDay = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).let {
        it.format(Date(now)) == it.format(Date(timestamp))
    }
    return if (sameDay) timeFormatter.format(Date(timestamp))
    else dateFormatter.format(Date(timestamp))
}

fun formatRelative(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000L} min ago"
        diff < 86_400_000L -> "${diff / 3_600_000L} hr ago"
        diff < 7L * 86_400_000L -> "${diff / 86_400_000L} d ago"
        else -> dateFormatter.format(Date(timestamp))
    }
}

/** Deterministic chat id so the same pair always reads/writes the same doc. */
fun chatIdFor(uidA: String, uidB: String): String =
    if (uidA < uidB) "${uidA}_$uidB" else "${uidB}_$uidA"
