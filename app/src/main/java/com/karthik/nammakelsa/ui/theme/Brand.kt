package com.karthik.nammakelsa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * One source of truth for the bright "luxury cream" page background.
 * No dark/dusky gradients anywhere.
 */
@Composable
@ReadOnlyComposable
fun brandBackground(): Brush {
    val cs = MaterialTheme.colorScheme
    // Soft top-left gold wash → cream, nothing greys out.
    return Brush.verticalGradient(
        0.00f to cs.secondaryContainer.copy(alpha = 0.55f),
        0.45f to cs.background,
        1.00f to cs.background
    )
}

/** Subtle hero gradient for marquee surfaces (login card, role card). */
@Composable
@ReadOnlyComposable
fun brandHero(): Brush {
    val cs = MaterialTheme.colorScheme
    return Brush.verticalGradient(
        listOf(
            cs.primaryContainer.copy(alpha = 0.45f),
            cs.secondaryContainer.copy(alpha = 0.35f),
            cs.background
        )
    )
}

/** Tint for the WhatsApp button. Single source. */
val whatsAppGreen: Color get() = WhatsAppGreen

val starYellow:    Color get() = StarYellow
val successGreen:  Color get() = SuccessGreen
val warningOrange: Color get() = WarningOrange
