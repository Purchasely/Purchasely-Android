package com.purchasely.samplev2.presentation.screen.attributes

/**
 * Data class that defines the ui state of [AttributesScreen]
 */
data class AttributesUiState(

    val attributes: MutableMap<String, Any> = mutableMapOf()
)