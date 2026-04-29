@file:Suppress("LongParameterList")

package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chitalka.epub.ensureFileUri
import com.chitalka.epub.fileUriToNativePath
import com.chitalka.i18n.AppLocale
import com.chitalka.library.LibrarySessionState
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.transitionDistancePx
import com.chitalka.storage.StorageService
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode

@Composable
fun ChitalkaReaderScreen(
    bookId: String,
    bookFileUri: String,
    storage: StorageService,
    librarySession: LibrarySessionState,
    locale: AppLocale,
    themeMode: ThemeMode,
    themeColors: ThemeColors,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val nativePath =
        remember(bookFileUri) {
            fileUriToNativePath(ensureFileUri(bookFileUri))
        }

    val viewModel: ReaderViewModel =
        viewModel(
            key = "reader-$bookId",
            factory = ReaderViewModel.Factory(bookId, storage, librarySession, locale, context.applicationContext),
        )

    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val distancePx =
        remember(configuration.screenWidthDp, density) {
            with(density) {
                val wPx = configuration.screenWidthDp.dp.roundToPx()
                ReaderScreenSpec.transitionDistancePx(wPx)
            }
        }

    val (readerFrameColor, readerPaperColor) =
        remember(themeMode, themeColors) {
            if (themeMode == ThemeMode.DARK) {
                parseThemeColor(themeColors.background) to parseThemeColor(themeColors.menuBackground)
            } else {
                parseThemeColor(ReaderScreenSpec.Colors.READER_FRAME_BACKGROUND_LIGHT_HEX) to
                    parseThemeColor(ReaderScreenSpec.Colors.READER_PAPER_BACKGROUND_LIGHT_HEX)
            }
        }

    LaunchedEffect(bookId) {
        viewModel.onBookRecordRefresh()
    }

    LaunchedEffect(bookId, nativePath) {
        viewModel.initialize(context, nativePath)
    }

    when (uiState.phase) {
        ReaderLoadPhase.Error ->
            ReaderErrorContent(
                errorText = uiState.errorText.orEmpty(),
                onBackToLibrary = onBackToLibrary,
                modifier = modifier.fillMaxSize(),
            )
        ReaderLoadPhase.Loading ->
            ReaderLoadingContent(modifier = modifier.fillMaxSize())
        ReaderLoadPhase.Ready ->
            ReaderReadyContent(
                state = uiState,
                viewModel = viewModel,
                distancePx = distancePx,
                readerFrameColor = readerFrameColor,
                readerPaperColor = readerPaperColor,
                themeMode = themeMode,
                themeColors = themeColors,
                onBackToLibrary = onBackToLibrary,
                modifier = modifier,
            )
    }
}
