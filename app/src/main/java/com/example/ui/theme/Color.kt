package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Playful Comic/Notebook Style Colors
val ComicPaperBg = Color(0xFFFAF9F4)       // Soft sketch book cream paper
val ComicBorder = Color(0xFF232B35)        // Soft high-fidelity dark slate charcoal borders instead of pure black
val ComicWhiteCard = Color(0xFFFFFFFF)     // Clean white panels/cards
val ComicYellow = Color(0xFFFFD600)        // Bright bulb hint yellow (accent)
val ComicGreen = Color(0xFF4CAF50)         // Action CTA green (unlocked/ready)
val ComicBlue = Color(0xFF3B82F6)          // Soft comic-book blue
val ComicPurple = Color(0xFF9C27B0)        // Share/Social purple
val ComicOrange = Color(0xFFE65100)        // Playful dark orange
val ComicSoftPink = Color(0xFFFFE0E5)      // Pastel soft pink
val ComicSoftGreen = Color(0xFFC8E6C9)     // Pastel soft green for Level card backgrounds
val ComicSoftOrange = Color(0xFFFFE0B2)    // Pastel soft peach-orange custom cards
val ComicShadow = Color(0x22232B35)        // Professional soft translucent 3D shadow overlay

// Keep backward compatibility so existing code builds cleanly but automatically styles in light comic colors!
val DarkBg = ComicPaperBg                  // Remapped to cream paper!
val PrimaryRed = ComicOrange
val SecondaryBlue = ComicBlue
val SurfaceBlue = ComicWhiteCard
val SoftGrayText = Color(0xFF1C1C1E)       // Playful contrast text
val CorrectGreen = ComicGreen
val WrongRed = Color(0xFFD32F2F)
val OrangeGold = ComicYellow
val DarkGold = Color(0xFFE65100)
val LightBlueAccent = ComicBlue
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val GrayLine = Color(0xFFE0DEC9)           // Notebook light-grid line color
val XRayOverlayColor = Color(0x22000000)
val Purple80 = Color(0xFFE1BEE7)
val PurpleGrey80 = Color(0xFFD1C4E9)
val Pink80 = Color(0xFFFFCDD2)
val Purple40 = Color(0xFF6A1B9A)
val PurpleGrey40 = Color(0xFF4527A0)
val Pink40 = Color(0xFFC2185B)

// Procedures to compute 3D highlight and shading colors
fun Color.brighten(factor: Float = 0.35f): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.darken(factor: Float = 0.3f): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

