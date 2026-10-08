package com.purchasely.samplev2.presentation.screen.home

import com.purchasely.samplev2.presentation.common.DisplayMethod
import com.purchasely.samplev2.presentation.common.SdkStatus
import io.purchasely.ext.presentation.PLYPresentation
import io.purchasely.views.presentation.models.PLYDimensionType
import io.purchasely.views.presentation.models.PLYTransitionType

/**
 * Data class that defines the ui state of [HomeScreen]
 */
data class HomeUiState(
    var sdkVersion : String = "",

    /** Drives the header status dot — nothing else works until the SDK has started. */
    var sdkStatus: SdkStatus = SdkStatus.STARTING,

    var userId: String = "",

    var anonymousUserId: String = "",

    var isAnonymous: Boolean = true,

    /** Observer mode leaves purchases to the app; Full mode lets Purchasely handle them. */
    var isObserverMode: Boolean = false,

    var themeMode: String = "",

    var allowDeeplink: Boolean = true,

    var allowCampaigns: Boolean = true,

    var planId: String = "",

    var placementId: String = "",

    var presentationId: String = "",

    var contentId: String = "",

    var isAsync : Boolean = false,

    var isRestoring : Boolean = false,

    var isSynchronizing : Boolean = false,

    var inlineScreen: PLYPresentation? = null,

    var displayMethod: DisplayMethod? = null,

    var transitionType: PLYTransitionType = PLYTransitionType.FULLSCREEN,

    var transitionHeightType: PLYDimensionType = PLYDimensionType.PERCENTAGE,

    var transitionHeightValue: String = "60",

    var transitionWidthType: PLYDimensionType = PLYDimensionType.PERCENTAGE,

    var transitionWidthValue: String = ""
)