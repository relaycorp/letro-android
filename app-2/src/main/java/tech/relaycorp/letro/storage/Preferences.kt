package tech.relaycorp.letro.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface Preferences {
    fun putString(key: String, value: String)
    fun putBoolean(key: String, value: Boolean)
    fun getString(key: String, defaultValue: String? = null): String?
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
}

class PreferencesImpl @Inject constructor(
    @ApplicationContext context: Context,
): Preferences {

    private val preferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun getString(key: String, defaultValue: String?): String? {
        return preferences.getString(key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    override fun putString(key: String, value: String) {
        preferences.edit()
            .putString(key, value)
            .apply()
    }

    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    private companion object {
        private const val PREF_NAME = "letro_preferences"
    }
}