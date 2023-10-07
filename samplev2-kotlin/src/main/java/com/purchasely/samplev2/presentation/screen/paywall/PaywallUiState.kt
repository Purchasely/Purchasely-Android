package com.purchasely.samplev2.presentation.screen.paywall

import io.purchasely.ext.PLYPresentation
import io.purchasely.ext.PLYPresentationViewProperties

/**
 * Data class that defines the ui state of [PaywallScreen]
 */
data class PaywallUiState(

    var template: String? = null,

    var placementId: String? = null,

    var presentationId: String? = null,

    var asyncLoading: Boolean = false,

    var properties: PLYPresentationViewProperties = PLYPresentationViewProperties(),

    var presentation : PLYPresentation? = null
)