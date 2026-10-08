package com.purchasely.samplev2.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PreferencesRepositoryImpl(
    private val context: Context
) : PreferencesRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            "com.purchasely.demo.samplev2.preferences",
            Context.MODE_PRIVATE
        )
    }

    override fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun setInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    override fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    override fun addToHistory(key: String, input: String) {
        if (input.isNotBlank()) {
            val currentHistory = getHistory(key = key).toMutableList()
            if (currentHistory.contains(input)) {
                currentHistory.remove(input)
            }
            currentHistory.add(0, input)
            if (currentHistory.size > HISTORY_SIZE_MAX) {
                currentHistory.removeAt(currentHistory.lastIndex)
            }
            sharedPreferences.edit().putString(key, currentHistory.joinToString(",")).apply()
        }
    }

    override fun getHistory(key: String): List<String> {
        val historyString = sharedPreferences.getString(key, "") ?: ""
        return historyString.split(",").filter { it.isNotBlank() }
    }
    override fun removeKey(key: String): Boolean {
        return sharedPreferences.edit().remove(key).commit()
    }

    override fun observeKey(key: String): Flow<Unit> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
            if (changed == key) trySend(Unit)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        send(Unit)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
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
        const val IS_FULL_MODE = "fullMode"
        const val ALLOW_DEEPLINK = "allowDeeplink"
        const val ALLOW_CAMPAIGNS = "allowCampaigns"
        const val SHOW_INLINE_SCREEN = "showInlineScreen"
        const val API_KEY = "apiKey"
        const val TEMPLATE = "template"
        const val STORE = "store"
        const val HISTORY_PRESENTATION = "presentationHistory"
        const val HISTORY_PLACEMENT = "placementHistory"
        const val THEME = "theme"
        const val LANGUAGE = "language"
        const val DEBUG_MODE = "debugMode"
        const val LOGS_SUBS = "logsSubs"
        const val DISPLAY_METHOD = "displayMethod"
        const val TRANSITION_TYPE = "transitionType"
        const val TRANSITION_HEIGHT_TYPE = "transitionHeightType"
        const val TRANSITION_HEIGHT_VALUE = "transitionHeightValue"
        const val TRANSITION_WIDTH_TYPE = "transitionWidthType"
        const val TRANSITION_WIDTH_VALUE = "transitionWidthValue"

        /**
         * history max size.
         */
        const val HISTORY_SIZE_MAX = 5
    }
}