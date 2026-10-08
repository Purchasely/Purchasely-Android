package com.purchasely.samplev2.presentation.screen.subscriptions

import androidx.lifecycle.ViewModel
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * ViewModel backing [SubscriptionsScreen]. Loads the user's active subscriptions via
 * [Purchasely.userSubscriptions] and exposes them as [SubscriptionsUiState].
 */
class SubscriptionsViewModel : ViewModel() {

    val uiState = MutableStateFlow(SubscriptionsUiState())

    init {
        load()
    }

    /**
     * Fetches the active subscriptions. [invalidateCache] forces a network refresh (used by the
     * refresh action) instead of serving the in-memory cache.
     */
    fun load(invalidateCache: Boolean = false) {
        uiState.value = uiState.value.copy(isLoading = true, error = null)
        // The callback overload dispatches its result on the main thread, so it is safe to update
        // the state flow directly from here.
        Purchasely.userSubscriptions(
            invalidateCache = invalidateCache,
            onSuccess = { subscriptions ->
                uiState.value = SubscriptionsUiState(isLoading = false, subscriptions = subscriptions)
            },
            onError = { error ->
                uiState.value = SubscriptionsUiState(
                    isLoading = false,
                    subscriptions = emptyList(),
                    error = error.message ?: "Unable to load subscriptions"
                )
            }
        )
    }
}
