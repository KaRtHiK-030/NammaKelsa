package com.karthik.nammakelsa.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * NammaKelsa — bright "luxury cream" palette.
 *
 * Brand:   deep emerald   #1F7A4D   (primary)
 * Accent:  champagne gold #C9A227   (secondary)
 * Surface: warm cream     #FFFBF5
 * No neon, no dusky gradients. High contrast on every screen.
 */

// ─── Brand & accents ────────────────────────────────────────────────────────
val BrandEmerald          = Color(0xFF1F7A4D)
val BrandEmeraldDark      = Color(0xFF155F3B)
val BrandEmeraldSoft      = Color(0xFFD7EFE0)   // 12% tint
val ChampagneGold         = Color(0xFFC9A227)
val ChampagneGoldSoft     = Color(0xFFFAEFC8)
val WarmCream             = Color(0xFFFFFBF5)
val Ivory                 = Color(0xFFFAF6EE)
val SoftCharcoal          = Color(0xFF1B1F1C)

// Status accents (used sparingly as 12-15% tints, never solid neon)
val SuccessGreen   = Color(0xFF2E7D32)
val WarningOrange  = Color(0xFFE07B00)
val ErrorRed       = Color(0xFFB3261E)
val InfoBlue       = Color(0xFF1565C0)
val StarYellow     = Color(0xFFE6B400)
val WhatsAppGreen  = Color(0xFF1FAE5C)   // muted vs pure 0xFF25D366

// ─── Light scheme ───────────────────────────────────────────────────────────
val LightPrimary              = BrandEmerald
val LightOnPrimary            = Color.White
val LightPrimaryContainer     = BrandEmeraldSoft
val LightOnPrimaryContainer   = Color(0xFF0B3520)

val LightSecondary            = ChampagneGold
val LightOnSecondary          = Color(0xFF2A1F00)
val LightSecondaryContainer   = ChampagneGoldSoft
val LightOnSecondaryContainer = Color(0xFF3F2E00)

val LightTertiary             = Color(0xFF5C6BC0)        // soft indigo, used very lightly
val LightOnTertiary           = Color.White
val LightTertiaryContainer    = Color(0xFFE0E4FA)
val LightOnTertiaryContainer  = Color(0xFF1A237E)

val LightBackground           = WarmCream
val LightOnBackground         = SoftCharcoal
val LightSurface              = Color.White
val LightOnSurface            = SoftCharcoal
val LightSurfaceVariant       = Ivory
val LightOnSurfaceVariant     = Color(0xFF55564F)
val LightSurfaceContainer     = Color(0xFFF5EFE3)
val LightSurfaceContainerHigh = Color(0xFFEFE7D6)

val LightOutline              = Color(0xFFB6B5AC)
val LightOutlineVariant       = Color(0xFFE2DFD4)

val LightErrorContainer       = Color(0xFFFFE3DF)
val LightOnErrorContainer     = Color(0xFF410002)

// ─── Dark scheme ────────────────────────────────────────────────────────────
val DarkPrimary               = Color(0xFF7BD3A3)
val DarkOnPrimary             = Color(0xFF003822)
val DarkPrimaryContainer      = Color(0xFF1F7A4D)
val DarkOnPrimaryContainer    = Color(0xFFD7EFE0)

val DarkSecondary             = Color(0xFFE8C766)
val DarkOnSecondary           = Color(0xFF2A1F00)
val DarkSecondaryContainer    = Color(0xFF8B6E14)
val DarkOnSecondaryContainer  = ChampagneGoldSoft

val DarkTertiary              = Color(0xFFB3BCFF)
val DarkOnTertiary            = Color(0xFF15226B)
val DarkTertiaryContainer     = Color(0xFF303F9F)
val DarkOnTertiaryContainer   = Color(0xFFE0E4FA)

val DarkBackground            = Color(0xFF111712)
val DarkOnBackground          = Color(0xFFE8EAE2)
val DarkSurface               = Color(0xFF161C18)
val DarkOnSurface             = Color(0xFFE8EAE2)
val DarkSurfaceVariant        = Color(0xFF3A3F39)
val DarkOnSurfaceVariant      = Color(0xFFC4C7BD)
val DarkSurfaceContainer      = Color(0xFF1E251F)
val DarkSurfaceContainerHigh  = Color(0xFF272E27)

val DarkOutline               = Color(0xFF8C8F87)
val DarkOutlineVariant        = Color(0xFF44473F)

val DarkErrorContainer        = Color(0xFF93000A)
val DarkOnErrorContainer      = Color(0xFFFFDAD6)
