package com.purchasely.samplev2.presentation.screen.attributes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.purchasely.ext.PLYDataProcessingLegalBasis
import io.purchasely.ext.PLYLogger
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

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
    fun addAttribute(key: String, value: String, type: String, legal: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val legalBasis = when (legal) {
            PLYDataProcessingLegalBasis.ESSENTIAL.name -> PLYDataProcessingLegalBasis.ESSENTIAL
            else -> PLYDataProcessingLegalBasis.OPTIONAL
        }
        when(type) {
            Integer::class.simpleName -> value.toIntOrNull()?.let {
                Purchasely.setUserAttribute(key, it, legalBasis)
            }
            Float::class.simpleName -> value.toFloatOrNull()?.let {
                Purchasely.setUserAttribute(key, it, legalBasis)
            }
            Boolean::class.simpleName -> value.toBooleanStrictOrNull()?.let {
                Purchasely.setUserAttribute(key, it, legalBasis)
            }

            String::class.simpleName -> Purchasely.setUserAttribute(key, value, legalBasis)
            Date::class.simpleName -> {
                try {
                    dateFormat.parse(value)?.let {
                        Purchasely.setUserAttribute(key, it, legalBasis)
                    }
                } catch (e: Exception) {
                    PLYLogger.e("Unable to parse date $value", e)
                }
            }

            "IntArray" -> {
                val array = value.split(";").mapNotNull { it.toIntOrNull() }
                Purchasely.setUserAttribute(key, array.toTypedArray<Int>(), legalBasis)
            }
            "FloatArray" -> {
                val array = value.split(";").mapNotNull { it.toFloatOrNull() }
                Purchasely.setUserAttribute(key, array.toTypedArray<Float>(), legalBasis)
            }
            "BooleanArray" -> {
                val array = value.split(";").mapNotNull { it.toBooleanStrictOrNull() }
                Purchasely.setUserAttribute(key, array.toTypedArray<Boolean>(), legalBasis)
            }
            "StringArray" -> {
                val array = value.split(";")
                Purchasely.setUserAttribute(key, array.toTypedArray<String>(), legalBasis)
            }

        }
        refreshAttributes()
    }

    /** Increment or decrement the Int attribute [key] by [amount] (blank means 1). */
    fun stepAttribute(key: String, amount: String, legal: String, increment: Boolean) {
        val legalBasis = if (legal == PLYDataProcessingLegalBasis.ESSENTIAL.name) PLYDataProcessingLegalBasis.ESSENTIAL else PLYDataProcessingLegalBasis.OPTIONAL
        val value = amount.toIntOrNull() ?: 1
        if (increment) Purchasely.incrementUserAttribute(key, value, legalBasis)
        else Purchasely.decrementUserAttribute(key, value, legalBasis)
        refreshAttributes()
    }

    /** Remove every user attribute. */
    fun clearAttributes() {
        Purchasely.clearUserAttributes()
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
        viewModelScope.launch {
            delay(1.seconds)
            uiState.value = uiState.value.copy(
                attributes = mutableMapOf<String, Any>().apply { putAll(Purchasely.userAttributes()) }
            )
        }
    }
}

