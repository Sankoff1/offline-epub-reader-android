@file:Suppress("LongParameterList", "LongMethod")

package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.webViewBaseUrl
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

private val ERROR_CONTENT_PADDING = 24.dp
private val ERROR_TEXT_TOP_GAP = 12.dp
private val ERROR_BUTTON_TOP_GAP = 20.dp
private val LOADING_TEXT_TOP_GAP = 12.dp
private val READER_TITLE_FONT_SIZE = 17.sp
private const val EMPTY_TITLE_PLACEHOLDER = "…"
private val PAGE_INDICATOR_CHIP_HORIZONTAL_PADDING = 14.dp
private val PAGE_INDICATOR_CHIP_VERTICAL_PADDING = 4.dp
private const val PAGE_INDICATOR_CHIP_CORNER_PERCENT = 50

@Composable
internal fun ReaderErrorContent(
    errorText: String,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(ERROR_CONTENT_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = chitalkaString(ReaderScreenSpec.I18nKeys.ERROR_TITLE),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = errorText,
            modifier = Modifier.padding(top = ERROR_TEXT_TOP_GAP),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onBackToLibrary,
            modifier = Modifier.padding(top = ERROR_BUTTON_TOP_GAP),
        ) {
            Text(chitalkaString(ReaderScreenSpec.I18nKeys.BACK_TO_BOOKS))
        }
    }
}

@Composable
internal fun ReaderLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                chitalkaString(ReaderScreenSpec.I18nKeys.LOADING),
                modifier = Modifier.padding(top = LOADING_TEXT_TOP_GAP),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderReadyContent(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    distancePx: Int,
    readerFrameColor: Color,
    readerPaperColor: Color,
    themeMode: ThemeMode,
    themeColors: ThemeColors,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // `Animatable` живёт в Composable — он не переживает recomposition без remember
    // и не сериализуется. VM публикует `TransitionCommand`, мы реагируем на него через LaunchedEffect.
    val transitionProgress = remember { Animatable(0f) }

    LaunchedEffect(state.transitionCommand?.revision) {
        if (state.transitionCommand == null) return@LaunchedEffect
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = ReaderScreenSpec.Timing.CHAPTER_TRANSITION_DURATION_MS.toInt(),
                    easing = FastOutSlowInEasing,
                ),
        )
        transitionProgress.snapTo(0f)
        viewModel.onTransitionAnimationFinished()
    }

    val tid = state.transitionTargetLayerId
    val activeLayer = state.activeLayer()
    val isTransitioning =
        tid != null &&
            activeLayer != null &&
            (if (tid == ReaderScreenSpec.ReaderLayerId.A) state.layerA else state.layerB) != null
    val animProgress = transitionProgress.value

    val baseUrl = ReaderScreenSpec.webViewBaseUrl(state.unpackedRoot)
    val titleText =
        state.bookRecord?.title?.trim()?.takeIf { it.isNotEmpty() }.orEmpty()
    val currentChapterIndex = activeLayer?.chapterIndex
    var tocOpen by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText.ifEmpty { EMPTY_TITLE_PLACEHOLDER },
                        fontSize = READER_TITLE_FONT_SIZE,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToLibrary) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                chitalkaString(ReaderScreenSpec.I18nKeys.BACK_TO_LIBRARY),
                        )
                    }
                },
                actions = {
                    if (state.spineLength > 0) {
                        IconButton(onClick = { tocOpen = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = chitalkaString(ReaderScreenSpec.I18nKeys.TOC),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (currentChapterIndex != null && state.spineLength > 0) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(readerFrameColor)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(
                                top = ReaderScreenSpec.Layout.PAGE_INDICATOR_PADDING_TOP_DP.dp,
                                bottom = ReaderScreenSpec.Layout.PAGE_INDICATOR_PADDING_BOTTOM_MIN_DP.dp,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        color = readerPaperColor,
                        shape = RoundedCornerShape(percent = PAGE_INDICATOR_CHIP_CORNER_PERCENT),
                    ) {
                        Text(
                            text =
                                ReaderScreenSpec.pageIndicatorSlash(
                                    zeroBasedChapterIndex = currentChapterIndex,
                                    spineLength = state.spineLength,
                                ),
                            fontSize = ReaderScreenSpec.Layout.PAGE_INDICATOR_TEXT_FONT_SP.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier =
                                Modifier.padding(
                                    horizontal = PAGE_INDICATOR_CHIP_HORIZONTAL_PADDING,
                                    vertical = PAGE_INDICATOR_CHIP_VERTICAL_PADDING,
                                ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        val contentMod = Modifier.padding(padding)
        if (state.unpackedRoot.isNotEmpty() && activeLayer != null) {
            Box(
                modifier =
                    contentMod
                        .fillMaxSize()
                        .background(readerFrameColor),
            ) {
                ReaderPageLayer(
                    layerId = ReaderScreenSpec.ReaderLayerId.A,
                    layer = state.layerA,
                    activeLayerId = state.activeLayerId,
                    transitionTargetLayerId = state.transitionTargetLayerId,
                    isTransitioning = isTransitioning,
                    transitionProgress = animProgress,
                    transitionDirection = state.transitionDirection,
                    distancePx = distancePx,
                    readerPaperColor = readerPaperColor,
                    baseUrl = baseUrl,
                    themeMode = themeMode,
                    themeColors = themeColors,
                    onBridge = { lid, msg -> viewModel.handleBridge(lid, msg) },
                    modifier = Modifier.fillMaxSize(),
                )
                ReaderPageLayer(
                    layerId = ReaderScreenSpec.ReaderLayerId.B,
                    layer = state.layerB,
                    activeLayerId = state.activeLayerId,
                    transitionTargetLayerId = state.transitionTargetLayerId,
                    isTransitioning = isTransitioning,
                    transitionProgress = animProgress,
                    transitionDirection = state.transitionDirection,
                    distancePx = distancePx,
                    readerPaperColor = readerPaperColor,
                    baseUrl = baseUrl,
                    themeMode = themeMode,
                    themeColors = themeColors,
                    onBridge = { lid, msg -> viewModel.handleBridge(lid, msg) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        if (tocOpen && currentChapterIndex != null) {
            ReaderTocSheet(
                spineLength = state.spineLength,
                currentChapterIndex = currentChapterIndex,
                onChapterSelected = { index ->
                    tocOpen = false
                    viewModel.goToChapter(index)
                },
                onDismiss = { tocOpen = false },
            )
        }
    }
}
