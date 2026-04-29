package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chitalka.screens.reader.ReaderScreenSpec
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

private val SHEET_TITLE_PADDING_HORIZONTAL = 24.dp
private val SHEET_TITLE_PADDING_VERTICAL = 12.dp
private val ITEM_PADDING_HORIZONTAL = 24.dp
private val ITEM_PADDING_VERTICAL = 14.dp
private val SHEET_LIST_MAX_HEIGHT = 480.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderTocSheet(
    spineLength: Int,
    currentChapterIndex: Int,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    LaunchedEffect(currentChapterIndex, spineLength) {
        if (spineLength > 0) {
            listState.scrollToItem(currentChapterIndex.coerceIn(0, spineLength - 1))
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = chitalkaString(ReaderScreenSpec.I18nKeys.TOC),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier =
                    Modifier.padding(
                        horizontal = SHEET_TITLE_PADDING_HORIZONTAL,
                        vertical = SHEET_TITLE_PADDING_VERTICAL,
                    ),
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.heightIn(max = SHEET_LIST_MAX_HEIGHT),
            ) {
                items((0 until spineLength).toList()) { index ->
                    TocRow(
                        label = chitalkaString(ReaderScreenSpec.I18nKeys.CHAPTER_LABEL, index + 1),
                        isCurrent = index == currentChapterIndex,
                        onClick = { onChapterSelected(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TocRow(label: String, isCurrent: Boolean, onClick: () -> Unit) {
    val rowModifier =
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .let {
                if (isCurrent) {
                    it.background(MaterialTheme.colorScheme.secondaryContainer)
                } else {
                    it
                }
            }
            .padding(
                horizontal = ITEM_PADDING_HORIZONTAL,
                vertical = ITEM_PADDING_VERTICAL,
            )
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        color =
            if (isCurrent) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        modifier = rowModifier,
    )
}
