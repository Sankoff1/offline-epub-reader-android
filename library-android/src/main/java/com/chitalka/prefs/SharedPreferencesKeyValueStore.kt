package com.chitalka.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.chitalka.library.LastOpenBookPersistence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "chitalka_kv"

/** [LastOpenBookPersistence] поверх [SharedPreferences] для ключей темы, локали и last-open. */
class SharedPreferencesKeyValueStore(
    context: Context,
    prefsName: String = PREFS_NAME,
) : LastOpenBookPersistence {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override suspend fun getItem(key: String): String? =
        withContext(Dispatchers.IO) {
            prefs.getString(key, null)
        }

    override suspend fun setItem(
        key: String,
        value: String,
    ) = withContext(Dispatchers.IO) {
        prefs.edit(commit = true) { putString(key, value) }
    }

    override suspend fun removeItem(key: String) =
        withContext(Dispatchers.IO) {
            prefs.edit(commit = true) { remove(key) }
        }
}
