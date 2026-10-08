package com.purchasely.samplev2.presentation.screen.home

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.purchasely.samplev2.SampleV2Application
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.ALLOW_CAMPAIGNS
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.SHOW_INLINE_SCREEN
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.DISPLAY_METHOD
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_FULL_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLAN_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_HEIGHT_TYPE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_HEIGHT_VALUE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_WIDTH_TYPE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_WIDTH_VALUE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.TRANSITION_TYPE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.common.DisplayMethod
import com.purchasely.samplev2.presentation.navigation.Screen
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import com.purchasely.samplev2.presentation.util.purchase
import io.purchasely.ext.Attribute
import io.purchasely.ext.PLYInterceptResult
import io.purchasely.ext.presentation.PLYPresentation
import io.purchasely.ext.presentation.PLYPresentationAction
import io.purchasely.ext.Purchasely
import io.purchasely.ext.presentation.display
import io.purchasely.ext.presentation.preload
import io.purchasely.views.presentation.models.PLYDimensionType
import io.purchasely.views.presentation.models.PLYTransitionDimension
import io.purchasely.views.presentation.models.PLYTransitionType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.toColorInt
import io.purchasely.views.presentation.models.PLYTransition

/**
 * Home viewModel, providing [HomeUiState] and methods for [HomeScreen].
 */
class HomeViewModel(
    private val application: Application,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /**
     * Stateflow of [HomeUiState].
     */
    var uiState = MutableStateFlow(HomeUiState())

    init {
        observeSdkStart()
        viewModelScope.launch(Dispatchers.Default) {
            getSettings()
            setPurchaselyAttributes()
            withContext(Dispatchers.Main) {
                uiState.value = uiState.value.copy(sdkVersion = Purchasely.sdkVersion)
            }
        }

    }

    /**
     * Restore previous purchases.
     */
    fun restore() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isRestoring = true)
            delay(1000)
            Purchasely.restoreAllProducts(
                onSuccess = {
                    Log.d(TAG, "Restored Plan is $it")
                    uiState.value = uiState.value.copy(isRestoring = false)
                },
                onError = {
                    Log.e(TAG, "Restoring error ${it?.message}", it)
                    uiState.value = uiState.value.copy(isRestoring = false)
                }
            )
        }
    }

    /**
     * Trigger Purchasely.synchronize() for manual testing of the v6 callback API.
     * Useful in observer mode to verify that onSuccess / onError fire correctly
     * after the SDK POSTs any pending receipts to the backend.
     */
    fun synchronize() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isSynchronizing = true)
            Purchasely.synchronize(
                onSuccess = { plan ->
                    Log.d(TAG, "Synchronize success — validated plan: ${plan?.vendorId ?: "<none>"}")
                    uiState.value = uiState.value.copy(isSynchronizing = false)
                },
                onError = { error ->
                    Log.e(TAG, "Synchronize error: ${error?.message ?: "<pending>"}", error)
                    uiState.value = uiState.value.copy(isSynchronizing = false)
                }
            )
        }
    }

    /**
     * Save [text] to clipboard.
     */
    fun saveToClipboard(context: Context, label: String, text: String) {
        viewModelScope.launch {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboardManager.setPrimaryClip(clip)
        }
    }

    /**
     * Set user attributes.
     */
    private fun setPurchaselyAttributes() {
        Purchasely.setAttribute(Attribute.CUSTOMERIO_USER_EMAIL, "user@example.com")
        Purchasely.setAttribute(Attribute.MPARTICLE_USER_ID, uiState.value.userId)
        Purchasely.setAttribute(Attribute.CUSTOMERIO_USER_ID, uiState.value.userId)
        Purchasely.setAttribute(Attribute.BRANCH_USER_DEVELOPER_IDENTITY, uiState.value.userId)
        Purchasely.setAttribute(Attribute.BATCH_CUSTOM_USER_ID, "demo_custom_user_id")
    }

    /**
     * Keeps the header status dot in sync. `start()` completes asynchronously and usually after the
     * home screen first composes, so reading it once would leave the dot stuck on pending.
     */
    private fun observeSdkStart() {
        val app = application as? SampleV2Application ?: return
        viewModelScope.launch {
            app.sdkStatus.collect { status ->
                uiState.value = uiState.value.copy(
                    sdkStatus = status,
                    // The anonymous id is only assigned once storage is up.
                    anonymousUserId = Purchasely.anonymousUserId,
                    isAnonymous = Purchasely.isAnonymous(),
                )
            }
        }
    }

    /**
     * Re-read the properties shown on the home screen. Called when the screen is entered so
     * settings changed on [com.purchasely.samplev2.presentation.screen.settings.SettingsScreen]
     * are reflected instead of the values read when the ViewModel was created.
     */
    fun refreshProperties() = getSettings()

    /**
     * Retrieve current settings (preferences) and live SDK state.
     */
    private fun getSettings() {
        uiState.value = uiState.value.copy(
            presentationId = preferencesRepository.getString(PRESENTATION_ID) ?: "",
            placementId = preferencesRepository.getString(PLACEMENT_ID) ?: "",
            contentId = preferencesRepository.getString(CONTENT_ID) ?: "",
            userId = preferencesRepository.getString(USER_ID) ?: "",
            planId = preferencesRepository.getString(PLAN_ID) ?: "",
            // Live SDK state, not preferences: these reflect the running instance.
            anonymousUserId = Purchasely.anonymousUserId,
            isAnonymous = Purchasely.isAnonymous(),
            themeMode = Purchasely.getThemeMode().name,
            allowDeeplink = Purchasely.allowDeeplink,
            // Applied at start(), so these describe the SDK as currently configured.
            isObserverMode = !preferencesRepository.getBoolean(IS_FULL_MODE),
            allowCampaigns = preferencesRepository.getBoolean(ALLOW_CAMPAIGNS, default = true),
            displayMethod = preferencesRepository.getInt(DISPLAY_METHOD).let { DisplayMethod.entries[it] },
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
            transitionWidthValue = preferencesRepository.getString(TRANSITION_WIDTH_VALUE) ?: ""
        )
        syncInlineScreen()
    }

    /**
     * Fetches the inline screen the first time it is wanted and drops it as soon as the setting is
     * turned off, so toggling in Settings takes effect on the next visit to Home.
     */
    private fun syncInlineScreen() {
        val wanted = preferencesRepository.getBoolean(SHOW_INLINE_SCREEN, default = false)
        when {
            !wanted -> uiState.value = uiState.value.copy(inlineScreen = null)
            uiState.value.inlineScreen == null -> prefetchInlineScreen()
        }
    }


    fun onDisplayScreenClick(context: Context, navigateToPaywall: () -> Unit) {
        val displayMethod = uiState.value.displayMethod ?: return
        when(displayMethod) {
            DisplayMethod.FETCH_DISPLAY,
            DisplayMethod.FETCH_FRAGMENT -> {
                display(context, displayMethod)
            }
            DisplayMethod.DISPLAY -> {
                displayAsync(context)
            }
            else -> {
                navigateToPaywall()
            }
        }
    }

    fun display(context: Context, displayMethod: DisplayMethod) {
        val prepared = PLYPresentation {
            backgroundColor("#FF0000".toColorInt())
            progressColor(android.graphics.Color.WHITE)
            preferencesRepository.getString(PLACEMENT_ID).takeUnless { it?.isBlank() == true }
                ?.let { placementId(it) }
            preferencesRepository.getString(PRESENTATION_ID).takeUnless { it?.isBlank() == true }
                ?.let { screenId(it) }
            preferencesRepository.getString(CONTENT_ID).takeUnless { it?.isBlank() == true }
                ?.let { contentId(it) }
        }

        viewModelScope.launch {
            val presentation = try {
                prepared.preload()
            } catch (e: Throwable) {
                Toast.makeText(context, "Error fetching presentation", Toast.LENGTH_SHORT).show()
                return@launch
            }

            when(displayMethod) {
                DisplayMethod.FETCH_DISPLAY -> {
                    presentation.display(context) { outcome ->
                        Log.d("Demo", "result: ${outcome.purchaseResult}, plan: ${outcome.plan}")
                        Toast.makeText(context, "${outcome.purchaseResult} ${outcome.plan?.name ?: ""}", Toast.LENGTH_SHORT).show()
                    }
                }
                DisplayMethod.FETCH_FRAGMENT -> {
                    val fragment = presentation.getFragment()
                    val fragmentManager = (context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager
                    fragmentManager?.beginTransaction()?.add(android.R.id.content, fragment, "flow")
                        ?.addToBackStack(null)?.commitAllowingStateLoss()
                        ?: run {
                            Toast.makeText(context, "Fragment manager is null", Toast.LENGTH_SHORT).show()
                        }
                }
                else -> {
                    // no-op
                }
            }
        }
    }

    /**
     * Builds a [PLYTransitionDimension] from the settings unit/value pair, or
     * null when the value is blank/non-positive so the surface keeps its default
     * sizing. Percentage values are entered as whole numbers (e.g. 60 -> 0.6).
     */
    private fun buildDimension(type: PLYDimensionType, value: String): PLYTransitionDimension? {
        val number = value.toFloatOrNull()?.takeIf { it > 0f } ?: return null
        return when (type) {
            PLYDimensionType.PERCENTAGE -> PLYTransitionDimension(PLYDimensionType.PERCENTAGE, (number / 100f).coerceAtMost(1f))
            PLYDimensionType.PIXEL -> PLYTransitionDimension(PLYDimensionType.PIXEL, number)
        }
    }

    private fun displayAsync(context: Context) {
        val presentation = PLYPresentation {
            backgroundColor("#FF0000".toColorInt())
            progressColor(android.graphics.Color.WHITE)
            preferencesRepository.getString(PLACEMENT_ID).takeUnless { it?.isBlank() == true }
                ?.let { placementId(it) }
            preferencesRepository.getString(PRESENTATION_ID).takeUnless { it?.isBlank() == true }
                ?.let { screenId(it) }
            contentId(preferencesRepository.getString(CONTENT_ID).takeUnless { it?.isBlank() == true })
        }

        viewModelScope.launch {
            try {
                presentation.display(context, transition = PLYTransition(
                    type = uiState.value.transitionType,
                    height = buildDimension(uiState.value.transitionHeightType, uiState.value.transitionHeightValue),
                    width = buildDimension(uiState.value.transitionWidthType, uiState.value.transitionWidthValue)
                ))
                Log.d(TAG, "Presentation displayed: $presentation")
            } catch (e: CancellationException) {
                Log.d(TAG, "Presentation cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error displaying presentation: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error displaying presentation", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun prefetchInlineScreen() {
        PLYPresentation { placementId("DO_NOT_TOUCH") }.preload { presentation, error ->
            if (error != null) {
                Log.e(TAG, "[HomeViewModel] Error prefetching inline presentation: $error")
                return@preload
            }

            // The setting may have been turned off while the fetch was in flight.
            if (!preferencesRepository.getBoolean(SHOW_INLINE_SCREEN, default = false)) return@preload

            uiState.value = uiState.value.copy(inlineScreen = presentation)
            Log.d(TAG, "[HomeViewModel] Prefetched inline presentation: $presentation")
        }
    }

    fun setPaywallInterceptors(navController: NavController) {
        Purchasely.interceptAction<PLYPresentationAction.Navigate> { info, navigate ->
            Log.e(TAG, "Intercepted NAVIGATE: $navigate, info: $info")
            Log.e(TAG, "Navigate to ${navigate.url}")
            PLYInterceptResult.NOT_HANDLED
        }

        Purchasely.interceptAction<PLYPresentationAction.Login> { info, _ ->
            Log.e(TAG, "Intercepted LOGIN, info: $info")
            navController.navigateUp()
            navController.navigate(Screen.Settings.route)
            PLYInterceptResult.SUCCESS
        }

        Purchasely.interceptAction<PLYPresentationAction.CloseAll> { info, _ ->
            Log.e(TAG, "Intercepted CLOSE_ALL, info: $info")
            PLYInterceptResult.NOT_HANDLED
        }

        Purchasely.interceptAction<PLYPresentationAction.Purchase> { info, purchase ->
            Log.e(TAG, "Intercepted PURCHASE: $purchase, info: $info")
            val isFullMode = preferencesRepository.getBoolean(IS_FULL_MODE)
            if (isFullMode) {
                PLYInterceptResult.NOT_HANDLED
            } else {
                val activity = info.activity ?: return@interceptAction PLYInterceptResult.NOT_HANDLED
                // Closes the paywall when the purchase flow concludes — on the success dialog
                // button, or right away for a pending purchase. Access is gated on the real
                // subscription status, which Purchasely.synchronize() refreshes once payment clears.
                val closePaywall = {
                    Purchasely.closeAllScreens()
                    if (navController.previousBackStackEntry != null) {
                        navController.navigateUp()
                    }
                }
                // purchase() suspends until the purchase is processed, so the paywall button
                // keeps its loading spinner during the BillingClient flow (like Full mode). It
                // returns SUCCESS once the success dialog shows, or FAILED on error/cancel.
                purchase(
                    activity = activity,
                    action = purchase,
                    onCloseRequested = closePaywall
                )
            }
        }
    }
}

