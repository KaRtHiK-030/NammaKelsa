package com.karthik.nammakelsa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColors = lightColorScheme(
    primary = LuxuryPrimary,
    onPrimary = TextOnPrimary,

    secondary = LuxurySecondary,
    onSecondary = TextOnPrimary,

    tertiary = LuxuryAccent,
    background = LuxuryBackground,
    onBackground = TextPrimary,

    surface = LuxurySurface,
    onSurface = TextPrimary,

    surfaceVariant = LuxurySurfaceSoft,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    outline = BorderMedium
)

@Composable
fun NammaKelsaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = Typography,
        content = content
    )
}