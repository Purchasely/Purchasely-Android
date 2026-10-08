package com.purchasely.samplev2.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/** System fonts: the private Sample V2 brand fonts are commercial and not redistributed. */
val DisplayFont = FontFamily.Serif

val TextFont = FontFamily.SansSerif

private fun TextStyle.display() = copy(fontFamily = DisplayFont, fontWeight = FontWeight.Medium)

private fun TextStyle.text() = copy(fontFamily = TextFont)

val Typography = with(Typography()) {
    Typography(
        displayLarge = displayLarge.display(),
        displayMedium = displayMedium.display(),
        displaySmall = displaySmall.display(),
        headlineLarge = headlineLarge.display(),
        headlineMedium = headlineMedium.display(),
        headlineSmall = headlineSmall.display(),
        titleLarge = titleLarge.display(),
        titleMedium = titleMedium.text(),
        titleSmall = titleSmall.text(),
        // No colour on any style: text inherits LocalContentColor, so a container's contentColor
        // reaches its labels instead of being overridden by the ambient body style.
        bodyLarge = bodyLarge.text(),
        bodyMedium = bodyMedium.text(),
        bodySmall = bodySmall.text(),
        labelLarge = labelLarge.text(),
        labelMedium = labelMedium.text(),
        labelSmall = labelSmall.text()
    )
}
