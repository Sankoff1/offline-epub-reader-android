@file:Suppress("LongParameterList")

package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.ui.bookactions.BookActionsSheetSpec
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

@Composable
internal fun BookActionsContent(
    book: LibraryBookWithProgress,
    onToggleFavorite: () -> Unit,
    onMoveToTrash: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            book.record.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            book.record.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        ActionRow(
            icon = if (book.record.isFavorite) Icons.Filled.FavoriteBorder else Icons.Filled.Favorite,
            label = chitalkaString(BookActionsSheetSpec.favoriteActionKey(book.record.isFavorite)),
            onClick = onToggleFavorite,
        )
        ActionRow(
            icon = Icons.Filled.Delete,
            label = chitalkaString(BookActionsSheetSpec.I18nKeys.MOVE_TO_TRASH),
            tint = MaterialTheme.colorScheme.error,
            onClick = onMoveToTrash,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(chitalkaString(BookActionsSheetSpec.I18nKeys.COMMON_CANCEL))
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}
