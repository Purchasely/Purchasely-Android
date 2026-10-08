package com.purchasely.samplev2.presentation.screen.logs

import android.app.Application
import androidx.lifecycle.ViewModel
import com.purchasely.samplev2.SampleV2Application
import kotlinx.coroutines.flow.MutableStateFlow

class LogsViewModel(
    private val application: Application
) : ViewModel() {

    var uiState = MutableStateFlow(LogsUiState())

    init {
        uiState.value = uiState.value.copy(
            logs = (application as SampleV2Application).logs.filter {
                it.message.contains(uiState.value.keyword)
            }
        )
    }

    /**
     * Set purchasely properties.
     */
    fun clearLogs() {
        (application as SampleV2Application).logs.clear()
        uiState.value = uiState.value.copy(logs = emptyList())
    }

    fun updateKeyword(keyword: String) {
        uiState.value = uiState.value.copy(
            keyword = keyword,
            logs = (application as SampleV2Application).logs.filter {
                it.message.lowercase().contains(keyword.lowercase())
            }
        )
    }
}
