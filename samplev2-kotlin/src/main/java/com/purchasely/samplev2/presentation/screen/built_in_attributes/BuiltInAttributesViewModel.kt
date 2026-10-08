package com.purchasely.samplev2.presentation.screen.built_in_attributes

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Home viewModel, providing [BuiltInAttributesUiState] and methods for [BuiltInAttributesScreen].
 */
class BuiltInAttributesViewModel() : ViewModel() {

    val uiState = MutableStateFlow(BuiltInAttributesUiState())

    init {
        retrieveBuiltInAttributes()
    }

    /**
     * Retrieve all the user's attributes.
     */
    @SuppressLint("DefaultLocale")
    fun retrieveBuiltInAttributes() {
        uiState.value = uiState.value.copy(
            attributes = mutableMapOf<String, Any>().apply {
                Purchasely.getBuiltInAttributes().forEach {
                    if(it.key.contains("revenue"))
                        put(it.key, "${it.value} $")
                    else
                        put(it.key, it.value)

                }
            }
        )
    }

    /**
     * Save [text] to clipboard.
     */
    fun saveToClipboard(context: Context, label: String, text: String) {
        viewModelScope.launch {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboardManager.setPrimaryClip(clip)
        }
    }

    fun clearAllUserAttributes() {
        Purchasely.clearBuiltInAttributes()
        retrieveBuiltInAttributes()
    }
}

