package com.karthik.nammakelsa

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karthik.nammakelsa.ui.theme.GlassBorderStrong
import com.karthik.nammakelsa.ui.theme.TextOnPrimary

@Composable
fun InitialAvatar(
    name: String?,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    fontSize: TextUnit = TextUnit.Unspecified
) {

    val initial =
        name?.trim()
            ?.firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            ?: "?"

    val bgColor =
        when ((initial.firstOrNull()?.code ?: 0) % 5) {
            0 -> Color(0xFF6B7280) // slate
            1 -> Color(0xFF8B7355) // luxury brown
            2 -> Color(0xFFC8A97E) // champagne
            3 -> Color(0xFF7C6F64) // taupe
            else -> Color(0xFF9CA3AF) // soft gray
        }

    val resolvedFontSize =
        if (fontSize == TextUnit.Unspecified)
            (size.value * 0.38f).sp
        else
            fontSize

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = 1.5.dp,
                color = GlassBorderStrong,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = resolvedFontSize,
            fontWeight = FontWeight.Bold,
            color = TextOnPrimary
        )
    }
}