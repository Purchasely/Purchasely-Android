package com.purchasely.samplev2.presentation.common

/**
 * Outcome of `Purchasely.start()`, shown as the pulsing status dot in the home header.
 * [STARTING] covers the window between `start()` being called and its callback firing.
 */
enum class SdkStatus { STARTING, STARTED, ERROR }
