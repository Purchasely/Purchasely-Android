package com.purchasely.samplev2.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            "com.purchasely.samplev2.preferences",
            Context.MODE_PRIVATE
        )
    }

    override fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    override fun addToHistory(key: String, input: String) {
        if(input.isNotBlank()) {
            val currentHistory = getHistory(key = key).toMutableList()
            if (currentHistory.contains(input)) {
                currentHistory.remove(input)
            }
            currentHistory.add(0, input)
            if (currentHistory.size > HISTORY_SIZE_MAX) {
                currentHistory.removeLast()
            }
            sharedPreferences.edit().putStringSet(key, currentHistory.toSet()).apply()
        }
    }

    override fun getHistory(key: String): List<String> {
        return sharedPreferences.getStringSet(key, setOf())?.toList() ?: emptyList()
    }

    companion object {
        /**
         * Preferences keys.
         */
        const val USER_ID = "userID"
        const val PLAN_ID = "planId"
        const val PRODUCT_ID = "productId"
        const val PLACEMENT_ID = "placementId"
        const val PRESENTATION_ID = "presentationId"
        const val CONTENT_ID = "contentId"
        const val IS_PRODUCTION_MODE = "productionMode"
        const val IS_OBSERVER_MODE = "observerMode"
        const val IS_ASYNC_LOADING = "asyncLoading"
        const val PAYWALL_URL = "paywallUrl"
        const val API_URL = "apiUrl"
        const val API_KEY = "apiKey"
        const val TEMPLATE = "template"
        const val STORE = "store"
        const val HISTORY_PRESENTATION = "presentationHistory"
        const val HISTORY_PLACEMENT = "placementHistory"

        /**
         * history max size.
         */
        const val HISTORY_SIZE_MAX = 5
    }
}