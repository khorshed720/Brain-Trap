package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Playful Comic/Notebook Style Colors
val ComicPaperBg = Color(0xFFFAF9F4)       // Soft sketch book cream paper
val ComicBorder = Color(0xFF000000)        // Strong 2.dp cartoonish black borders
val ComicWhiteCard = Color(0xFFFFFFFF)     // Clean white panels/cards
val ComicYellow = Color(0xFFFFD600)        // Bright bulb hint yellow (accent)
val ComicGreen = Color(0xFF4CAF50)         // Action CTA green (unlocked/ready)
val ComicBlue = Color(0xFF3B82F6)          // Soft comic-book blue
val ComicPurple = Color(0xFF9C27B0)        // Share/Social purple
val ComicOrange = Color(0xFFE65100)        // Playful dark orange
val ComicSoftPink = Color(0xFFFFE0E5)      // Pastel soft pink
val ComicSoftGreen = Color(0xFFC8E6C9)     // Pastel soft green for Level card backgrounds
val ComicSoftOrange = Color(0xFFFFE0B2)    // Pastel soft peach-orange custom cards

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

