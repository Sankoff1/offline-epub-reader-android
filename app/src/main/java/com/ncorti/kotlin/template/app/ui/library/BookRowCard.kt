package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.ui.bookcard.BookCardSpec
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

private val COVER_CORNER_RADIUS = 8.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BookRowCard(
    book: LibraryBookWithProgress,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val coverW = BookCardSpec.Layout.COVER_WIDTH_DP.dp
    val coverH = BookCardSpec.Layout.coverHeightDp().dp
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            Modifier.padding(BookCardSpec.Layout.CARD_PADDING_DP.dp),
            horizontalArrangement = Arrangement.spacedBy(BookCardSpec.Layout.ROW_GAP_DP.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCover(
                coverUri = book.record.coverUri,
                coverW = coverW,
                coverH = coverH,
            )
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        book.record.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    if (book.record.isFavorite) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Text(
                    book.record.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReadingProgressBlock(book = book)
            }
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(BookCardSpec.Layout.MENU_BUTTON_SIZE_DP.dp),
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = chitalkaString(BookCardSpec.I18nKeys.A11Y_OPEN_MENU),
                )
            }
        }
    }
}

@Composable
private fun ReadingProgressBlock(book: LibraryBookWithProgress) {
    val raw = book.progressFraction ?: return
    val fraction = BookCardSpec.clampProgressFraction(raw).toFloat()
    val percent = BookCardSpec.progressPercentRounded(raw)
    val trackHeight = 8.dp
    val trackShape = RoundedCornerShape(percent = 50)
    Column(Modifier.padding(top = BookCardSpec.Layout.PROGRESS_ROW_MARGIN_TOP_DP.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(trackShape)
                .background(MaterialTheme.colorScheme.outlineVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(trackHeight)
                    .clip(trackShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(
            chitalkaString(BookCardSpec.I18nKeys.READ_PERCENT, percent),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = BookCardSpec.Layout.PROGRESS_ROW_GAP_DP.dp),
        )
    }
}

@Composable
private fun BookCover(
    coverUri: String?,
    coverW: Dp,
    coverH: Dp,
) {
    if (coverUri != null) {
        AsyncImage(
            model = coverUri,
            contentDescription = null,
            modifier = Modifier
                .width(coverW)
                .height(coverH)
                .clip(RoundedCornerShape(COVER_CORNER_RADIUS)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier
                .width(coverW)
                .height(coverH)
                .clip(RoundedCornerShape(COVER_CORNER_RADIUS))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
