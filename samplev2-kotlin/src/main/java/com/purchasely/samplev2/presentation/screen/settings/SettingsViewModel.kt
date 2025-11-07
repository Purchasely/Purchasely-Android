package com.purchasely.samplev2.presentation.screen.settings

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.purchasely.samplev2.SampleV2Application
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.API_KEY
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.HISTORY_PLACEMENT
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.HISTORY_PRESENTATION
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_ASYNC_LOADING
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_OBSERVER_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRODUCT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.STORE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.THEME
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import io.purchasely.ext.Purchasely
import io.purchasely.views.presentation.PLYThemeMode

class SettingsViewModel(
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
    }

    private fun getSettings() {
        uiState.value = uiState.value.copy(
            presentationId = preferencesRepository.getString(PRESENTATION_ID) ?: "",
            placementId = preferencesRepository.getString(PLACEMENT_ID) ?: "",
            productId = preferencesRepository.getString(PRODUCT_ID) ?: "",
            contentId = preferencesRepository.getString(CONTENT_ID) ?: "",
            userId = preferencesRepository.getString(USER_ID) ?: "",
            apiKey = preferencesRepository.getString(API_KEY) ?: "",
            store = preferencesRepository.getString(STORE) ?: "",
            theme = preferencesRepository.getString(THEME) ?: "",
            isAsyncLoading = preferencesRepository.getBoolean(IS_ASYNC_LOADING),
            isObserverMode = preferencesRepository.getBoolean(IS_OBSERVER_MODE),
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
            preferencesRepository.setString(API_KEY, apiKey)
            preferencesRepository.setString(STORE, store)
            preferencesRepository.setString(THEME, theme)
            preferencesRepository.setBoolean(IS_OBSERVER_MODE, isObserverMode)
            preferencesRepository.setBoolean(IS_ASYNC_LOADING, isAsyncLoading)
            preferencesRepository.addToHistory(HISTORY_PRESENTATION, presentationId)
            preferencesRepository.addToHistory(HISTORY_PLACEMENT, placementId)
            if(userId.isNotBlank())
                preferencesRepository.setString(USER_ID, userId)
            else
                preferencesRepository.removeKey(USER_ID)

            when(theme) {
                "LIGHT" -> Purchasely.setThemeMode(PLYThemeMode.LIGHT)
                "DARK" -> Purchasely.setThemeMode(PLYThemeMode.DARK)
                "SYSTEM" -> Purchasely.setThemeMode(PLYThemeMode.SYSTEM)
            }

            if (userId.isNotBlank())
                Purchasely.userLogin(userId)
            else
                Purchasely.userLogout()

            if (needRestart) {
                (application as SampleV2Application).apply {
                    startPurchasely(apiKey, userId)
                }
            }
        }
    }

    private fun getStores() {
        val stores = mutableListOf("Google", "Amazon", "Huawei")
        uiState.value = uiState.value.copy(storesList = stores)
    }
}

