package com.karthik.nammakelsa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(

    primary = androidx.compose.ui.graphics.Color(0xFF4CAF50),

    secondary = androidx.compose.ui.graphics.Color(0xFF81C784),

    background = androidx.compose.ui.graphics.Color(0xFF121212),

    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
)

private val LightColors = lightColorScheme(

    primary = androidx.compose.ui.graphics.Color(0xFF4CAF50),

    secondary = androidx.compose.ui.graphics.Color(0xFF81C784),

    background = androidx.compose.ui.graphics.Color(0xFFF5F5F5),

    surface = androidx.compose.ui.graphics.Color.White
)

@Composable
fun NammaKelsaTheme(

    darkTheme: Boolean =
        isSystemInDarkTheme(),

    content: @Composable () -> Unit
) {

    val colors = if (darkTheme)
        DarkColors
    else
        LightColors

    MaterialTheme(

        colorScheme = colors,

        typography = Typography(),

        content = content
    )
}