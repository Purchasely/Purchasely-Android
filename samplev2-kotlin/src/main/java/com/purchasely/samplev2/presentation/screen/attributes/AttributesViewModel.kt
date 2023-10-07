package com.purchasely.samplev2.presentation.screen.attributes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date
import javax.inject.Inject

/**
 * Home viewModel, providing [AttributesUiState] and methods for [AttributesScreen].
 */
@HiltViewModel
class AttributesViewModel @Inject constructor(): ViewModel() {

    val uiState = MutableStateFlow(AttributesUiState())

    init {
        refreshAttributes()
    }

    /**
     * Add a new user attribute.
     */
    fun addAttribute(key: String, value: Any) {
        when(value) {
            is Int -> Purchasely.setUserAttribute(key, value)
            is Float -> Purchasely.setUserAttribute(key, value)
            is String -> Purchasely.setUserAttribute(key, value)
            is Date -> Purchasely.setUserAttribute(key, value)
        }
        refreshAttributes()
    }

    /**
     * Remove the user attribute corresponding the [key].
     */
    fun removeAttribute(key: String) {
        Purchasely.clearUserAttribute(key)
        refreshAttributes()
    }

    /**
     * Retrieve all the user's attributes.
     */
    private fun refreshAttributes() {
        uiState.value = uiState.value.copy(
            attributes = mutableMapOf<String, Any>().apply { putAll(Purchasely.userAttributes()) }
        )
    }
}

