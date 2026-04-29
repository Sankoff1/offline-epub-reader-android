@file:Suppress("LongMethod", "LongParameterList")

package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.library.LibraryImportSpec
import com.chitalka.screens.common.BookListScreenSpec
import com.chitalka.ui.bookcard.BookCardSpec
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChitalkaLibraryListPane(
    viewModel: LibraryViewModel,
    listRefreshNonce: Int,
    normalizedSearchQuery: String,
    showImportFab: Boolean,
    onImportClick: () -> Unit,
    onOpenBook: (LibraryBookWithProgress) -> Unit,
    emptyI18nKey: String = BookListScreenSpec.BOOKS_EMPTY_KEY,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(listRefreshNonce) {
        if (listRefreshNonce > 0) viewModel.onExternalRefresh()
    }
    LaunchedEffect(normalizedSearchQuery) {
        viewModel.onSearchQueryChange(normalizedSearchQuery)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeSheet = state.activeBookSheet
    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onBookActionsDismiss,
            sheetState = sheetState,
        ) {
            BookActionsContent(
                book = activeSheet.book,
                onToggleFavorite = { viewModel.onToggleFavorite(activeSheet.book) },
                onMoveToTrash = { viewModel.onMoveToTrash(activeSheet.book) },
                onDismiss = viewModel::onBookActionsDismiss,
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (state.filteredBooks.isEmpty()) {
            EmptyLibraryState(message = chitalkaString(emptyI18nKey))
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(BookCardSpec.Layout.CARD_MARGIN_BOTTOM_DP.dp),
            ) {
                items(state.filteredBooks, key = { it.record.bookId }) { book ->
                    BookRowCard(
                        book = book,
                        onClick = { onOpenBook(book) },
                        onLongClick = { viewModel.onBookActionsOpen(book) },
                        onMenuClick = { viewModel.onBookActionsOpen(book) },
                    )
                }
            }
        }
        if (showImportFab) {
            FloatingActionButton(
                onClick = onImportClick,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                shape = CircleShape,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = chitalkaString(LibraryImportSpec.I18nKeys.ADD_BOOK_A11Y),
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.size(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
