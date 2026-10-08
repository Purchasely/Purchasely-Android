package com.purchasely.samplev2.presentation.screen.settings

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.purchasely.samplev2.SampleV2Application
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.ALLOW_CAMPAIGNS
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.SHOW_INLINE_SCREEN
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.ALLOW_DEEPLINK
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.API_KEY
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.DEBUG_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.DISPLAY_METHOD
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_HEIGHT_TYPE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_HEIGHT_VALUE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_WIDTH_TYPE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_WIDTH_VALUE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_TYPE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.HISTORY_PLACEMENT
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.HISTORY_PRESENTATION
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_FULL_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.LANGUAGE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRODUCT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.STORE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.THEME
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.common.DisplayMethod
import io.purchasely.ext.Purchasely
import io.purchasely.views.presentation.PLYThemeMode
import io.purchasely.views.presentation.models.PLYDimensionType
import io.purchasely.views.presentation.models.PLYTransitionType
import java.util.Locale

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
            displayMethod = DisplayMethod.entries[preferencesRepository.getInt(DISPLAY_METHOD)],
            transitionType = preferencesRepository.getString(TRANSITION_TYPE)
                ?.let { runCatching { PLYTransitionType.valueOf(it) }.getOrNull() }
                ?: PLYTransitionType.FULLSCREEN,
            transitionHeightType = preferencesRepository.getString(TRANSITION_HEIGHT_TYPE)
                ?.let { runCatching { PLYDimensionType.valueOf(it) }.getOrNull() }
                ?: PLYDimensionType.PERCENTAGE,
            transitionHeightValue = preferencesRepository.getString(TRANSITION_HEIGHT_VALUE) ?: "60",
            transitionWidthType = preferencesRepository.getString(TRANSITION_WIDTH_TYPE)
                ?.let { runCatching { PLYDimensionType.valueOf(it) }.getOrNull() }
                ?: PLYDimensionType.PERCENTAGE,
            transitionWidthValue = preferencesRepository.getString(TRANSITION_WIDTH_VALUE) ?: "",
            isObserverMode = !preferencesRepository.getBoolean(IS_FULL_MODE),
            allowDeeplink = preferencesRepository.getBoolean(ALLOW_DEEPLINK, default = true),
            allowCampaigns = preferencesRepository.getBoolean(ALLOW_CAMPAIGNS, default = true),
            showInlineScreen = preferencesRepository.getBoolean(SHOW_INLINE_SCREEN, default = false),
            placementHistory = preferencesRepository.getHistory(HISTORY_PLACEMENT),
            presentationHistory = preferencesRepository.getHistory(HISTORY_PRESENTATION),
            debugMode = preferencesRepository.getBoolean(DEBUG_MODE, default = true),
            language = preferencesRepository.getString(LANGUAGE) ?: "",
        )
    }

    fun saveSettings() {
        with(uiState.value) {
            preferencesRepository.setString(PRODUCT_ID, productId)
            preferencesRepository.setString(CONTENT_ID, contentId)
            preferencesRepository.setString(PLACEMENT_ID, placementId)
            preferencesRepository.setString(PRESENTATION_ID, presentationId)
            if(userId.isNotBlank()) preferencesRepository.setString(USER_ID, userId)
            else preferencesRepository.removeKey(USER_ID)
            preferencesRepository.setString(API_KEY, apiKey)
            preferencesRepository.setString(STORE, store)
            preferencesRepository.setString(THEME, theme)
            preferencesRepository.setBoolean(IS_FULL_MODE, !isObserverMode)
            preferencesRepository.setInt(DISPLAY_METHOD, displayMethod.ordinal)
            preferencesRepository.setString(TRANSITION_TYPE, transitionType.name)
            preferencesRepository.setString(TRANSITION_HEIGHT_TYPE, transitionHeightType.name)
            preferencesRepository.setString(TRANSITION_HEIGHT_VALUE, transitionHeightValue)
            preferencesRepository.setString(TRANSITION_WIDTH_TYPE, transitionWidthType.name)
            preferencesRepository.setString(TRANSITION_WIDTH_VALUE, transitionWidthValue)
            preferencesRepository.setBoolean(ALLOW_DEEPLINK, allowDeeplink)
            preferencesRepository.setBoolean(ALLOW_CAMPAIGNS, allowCampaigns)
            preferencesRepository.setBoolean(SHOW_INLINE_SCREEN, showInlineScreen)
            preferencesRepository.setBoolean(DEBUG_MODE, debugMode)
            preferencesRepository.addToHistory(HISTORY_PRESENTATION, presentationId)
            preferencesRepository.addToHistory(HISTORY_PLACEMENT, placementId)

            when(theme) {
                "LIGHT" -> Purchasely.setThemeMode(PLYThemeMode.LIGHT)
                "DARK" -> Purchasely.setThemeMode(PLYThemeMode.DARK)
                "SYSTEM" -> Purchasely.setThemeMode(PLYThemeMode.SYSTEM)
            }

            Purchasely.debugMode = debugMode
            preferencesRepository.setString(LANGUAGE, language)
            Purchasely.language = language.takeIf { it.isNotBlank() }?.let { Locale(it) }

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

