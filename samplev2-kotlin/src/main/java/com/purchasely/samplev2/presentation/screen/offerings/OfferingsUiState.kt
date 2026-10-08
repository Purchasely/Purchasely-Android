package com.purchasely.samplev2.presentation.screen.offerings

import io.purchasely.models.PLYProduct
import io.purchasely.storage.userData.PLYDynamicOffering


/**
 * Data class that defines the ui state of [OfferingsScreen]
 */
data class OfferingsUiState(
    val error: String? = null,
    val loading : Boolean = false,
    val offerings: List<PLYDynamicOffering> = emptyList(),
    val products: List<PLYProduct> = emptyList(),
)