package com.purchasely.samplev2.presentation.screen.paywall

import androidx.lifecycle.ViewModel
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_ASYNC_LOADING
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TEMPLATE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_OBSERVER_MODE
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.purchasely.ext.PLYPresentationViewProperties
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var uiState = MutableStateFlow(PaywallUiState())

    init {
        with(preferencesRepository) {
            uiState.value = uiState.value.copy(
                template = getString(TEMPLATE).takeUnless { it == "None" || it?.isEmpty() == true },
                placementId = getString(PLACEMENT_ID).takeUnless { it?.isBlank() == true },
                presentationId = getString(PRESENTATION_ID).takeUnless { it?.isBlank() == true },
                asyncLoading = getBoolean(IS_ASYNC_LOADING),
                properties = getProperties()
            )
        }
    }

    /**
     * Set purchasely properties.
     */
    private fun getProperties() =
        PLYPresentationViewProperties(
            placementId = preferencesRepository.getString(PLACEMENT_ID).takeUnless { it?.isBlank() == true },
            presentationId = preferencesRepository.getString(PRESENTATION_ID).takeUnless { it?.isBlank() == true },
            contentId = preferencesRepository.getString(CONTENT_ID).takeUnless { it?.isBlank() == true },
            onLoaded = {}
        )

    fun observerMode() = preferencesRepository.getBoolean(IS_OBSERVER_MODE)
}
