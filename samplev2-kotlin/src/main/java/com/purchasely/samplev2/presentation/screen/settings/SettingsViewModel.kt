package com.purchasely.samplev2.presentation.screen.settings

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.purchasely.samplev2.SampleV2Application
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.API_KEY
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.API_URL
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_ASYNC_LOADING
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_OBSERVER_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_PRODUCTION_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PAYWALL_URL
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.HISTORY_PLACEMENT
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.HISTORY_PRESENTATION
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRODUCT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.STORE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TEMPLATE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.purchasely.ext.Purchasely
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /**
     * Stateflow of [SettingsUiState].
     */
    var uiState = mutableStateOf(SettingsUiState())

    init {
        getSettings()
        getStores()
        getLocalTemplates()
    }

    private fun getSettings() {
        uiState.value = uiState.value.copy(
            presentationId = preferencesRepository.getString(PRESENTATION_ID) ?: "",
            placementId = preferencesRepository.getString(PLACEMENT_ID) ?: "",
            productId = preferencesRepository.getString(PRODUCT_ID) ?: "",
            contentId = preferencesRepository.getString(CONTENT_ID) ?: "",
            userId = preferencesRepository.getString(USER_ID) ?: "",
            apiKey = preferencesRepository.getString(API_KEY) ?: "",
            apiUrl = preferencesRepository.getString(API_URL) ?: "",
            paywallUrl = preferencesRepository.getString(PAYWALL_URL) ?: "",
            template = preferencesRepository.getString(TEMPLATE) ?: "",
            store = preferencesRepository.getString(STORE) ?: "",
            isAsyncLoading = preferencesRepository.getBoolean(IS_ASYNC_LOADING),
            isObserverMode = preferencesRepository.getBoolean(IS_OBSERVER_MODE),
            isProductionMode = preferencesRepository.getBoolean(IS_PRODUCTION_MODE),
            placementHistory = preferencesRepository.getHistory(HISTORY_PLACEMENT),
            presentationHistory = preferencesRepository.getHistory(HISTORY_PRESENTATION),
        )
    }

    fun saveSettings() {
        with(uiState.value) {
            preferencesRepository.setString(PRODUCT_ID, productId)
            preferencesRepository.setString(CONTENT_ID, contentId)
            preferencesRepository.setString(PLACEMENT_ID, placementId)
            preferencesRepository.setString(PRESENTATION_ID, presentationId)
            preferencesRepository.setString(USER_ID, userId)
            preferencesRepository.setString(API_KEY, apiKey)
            preferencesRepository.setString(API_URL, apiUrl)
            preferencesRepository.setString(PAYWALL_URL, paywallUrl)
            preferencesRepository.setString(TEMPLATE, template)
            preferencesRepository.setString(STORE, store)
            preferencesRepository.setBoolean(IS_OBSERVER_MODE, isObserverMode)
            preferencesRepository.setBoolean(IS_ASYNC_LOADING, isAsyncLoading)
            preferencesRepository.setBoolean(IS_PRODUCTION_MODE, isProductionMode)
            preferencesRepository.addToHistory(HISTORY_PRESENTATION, presentationId)
            preferencesRepository.addToHistory(HISTORY_PLACEMENT, placementId)

            if (userId.isNotBlank())
                Purchasely.userLogin(userId)
            else
                Purchasely.userLogout()

            if (needRestart) {
                (application as SampleV2Application).apply {
                    setEnvironment(isProductionMode, apiUrl, paywallUrl)
                    startPurchasely(apiKey, userId)
                }
            }
        }
    }

    private fun getLocalTemplates() {
        val templates = mutableListOf("None")
        application.applicationContext.assets.list("templates")?.mapTo(templates) {
            it.removeSuffix(".json")
        }
        uiState.value = uiState.value.copy(templatesList = templates)
    }

    private fun getStores() {
        val stores = mutableListOf("Google", "Amazon", "Huawei")
        uiState.value = uiState.value.copy(storesList = stores)
    }
}

