package com.purchasely.samplev2.presentation.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable

/**
 * Bottom inset for a scrolling list, meant for `contentPadding` rather than a padding modifier:
 * as content padding the list still fills the screen and scrolls under the navigation bar, and
 * only comes to rest above it. As a modifier it would shrink the list instead.
 */
val navigationBarsContentPadding: PaddingValues
    @Composable
    get() = WindowInsets.navigationBars.asPaddingValues()
