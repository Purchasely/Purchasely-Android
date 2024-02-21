package com.purchasely.samplev2.presentation.screen.settings

/**
 * Data class that defines the ui state of [SettingsScreen]
 */
data class SettingsUiState(
    val templatesList: MutableList<String> = mutableListOf(""),

    val storesList: MutableList<String> = mutableListOf(""),

    var apiKey: String = "",

    var productId: String = "",

    var userId: String = "",

    var planId: String = "",

    var placementId: String = "",

    var contentId: String = "",

    var presentationId: String = "",

    var store: String = "",

    var isObserverMode: Boolean = false,

    var isAsyncLoading: Boolean = false,

    var needRestart: Boolean = false,

    var placementHistory: List<String> = emptyList(),

    var presentationHistory: List<String> = emptyList(),

    val themesList: List<String> = listOf("LIGHT", "DARK", "SYSTEM"),

    var theme: String = ""
)