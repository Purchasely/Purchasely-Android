package com.purchasely.samplev2

import android.app.Activity
import android.app.Application
import android.util.Log
import android.view.View
import com.purchasely.samplev2.presentation.common.SdkStatus
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.ALLOW_CAMPAIGNS
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.ALLOW_DEEPLINK
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.API_KEY
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.DEBUG_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_FULL_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.LANGUAGE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.THEME
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import com.purchasely.samplev2.di.appModule
import com.purchasely.samplev2.domain.log.LogMessage
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import com.purchasely.samplev2.presentation.util.formatDate
import com.purchasely.samplev2.presentation.util.info
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import com.purchasely.samplev2.presentation.util.getAppCustomScreen
import io.purchasely.views.presentation.PLYThemeMode
import io.purchasely.ext.EventListener
import io.purchasely.ext.LogLevel
import io.purchasely.ext.PLYAlertMessage
import io.purchasely.ext.PLYCustomScreen
import io.purchasely.ext.PLYCustomScreenProvider
import io.purchasely.ext.PLYEvent
import io.purchasely.ext.PLYLogging
import io.purchasely.ext.PLYMessage
import io.purchasely.ext.presentation.PLYPresentation
import io.purchasely.ext.presentation.display
import io.purchasely.ext.PLYRunningMode
import io.purchasely.ext.PLYUIHandler
import io.purchasely.storage.userData.PLYUserAttributeSource
import io.purchasely.storage.userData.PLYUserAttributeType
import io.purchasely.ext.UserAttributeListener
import io.purchasely.models.PLYWebRedemptionResult
import io.purchasely.ext.Purchasely
import io.purchasely.google.GoogleStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext.startKoin

class SampleV2Application : Application(), KoinComponent {

    val logs: MutableList<LogMessage> = CopyOnWriteArrayList()

    private val _sdkStatus = MutableStateFlow(SdkStatus.STARTING)

    /**
     * How `Purchasely.start()` is progressing. It completes asynchronously, so screens reporting
     * SDK state must observe this rather than reading [Purchasely.isStarted] once.
     */
    val sdkStatus: StateFlow<SdkStatus> = _sdkStatus.asStateFlow()

    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate() {
        /*StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )*/
        super.onCreate()

        startKoin {
            androidContext(this@SampleV2Application)
            modules(appModule)
        }

        preferencesRepository = get()

        val demoApiKey = "fcb39be4-2ba4-4db7-bde3-2a5a1e20745d"
        val apiKey = preferencesRepository.getString(API_KEY) ?: let {
            preferencesRepository.setString(API_KEY, demoApiKey)
            demoApiKey
        }

        val userId = preferencesRepository.getString(USER_ID)
        // The stored choice is what start() restores, so only seed it from the SDK the first time.
        if (preferencesRepository.getString(THEME).isNullOrBlank()) {
            preferencesRepository.setString(THEME, Purchasely.getThemeMode().name)
        }

        startPurchasely(apiKey, userId)
        setCustomScreensProvider()
        Purchasely.debugMode = preferencesRepository.getBoolean(DEBUG_MODE, default = true)
        preferencesRepository.getString(LANGUAGE)?.takeIf { it.isNotBlank() }?.let { Purchasely.language = Locale(it) }

        Purchasely.addLogger(object : PLYLogging {
            override fun messageLogged(message: PLYMessage) {
                val log = LogMessage(message.message, message.logLevel.name, message.date.formatDate())
                logs.add(log)

            }
        })

        Purchasely.uiHandler = object : PLYUIHandler {
            override fun onPresentation(presentation: PLYPresentation, proceed: () -> Unit) {
                super.onPresentation(presentation, proceed)
                Log.d(TAG, "onPresentation: screenId=${presentation.screenId}, placementId=${presentation.placementId}")
                logs.add(LogMessage("Presentation ${presentation.info().joinToString { (k, v) -> "$k=$v" }}", "PRESENTATION", Date().formatDate()))
            }

            override fun onAlert(alert: PLYAlertMessage, purchaselyView: View, activity: Activity?, proceed: () -> Unit) {
                super.onAlert(alert, purchaselyView, activity, proceed)
                Log.d(TAG, "onAlert: alertId=${alert.getContentMessage()}")
            }
        }

        Purchasely.eventListener = object : EventListener {
            override fun onEvent(event: PLYEvent) {
                logs.add(LogMessage("Event ${event.name}: ${event.properties}", "EVENT", Date().formatDate()))
            }
        }

        Purchasely.userAttributeListener = object : UserAttributeListener {
            override fun onUserAttributeSet(key: String, type: PLYUserAttributeType, value: Any, source: PLYUserAttributeSource) {
                logs.add(LogMessage("User attribute set $key=$value ($type, $source)", "ATTRIBUTE", Date().formatDate()))
            }

            override fun onUserAttributeRemoved(key: String, source: PLYUserAttributeSource) {
                logs.add(LogMessage("User attribute removed $key ($source)", "ATTRIBUTE", Date().formatDate()))
            }
        }
    }

    /** Start purchasely instance with the Kotlin DSL. */
    fun startPurchasely(apiKey: String, userId: String?) {
        // Settings can restart the SDK, so go back to pending until the callback reports the result.
        _sdkStatus.value = SdkStatus.STARTING
        val runningMode = if (preferencesRepository.getBoolean(IS_FULL_MODE)) PLYRunningMode.Full else PLYRunningMode.Observer
        val allowDeeplink = preferencesRepository.getBoolean(ALLOW_DEEPLINK, default = true)
        val allowCampaigns = preferencesRepository.getBoolean(ALLOW_CAMPAIGNS, default = true)
        // Without this the SDK falls back to SYSTEM on every start and the saved choice is lost.
        val themeMode = preferencesRepository.getString(THEME)
            ?.let { runCatching { PLYThemeMode.valueOf(it) }.getOrNull() }
            ?: PLYThemeMode.SYSTEM

        Purchasely {
            context(applicationContext)
            apiKey(apiKey)
            userId(userId?.takeIf { it.isNotBlank() })
            logLevel(LogLevel.DEBUG)
            allowDeeplink(allowDeeplink)
            allowCampaigns(allowCampaigns)
            runningMode(runningMode)
            stores(listOf(GoogleStore()))
            themeMode(themeMode)
            // The app owns the outcome UI here: no SDK alert, and the success placement is
            // displayed instead, as soon as the redemption settles. Drop the flag to get the SDK's
            // own alert back, with this callback running when the user dismisses it.
            webRedemptionListener(appHandlesRedemptionAlert = true) { result ->
                when (result) {
                    is PLYWebRedemptionResult.Success -> {
                        logs.add(LogMessage("Web redemption succeeded (replay=${result.replay})", "REDEMPTION", Date().formatDate()))
                        // Everything the SDK grants must be reachable from here alone, with no
                        // second lookup: the subscription arrives with its plan and its product.
                        val subscription = result.context?.subscription
                        Log.d(
                            TAG,
                            "Web redemption succeeded: replay=${result.replay}, " +
                                "context=${if (result.context != null) "present" else "null"}, " +
                                "subscription=${subscription?.data?.id ?: "null"}, " +
                                "status=${subscription?.data?.subscriptionStatus}, " +
                                "store=${subscription?.data?.storeType}, " +
                                "nextRenewalAt=${subscription?.data?.nextRenewalAt}, " +
                                "plan=${subscription?.plan?.vendorId}, " +
                                "product=${subscription?.product?.vendorId}"
                        )
                        PLYPresentation { placementId(REDEMPTION_COMPLETED_PLACEMENT) }
                            .display(applicationContext) { outcome ->
                                Log.d(TAG, "Redemption placement dismissed: ${outcome.purchaseResult}")
                            }
                    }

                    is PLYWebRedemptionResult.Failure -> {
                        Log.e(TAG, "Web redemption failed: code=${result.errorCode}, message=${result.errorMessage}")
                        logs.add(LogMessage("Web redemption failed: ${result.errorCode} ${result.errorMessage}", "REDEMPTION", Date().formatDate()))
                    }
                }
            }
            onInitialized { error ->
                _sdkStatus.value = if (error == null) SdkStatus.STARTED else SdkStatus.ERROR
                if (error != null) Log.e(TAG, "Purchasely configuration error", error)
                else Log.d(TAG, "Purchasely configured successfully")
            }
        }
    }

    private fun setCustomScreensProvider()  {
        Purchasely.setCustomScreenProvider(
            object : PLYCustomScreenProvider {
                override fun onCustomScreenRequested(presentation: PLYPresentation): PLYCustomScreen {

                    return getAppCustomScreen(
                        context = this@SampleV2Application,
                        useFragment = presentation.screenId?.contains("fragment" ) ?: false,
                        vendorId = presentation.screenId,
                        connections = presentation.connections,
                        backgroundColor = presentation.backgroundColor,
                        onExecuteConnection = { connection -> presentation.execute(connection) },
                        onPrevious = { presentation.back() }
                    )
                }
            }
        )
    }

    private companion object {
        /** Placement shown once a Web2App redemption has granted the subscription. */
        const val REDEMPTION_COMPLETED_PLACEMENT = "web_redemption_completed"
    }
}
