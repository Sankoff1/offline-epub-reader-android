package com.ncorti.kotlin.template.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.chitalka.library.refreshBookCount
import com.chitalka.navigation.ReaderRouteLifecycle
import com.ncorti.kotlin.template.app.ui.reader.ChitalkaReaderScreen
import kotlinx.coroutines.launch

@Composable
internal fun ReaderRouteScreen(model: ReaderRouteUiModel) {
    val scope = rememberCoroutineScope()
    val locale = LocalChitalkaLocale.current
    val themeMode = LocalChitalkaThemeMode.current
    val themeColors = LocalChitalkaThemeColors.current

    suspend fun leaveReader() {
        ReaderRouteLifecycle.onReaderStackBeforeRemove(model.persistence)
        model.librarySession.refreshBookCount(model.storage)
        model.onPop()
    }

    LaunchedEffect(model.bookId) {
        if (model.bookId.isNotBlank()) {
            ReaderRouteLifecycle.onReaderEntered(model.persistence, model.bookId)
        }
    }

    BackHandler {
        scope.launch { leaveReader() }
    }

    ChitalkaReaderScreen(
        bookId = model.bookId,
        bookFileUri = model.bookPath,
        storage = model.storage,
        librarySession = model.librarySession,
        locale = locale,
        themeMode = themeMode,
        themeColors = themeColors,
        onBackToLibrary = { scope.launch { leaveReader() } },
        modifier = Modifier.fillMaxSize().testTag("reader_root"),
    )
}
