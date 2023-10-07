package com.purchasely.samplev2

import android.app.Application
import android.util.Log
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.API_KEY
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.API_URL
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_OBSERVER_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_PRODUCTION_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PAYWALL_URL
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import dagger.hilt.android.HiltAndroidApp
import io.purchasely.ext.LogLevel
import io.purchasely.ext.PLYAPIEnvironment
import io.purchasely.ext.PLYRunningMode
import io.purchasely.ext.Purchasely
import io.purchasely.google.GoogleStore
import io.purchasely.switchEnvironment
import javax.inject.Inject

@HiltAndroidApp
class SampleV2Application : Application() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate() {
        super.onCreate()

        val stagingKey = "fcb39be4-2ba4-4db7-bde3-2a5a1e20745d"
        val apiKey = preferencesRepository.getString(API_KEY) ?: let {
            preferencesRepository.setString(API_KEY, stagingKey)
            stagingKey
        }

        val userId = preferencesRepository.getString(USER_ID)
        val apiUrl = preferencesRepository.getString(API_URL)
        val paywallUrl = preferencesRepository.getString(PAYWALL_URL)
        val isProductionMode = preferencesRepository.getBoolean(IS_PRODUCTION_MODE)

        setEnvironment(isProductionMode, apiUrl, paywallUrl)
        startPurchasely(apiKey, userId)
    }

    /**
     * Start purchasely instance.
     */
    fun startPurchasely(apiKey: String, userId: String?) {
        Purchasely.Builder(applicationContext)
            .apiKey(apiKey)
            .userId(userId)
            .logLevel(LogLevel.DEBUG)
            .readyToOpenDeeplink(true)
            .runningMode(if(preferencesRepository.getBoolean(IS_OBSERVER_MODE)) PLYRunningMode.PaywallObserver else PLYRunningMode.Full)
            .stores(listOf(GoogleStore()))
            .build()
            .start { isConfigured, error ->
                if (isConfigured) {
                    Log.d(TAG, "Purchasely configured successfully")
                }
                if (error != null) {
                    Log.e(TAG, "Purchasely configuration error", error)
                }
            }
    }

    /**
     * Set purchasely environment.
     */
    fun setEnvironment(isProduction: Boolean, apiUrl: String?, paywallUrl: String?) {
        Purchasely.switchEnvironment(
            when {
                isProduction -> PLYAPIEnvironment.Production
                apiUrl.isNullOrBlank().not() || paywallUrl.isNullOrBlank().not() -> PLYAPIEnvironment.Custom(apiUrl, null, paywallUrl)
                else -> PLYAPIEnvironment.Staging
            }
        )
    }
}