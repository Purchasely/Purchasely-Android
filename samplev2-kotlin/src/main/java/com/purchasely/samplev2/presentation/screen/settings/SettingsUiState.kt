package com.purchasely.samplev2.presentation.screen.settings

import com.purchasely.samplev2.presentation.common.DisplayMethod
import io.purchasely.views.presentation.models.PLYDimensionType
import io.purchasely.views.presentation.models.PLYTransitionType
/**
 * Data class that defines the ui state of [SettingsScreen]
 */
data class SettingsUiState(
    val storesList: MutableList<String> = mutableListOf(""),

    var apiKey: String = "fcb39be4-2ba4-4db7-bde3-2a5a1e20745d",

    var productId: String = "",

    var userId: String = "",

    var planId: String = "",

    var placementId: String = "",

    var contentId: String = "",

    var presentationId: String = "",

    var store: String = "",

    var isObserverMode: Boolean = false,

    var isAsyncLoading: Boolean = false,

    var allowDeeplink: Boolean = true,

    var allowCampaigns: Boolean = true,
    var showInlineScreen: Boolean = false,

    var needRestart: Boolean = false,

    var placementHistory: List<String> = emptyList(),

    var presentationHistory: List<String> = emptyList(),

    val themesList: List<String> = listOf("LIGHT", "DARK", "SYSTEM"),

    var theme: String = "",

    var displayMethod: DisplayMethod = DisplayMethod.FETCH_DISPLAY,

    var transitionType: PLYTransitionType = PLYTransitionType.FULLSCREEN,

    var transitionHeightType: PLYDimensionType = PLYDimensionType.PERCENTAGE,

    var transitionHeightValue: String = "60",

    var transitionWidthType: PLYDimensionType = PLYDimensionType.PERCENTAGE,

    var transitionWidthValue: String = "",

    /** `Purchasely.debugMode`. */
    var debugMode: Boolean = true,

    /** SDK language: a code from [languagesList], or "" to follow the device. */
    var language: String = "",

    val languagesList: List<String> = listOf("", "en", "fr", "de", "es", "it", "pt", "ja")
)