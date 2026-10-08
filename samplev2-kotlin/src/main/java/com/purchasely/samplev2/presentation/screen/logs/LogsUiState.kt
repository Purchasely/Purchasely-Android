package com.purchasely.samplev2.presentation.screen.logs

import com.purchasely.samplev2.domain.log.LogMessage

/**
 * Data class that defines the ui state of [LogsScreen]
 */
data class LogsUiState(
    val keyword: String = "",
    val logs: List<LogMessage> = emptyList(),
)