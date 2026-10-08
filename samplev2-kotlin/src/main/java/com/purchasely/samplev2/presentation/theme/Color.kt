package com.purchasely.samplev2.presentation.theme

import androidx.compose.ui.graphics.Color

// Brand palette
val Evergreen = Color(0xFF002420)
val DeepGreen = Color(0xFF003A32)
val InkBlack = Color(0xFF001614)
val FrostedMint = Color(0xFFCEECD0)
val MintCream = Color(0xFFEEF3E9)
val Lavender = Color(0xFFBDBBFF)
val LavenderSoft = Color(0xFFE9E8FF)
val LavenderInk = Color(0xFF4A2AD0)
val Coral = Color(0xFFFFB6B2)
val Cream = Color(0xFFFFF3D6)

// Neutrals on light surfaces, as alpha tints of the evergreen ink
val TextMuted = Color(0x99002420)
val Hairline = Color(0x1A002420)
val SurfaceInset = Color(0x0D002420)

// Neutrals on dark surfaces, as alpha tints of the mint cream
val MintWhite = Color(0xFFE6F2E3)
val MintMuted = Color(0x9EEEF3E9)
val MintHairline = Color(0x21EEF3E9)
val MintInset = Color(0x12EEF3E9)

// Input fields sit a shade under the card they are on, outlined a touch stronger than a divider
val FieldDark = Color(0xFF00302A)
val HairlineStrong = Color(0x2B002420)
val MintHairlineStrong = Color(0x38EEF3E9)

// Accents dimmed for dark surfaces
val LavenderDim = Color(0x2EBDBBFF)
val LavenderPale = Color(0xFFD7CCFF)
val CreamDim = Color(0x24FFF3D6)
val CoralDim = Color(0x29FFB6B2)

// Feedback. The soft variants are backgrounds for the matching foreground.
val Success = Color(0xFF17845C)
val Warning = Color(0xFF8A5C11)
val Danger = Color(0xFFC4443C)
val DangerSoft = Color(0x61FFB6B2)

/**
 * SDK status dot accents. Lighter than [Success] / [Warning] / [Danger], which lose contrast
 * against the evergreen header. The header stays dark in both themes, so these do not flip.
 */
val StatusOk = Color(0xFF7AD9AC)
val StatusPending = Color(0xFFFFD88A)
val StatusError = Coral
