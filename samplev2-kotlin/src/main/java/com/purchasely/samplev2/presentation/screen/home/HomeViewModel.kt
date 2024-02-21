package com.purchasely.samplev2.presentation.screen.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_ASYNC_LOADING
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLAN_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import io.purchasely.ext.Attribute
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home viewModel, providing [HomeUiState] and methods for [HomeScreen].
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /**
     * Stateflow of [HomeUiState].
     */
    var uiState = MutableStateFlow(HomeUiState())

    init {
        getSettings()
        setPurchaselyAttributes()
    }

    /**
     * Restore previous purchases.
     */
    fun restore() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isRestoring = true)
            delay(1000)
            Purchasely.restoreAllProducts(
                onSuccess = {
                    Log.d(TAG, "Restored Plan is $it")
                    uiState.value = uiState.value.copy(isRestoring = false)
                },
                onError = {
                    Log.e(TAG, "Restoring error ${it?.message}", it)
                    uiState.value = uiState.value.copy(isRestoring = false)
                }
            )
        }
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

    /**
     * Set user attributes.
     */
    private fun setPurchaselyAttributes() {
        Purchasely.setAttribute(Attribute.CUSTOMERIO_USER_EMAIL, "support@purchasely.com")
        Purchasely.setAttribute(Attribute.MPARTICLE_USER_ID, uiState.value.userId)
        Purchasely.setAttribute(Attribute.CUSTOMERIO_USER_ID, uiState.value.userId)
        Purchasely.setAttribute(Attribute.BRANCH_USER_DEVELOPER_IDENTITY, uiState.value.userId)
    }

    /**
     * Retrieve current settings (preferences).
     */
    private fun getSettings() {
        uiState.value = uiState.value.copy(
            presentationId = preferencesRepository.getString(PRESENTATION_ID) ?: "",
            placementId = preferencesRepository.getString(PLACEMENT_ID) ?: "",
            contentId = preferencesRepository.getString(CONTENT_ID) ?: "",
            userId = preferencesRepository.getString(USER_ID) ?: "",
            planId = preferencesRepository.getString(PLAN_ID) ?: "",
            isAsync = preferencesRepository.getBoolean(IS_ASYNC_LOADING)
        )
    }

    fun displayPropertiesCard() =
        uiState.value.userId.isBlank().not() ||
        uiState.value.contentId.isBlank().not() ||
        uiState.value.presentationId.isBlank().not() ||
        uiState.value.placementId.isBlank().not()
}

