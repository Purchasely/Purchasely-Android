package com.purchasely.samplev2.presentation.screen.subscriptions

import io.purchasely.models.PLYSubscriptionData

/**
 * UI state of [SubscriptionsScreen]: the result of [io.purchasely.ext.Purchasely.userSubscriptions].
 */
data class SubscriptionsUiState(
    val isLoading: Boolean = true,
    val subscriptions: List<PLYSubscriptionData> = emptyList(),
    val error: String? = null
)
