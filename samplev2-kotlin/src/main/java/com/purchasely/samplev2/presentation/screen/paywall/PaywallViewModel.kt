package com.purchasely.samplev2.presentation.screen.paywall

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.DISPLAY_METHOD
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.common.DisplayMethod
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import io.purchasely.ext.presentation.PLYPresentation
import io.purchasely.ext.presentation.PLYPresentationPrepared
import io.purchasely.ext.presentation.preload
import io.purchasely.models.PLYError
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var uiState = mutableStateOf(PaywallUiState())

    init {
        with(preferencesRepository) {
            uiState.value = uiState.value.copy(
                placementId = getString(PLACEMENT_ID).takeUnless { it?.isBlank() == true },
                presentationId = getString(PRESENTATION_ID).takeUnless { it?.isBlank() == true },
                contentId = getString(CONTENT_ID).takeUnless { it?.isBlank() == true },
                displayMethod = getInt(DISPLAY_METHOD).let { DisplayMethod.entries[it] },
                prepared = buildPrepared()
            ).apply {
                // Both embedded modes build their view from the preloaded presentation.
                prefetchScreen(
                    onLoaded = {
                        uiState.value = uiState.value.copy(presentation = it)
                        Log.d(TAG, "[PaywallViewModel] Prefetched presentation: $it")
                    },
                    onError = {
                        Log.e(TAG, "[PaywallViewModel] Error prefetching presentation: $it")
                    }
                )
            }
        }
    }

    /**
     * Build a prepared presentation that mirrors the user-selected identifiers.
     */
    private fun buildPrepared(): PLYPresentationPrepared = PLYPresentation {
        preferencesRepository.getString(PLACEMENT_ID).takeUnless { it?.isBlank() == true }?.let { placementId(it) }
        preferencesRepository.getString(PRESENTATION_ID).takeUnless { it?.isBlank() == true }?.let { screenId(it) }
        preferencesRepository.getString(CONTENT_ID).takeUnless { it?.isBlank() == true }?.let { contentId(it) }
    }

    fun prefetchScreen(onLoaded: (PLYPresentation?) -> Unit = {}, onError: (PLYError?) -> Unit = {}) {
        val prepared = buildPrepared()

        viewModelScope.launch {
            try {
                val presentation = prepared.preload()
                onLoaded(presentation)
            } catch (e: Throwable) {
                onError(e as? PLYError ?: PLYError.Unknown(IllegalStateException(e.message, e)))
            }
        }
    }
}
