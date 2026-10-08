package com.purchasely.samplev2.presentation.screen.offerings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class OfferingsViewModel(): ViewModel() {

    val uiState = MutableStateFlow(OfferingsUiState())

    init {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(loading = true)
            refreshProducts()
            refreshDynamicOfferings()
            uiState.value = uiState.value.copy(loading = false)
        }
    }

    fun addDynamicOffering(reference: String, planId: String, offerId: String? = null) {
        viewModelScope.launch {
            val isAdded = Purchasely.setDynamicOffering(reference = reference, planVendorId = planId, offerVendorId = offerId)
            if (!isAdded) {
                uiState.value = uiState.value.copy(
                    error = "Dynamic offerings limit reached." +
                            "\nTo unlock more, please send a handwritten letter to @BackendTeam explaining why you deserve more offerings" +
                            "\nAnd don’t forget the ☕️, 🥐 and 🥙 to boost your chances."
                )
                return@launch
            }
            refreshDynamicOfferings()
        }
    }


    fun removeDynamicOffering(reference: String) {
        viewModelScope.launch {
            Purchasely.removeDynamicOffering(reference = reference)
            refreshDynamicOfferings()
        }
    }


    private suspend fun refreshDynamicOfferings() {
        uiState.value = uiState.value.copy(
            offerings = Purchasely.getDynamicOfferings()
        )
    }

    private suspend fun refreshProducts() {
        uiState.value = uiState.value.copy(
            products = Purchasely.allProducts()
        )
    }

    fun hideError() {
        uiState.value = uiState.value.copy(
            error = null
        )
    }
}

