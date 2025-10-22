package com.purchasely.samplev2.presentation.screen.attributes

import android.util.Log
import androidx.lifecycle.ViewModel
import com.purchasely.samplev2.presentation.util.Constants
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home viewModel, providing [AttributesUiState] and methods for [AttributesScreen].
 */
class AttributesViewModel() : ViewModel() {

    val uiState = MutableStateFlow(AttributesUiState())

    init {
        refreshAttributes()
    }

    /**
     * Add a new user attribute.
     */
    fun addAttribute(key: String, value: String, type: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        when (type) {
            Integer::class.simpleName -> value.toIntOrNull()?.let {
                Purchasely.setUserAttribute(key, it)
            }

            Float::class.simpleName -> value.toFloatOrNull()?.let {
                Purchasely.setUserAttribute(key, it)
            }

            Boolean::class.simpleName -> value.toBooleanStrictOrNull()?.let {
                Purchasely.setUserAttribute(key, it)
            }

            String::class.simpleName -> Purchasely.setUserAttribute(key, value)
            Date::class.simpleName -> {
                try {
                    dateFormat.parse(value)?.let {
                        Purchasely.setUserAttribute(key, it)
                    }
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Unable to parse date $value", e)
                }
            }
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

