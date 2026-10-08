package com.purchasely.samplev2.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Brand roles Material has no slot for. The hero stays dark in both themes while
 * [androidx.compose.material3.ColorScheme.primary] inverts to mint on dark, so a single role
 * cannot serve both.
 */
@Immutable
data class BrandColors(
    val hero: Color,
    val onHero: Color,
    /** Popup surfaces that sit above a card and need their own tint. */
    val raised: Color,
    /** Input fill and its outline, a shade under the card the field sits on. */
    val fieldBackground: Color,
    val fieldBorder: Color,
    val ok: Color,
    val warning: Color
)

private val LightBrand = BrandColors(
    hero = Evergreen,
    onHero = MintCream,
    raised = FrostedMint,
    fieldBackground = Color.White,
    fieldBorder = HairlineStrong,
    ok = Success,
    warning = Warning
)

private val DarkBrand = BrandColors(
    hero = InkBlack,
    onHero = FrostedMint,
    raised = DeepGreen,
    fieldBackground = FieldDark,
    fieldBorder = MintHairlineStrong,
    ok = StatusOk,
    warning = StatusPending
)

val LocalBrandColors = staticCompositionLocalOf { LightBrand }

/** Brand colours for the theme in scope, e.g. `Brand.colors.hero`. */
object Brand {
    val colors: BrandColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBrandColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = FrostedMint,
    onPrimary = Evergreen,
    primaryContainer = DeepGreen,
    onPrimaryContainer = FrostedMint,
    secondary = Lavender,
    onSecondary = InkBlack,
    secondaryContainer = LavenderDim,
    onSecondaryContainer = LavenderPale,
    tertiary = Coral,
    onTertiary = InkBlack,
    tertiaryContainer = CreamDim,
    onTertiaryContainer = StatusPending,
    background = Evergreen,
    onBackground = MintWhite,
    surface = DeepGreen,
    onSurface = MintWhite,
    surfaceVariant = MintInset,
    onSurfaceVariant = MintMuted,
    outline = MintHairline,
    outlineVariant = MintHairline,
    error = Coral,
    onError = InkBlack,
    errorContainer = CoralDim,
    onErrorContainer = Coral
)

private val LightColorScheme = lightColorScheme(
    primary = Evergreen,
    onPrimary = MintCream,
    primaryContainer = DeepGreen,
    onPrimaryContainer = FrostedMint,
    secondary = Lavender,
    onSecondary = InkBlack,
    secondaryContainer = LavenderSoft,
    onSecondaryContainer = LavenderInk,
    tertiary = Coral,
    onTertiary = InkBlack,
    tertiaryContainer = Cream,
    onTertiaryContainer = Warning,
    background = MintCream,
    onBackground = InkBlack,
    surface = Color.White,
    onSurface = InkBlack,
    surfaceVariant = SurfaceInset,
    onSurfaceVariant = TextMuted,
    outline = Hairline,
    outlineVariant = Hairline,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerSoft,
    onErrorContainer = Danger
)

@Composable
fun PurchaselyTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // The hero paints behind the status bar in both themes, so its icons stay light.
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalBrandColors provides if (darkTheme) DarkBrand else LightBrand
    ) {
        MaterialTheme(
            shapes = Shapes,
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
