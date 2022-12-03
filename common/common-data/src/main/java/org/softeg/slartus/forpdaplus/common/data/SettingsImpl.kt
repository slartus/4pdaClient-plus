package org.softeg.slartus.forpdaplus.common.data

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.softeg.slartus.common.api.Settings
import javax.inject.Inject

class SettingsImpl @Inject constructor(private val preferences: SharedPreferences) : Settings {
    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        preferences.edit().remove(key).apply()
    }

    override suspend fun hasKey(key: String): Boolean = withContext(Dispatchers.IO) {
        preferences.contains(key)
    }

    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        preferences.edit().putString(key, value).apply()
    }

    override suspend fun getString(key: String, defaultValue: String?): String? =
        withContext(Dispatchers.IO) {
            preferences.getString(key, defaultValue)
        }
}