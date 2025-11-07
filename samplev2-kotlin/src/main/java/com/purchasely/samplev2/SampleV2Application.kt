package com.purchasely.samplev2

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.widget.Toast
import com.purchasely.samplev2.data.di.appModule
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.API_KEY
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_OBSERVER_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import io.purchasely.ext.LogLevel
import io.purchasely.ext.PLYRunningMode
import io.purchasely.ext.Purchasely
import io.purchasely.google.GoogleStore
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class SampleV2Application : Application() {

    lateinit var preferencesRepository: PreferencesRepository

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SampleV2Application)
            modules(appModule)
        }

        preferencesRepository = get()


        val stagingKey = "fcb39be4-2ba4-4db7-bde3-2a5a1e20745d"
        val apiKey = preferencesRepository.getString(API_KEY) ?: let {
            preferencesRepository.setString(API_KEY, stagingKey)
            stagingKey
        }

        val userId = preferencesRepository.getString(USER_ID)
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
                    Log.e(TAG, "Purchasely configuration error: $error")
                    Toast.makeText(applicationContext, "Failed to start Purchasely SDK.", Toast.LENGTH_LONG).show()
                }
            }
    }
}