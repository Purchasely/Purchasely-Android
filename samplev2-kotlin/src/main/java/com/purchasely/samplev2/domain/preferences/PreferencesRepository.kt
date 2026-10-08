package com.purchasely.samplev2.domain.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    /**
     * Save in shared preferences [value] with [key] as a key.
     */
    fun setString(key: String, value: String)

    /**
     * Save in shared preferences [value] with [key] as a key.
     */
    fun setInt(key: String, value: Int)

    /**
     * Save in shared preferences [value] with [key] as a key.
     */
    fun setBoolean(key: String, value: Boolean)

    /**
     * Retrieve the [String] associated with [key].
     */
    fun getString(key: String): String?

    /**
     * Retrieve the [Int] associated with [key].
     */
    fun getInt(key: String): Int

    /**
     * Retrieve the [Boolean] associated with [key].
     */
    fun getBoolean(key: String): Boolean

    /**
     * Retrieve the [Boolean] associated with [key], returning [default] when absent.
     */
    fun getBoolean(key: String, default: Boolean): Boolean

    /**
     * add the [input] to the history having [key] as a key.
     */
    fun addToHistory(key: String, input: String)

    /**
     * Retrieve the [String] history associated with [key].
     */
    fun getHistory(key: String): List<String>

    fun removeKey(key: String): Boolean

    /**
     * Emits once on collection and again every time [key] is written, so callers can react to a
     * setting changing without polling.
     */
    fun observeKey(key: String): Flow<Unit>
}