package com.purchasely.samplev2.presentation.screen.privacy

import android.util.Log
import androidx.lifecycle.ViewModel
import io.purchasely.ext.PLYDataProcessingPurpose
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.flow.MutableStateFlow

private val NON_ESSENTIALS = setOf(
    PLYDataProcessingPurpose.Analytics,
    PLYDataProcessingPurpose.ThirdPartyIntegrations,
    PLYDataProcessingPurpose.IdentifiedAnalytics,
    PLYDataProcessingPurpose.Personalization,
    PLYDataProcessingPurpose.Campaigns
)

class PrivacyViewModel : ViewModel() {

    val uiState = MutableStateFlow(PrivacyUiState())

    init {
        refreshState()
    }

    // shortcut: revoked purposes live in view-model memory (no public getter), so they reset at restart.
    fun setPurposes(purposes: Set<PLYDataProcessingPurpose>) {
        Purchasely.revokeDataProcessingConsent(purposes)
        Log.d("PrivacyViewModel", "Revoking: $purposes")
        uiState.value = uiState.value.copy(
            purposes = if (PLYDataProcessingPurpose.AllNonEssentials in purposes) NON_ESSENTIALS else purposes
        )
        refreshState()
    }

    fun refreshState() {
        uiState.value = uiState.value.copy(
            all = uiState.value.purposes.containsAll(NON_ESSENTIALS)
        )
        Log.d("PrivacyViewModel", "Current purposes: ${uiState.value.purposes}")
    }
}

data class PrivacyUiState(
    val purposes: Set<PLYDataProcessingPurpose> = mutableSetOf(),
    val all: Boolean = false
)