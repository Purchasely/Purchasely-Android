package com.purchasely.samplev2.presentation.screen.home

/**
 * Data class that defines the ui state of [HomeScreen]
 */
data class HomeUiState(
    var userId: String = "",

    var planId: String = "",

    var placementId: String = "",

    var presentationId: String = "",

    var contentId: String = "",

    var isAsync : Boolean = false,

    var isRestoring : Boolean = false,
)