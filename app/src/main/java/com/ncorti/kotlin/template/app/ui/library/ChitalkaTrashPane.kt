package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.screens.trash.TrashScreenSpec
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

@Composable
fun ChitalkaTrashPane(
    viewModel: TrashViewModel,
    listRefreshNonce: Int,
    normalizedSearchQuery: String,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(listRefreshNonce) {
        if (listRefreshNonce > 0) viewModel.onExternalRefresh()
    }
    LaunchedEffect(normalizedSearchQuery) {
        viewModel.onSearchQueryChange(normalizedSearchQuery)
    }

    state.pendingPurge?.let {
        PurgeConfirmDialog(
            onConfirm = viewModel::onPurgeConfirm,
            onDismiss = viewModel::onPurgeCancel,
        )
    }

    if (state.filteredBooks.isEmpty()) {
        EmptyTrashState(
            message = chitalkaString(TrashScreenSpec.emptyListKey(state.hasActiveSearch)),
        )
    } else {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.filteredBooks, key = { it.record.bookId }) { book ->
                TrashRowCard(
                    book = book,
                    onRestore = { viewModel.onRestore(book) },
                    onDelete = { viewModel.onPurgeRequest(book) },
                )
            }
        }
    }
}

@Composable
private fun PurgeConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text(chitalkaString(TrashScreenSpec.I18nKeys.CONFIRM_DELETE_TITLE)) },
        text = { Text(chitalkaString(TrashScreenSpec.I18nKeys.CONFIRM_DELETE_MESSAGE)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    chitalkaString(TrashScreenSpec.I18nKeys.DELETE_FOREVER),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(chitalkaString(TrashScreenSpec.I18nKeys.COMMON_CANCEL))
            }
        },
    )
}

@Composable
private fun EmptyTrashState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun TrashRowCard(
    book: LibraryBookWithProgress,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    book.record.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    book.record.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${TrashScreenSpec.formatFileSizeMbNumber(book.record.fileSizeBytes)} " +
                        chitalkaString(TrashScreenSpec.I18nKeys.COMMON_MB),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = chitalkaString(TrashScreenSpec.I18nKeys.RESTORE),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = chitalkaString(TrashScreenSpec.I18nKeys.DELETE_FOREVER),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
