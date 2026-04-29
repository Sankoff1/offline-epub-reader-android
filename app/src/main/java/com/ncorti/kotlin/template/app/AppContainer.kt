package com.ncorti.kotlin.template.app

import android.content.Context
import com.chitalka.library.LibrarySessionState
import com.chitalka.prefs.SharedPreferencesKeyValueStore
import com.chitalka.storage.StorageService

/**
 * Контейнер процесс-уровневых зависимостей. Создаётся один раз в [ChitalkaApplication.onCreate]
 * и переживает recreate Activity (смена темы/локали системой), благодаря чему
 * [LibrarySessionState] (epoch, welcome-флаги) не сбрасывается на каждой пересборке UI.
 *
 * Все хранилища держат `applicationContext`, а не Activity — иначе SQLite-helper и SharedPrefs
 * утекли бы вместе со старой Activity при смене конфигурации.
 */
class AppContainer(applicationContext: Context) {
    val storage: StorageService = StorageService(applicationContext)
    val persistence: SharedPreferencesKeyValueStore = SharedPreferencesKeyValueStore(applicationContext)
    val librarySession: LibrarySessionState = LibrarySessionState()
}
