package com.purchasely.samplev2.domain.preferences

interface PreferencesRepository {

    /**
     * Save in shared preferences [value] with [key] as a key.
     */
    fun setString(key: String, value: String)

    /**
     * Save in shared preferences [value] with [key] as a key.
     */
    fun setBoolean(key: String, value: Boolean)

    /**
     * Retrieve the [String] associated with [key].
     */
    fun getString(key: String): String?

    /**
     * Retrieve the [Boolean] associated with [key].
     */
    fun getBoolean(key: String): Boolean

    /**
     * add the [input] to the history having [key] as a key.
     */
    fun addToHistory(key: String, input: String)

    /**
     * Retrieve the [String] history associated with [key].
     */
    fun getHistory(key: String): List<String>

    fun removeKey(key: String): Boolean
}