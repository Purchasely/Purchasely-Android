package com.purchasely.samplev2.presentation.screen.settings

/**
 * Data class that defines the ui state of [SettingsScreen]
 */
data class SettingsUiState(
    val templatesList: MutableList<String> = mutableListOf(""),

    val storesList: MutableList<String> = mutableListOf(""),

    var apiKey: String = "fcb39be4-2ba4-4db7-bde3-2a5a1e20745d",

    var productId: String = "",

    var userId: String = "",

    var planId: String = "",

    var placementId: String = "",

    var contentId: String = "",

    var presentationId: String = "",

    var apiUrl: String = "",

    var paywallUrl: String = "",

    var template: String = "",

    var store: String = "",

    var isObserverMode: Boolean = false,

    var isAsyncLoading: Boolean = false,

    var isProductionMode: Boolean = false,

    var needRestart: Boolean = false,

    var placementHistory: List<String> = emptyList(),

    var presentationHistory: List<String> = emptyList()
)