package com.purchasely.samplev2.presentation.screen.paywall

import com.purchasely.samplev2.presentation.common.DisplayMethod
import io.purchasely.ext.presentation.PLYPresentation
import io.purchasely.ext.presentation.PLYPresentationPrepared

/**
 * Data class that defines the ui state of [PaywallScreen]
 */
data class PaywallUiState(

    val placementId: String? = null,

    val presentationId: String? = null,

    val contentId: String? = null,

    val prepared: PLYPresentationPrepared? = null,

    val presentation: PLYPresentation? = null,

    val displayMethod: DisplayMethod? = null,
)