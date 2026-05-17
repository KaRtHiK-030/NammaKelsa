package com.karthik.nammakelsa.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary               = LightPrimary,
    onPrimary             = LightOnPrimary,
    primaryContainer      = LightPrimaryContainer,
    onPrimaryContainer    = LightOnPrimaryContainer,

    secondary             = LightSecondary,
    onSecondary           = LightOnSecondary,
    secondaryContainer    = LightSecondaryContainer,
    onSecondaryContainer  = LightOnSecondaryContainer,

    tertiary              = LightTertiary,
    onTertiary            = LightOnTertiary,
    tertiaryContainer     = LightTertiaryContainer,
    onTertiaryContainer   = LightOnTertiaryContainer,

    background            = LightBackground,
    onBackground          = LightOnBackground,
    surface               = LightSurface,
    onSurface             = LightOnSurface,
    surfaceVariant        = LightSurfaceVariant,
    onSurfaceVariant      = LightOnSurfaceVariant,

    outline               = LightOutline,
    outlineVariant        = LightOutlineVariant,

    error                 = ErrorRed,
    onError               = Color.White,
    errorContainer        = LightErrorContainer,
    onErrorContainer      = LightOnErrorContainer
)

private val DarkColors = darkColorScheme(
    primary               = DarkPrimary,
    onPrimary             = DarkOnPrimary,
    primaryContainer      = DarkPrimaryContainer,
    onPrimaryContainer    = DarkOnPrimaryContainer,

    secondary             = DarkSecondary,
    onSecondary           = DarkOnSecondary,
    secondaryContainer    = DarkSecondaryContainer,
    onSecondaryContainer  = DarkOnSecondaryContainer,

    tertiary              = DarkTertiary,
    onTertiary            = DarkOnTertiary,
    tertiaryContainer     = DarkTertiaryContainer,
    onTertiaryContainer   = DarkOnTertiaryContainer,

    background            = DarkBackground,
    onBackground          = DarkOnBackground,
    surface               = DarkSurface,
    onSurface             = DarkOnSurface,
    surfaceVariant        = DarkSurfaceVariant,
    onSurfaceVariant      = DarkOnSurfaceVariant,

    outline               = DarkOutline,
    outlineVariant        = DarkOutlineVariant,

    error                 = Color(0xFFFFB4AB),
    onError               = Color(0xFF690005),
    errorContainer        = DarkErrorContainer,
    onErrorContainer      = DarkOnErrorContainer
)

/**
 * App theme. Dynamic-color is intentionally OFF by default so the brand
 * (cream + emerald + champagne gold) is preserved across devices. Pass
 * `dynamicColor = true` if you ever want Material You.
 */
@Composable
fun NammaKelsaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colors.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
                window.navigationBarColor = colors.background.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography  = Typography,
        content     = content
    )
}
