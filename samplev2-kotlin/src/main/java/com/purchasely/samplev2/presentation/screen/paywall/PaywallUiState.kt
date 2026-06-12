package com.purchasely.samplev2.presentation.screen.paywall

/**
 * Data class that defines the ui state of [PaywallScreen]
 */
data class PaywallUiState(

    var placementId: String? = null,

    var presentationId: String? = null,

    var contentId: String? = null,

    var asyncLoading: Boolean = false,
)
