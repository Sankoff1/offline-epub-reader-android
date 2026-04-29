@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.debug.DebugLogEntry
import com.chitalka.debug.debugLogFormatExport
import com.chitalka.i18n.AppLocale
import com.chitalka.screens.debuglogs.DebugLogsScreenSpec
import com.ncorti.kotlin.template.app.BuildConfig
import com.ncorti.kotlin.template.app.ui.LocalChitalkaLocale
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChitalkaDebugLogsPane() {
    val viewModel: DebugLogsViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locale = LocalChitalkaLocale.current
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            chitalkaString(DebugLogsScreenSpec.I18nKeys.TITLE),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            chitalkaString(DebugLogsScreenSpec.I18nKeys.SUBTITLE),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DebugActionButton(
                icon = Icons.Filled.Clear,
                label = chitalkaString(DebugLogsScreenSpec.I18nKeys.CLEAR),
                enabled = !state.exporting && !state.isEmpty,
                onClick = viewModel::onClear,
            )
            DebugActionButton(
                icon = Icons.Filled.Edit,
                label = chitalkaString(DebugLogsScreenSpec.I18nKeys.COPY),
                enabled = !state.exporting && !state.isEmpty,
                onClick = { copyLogsToClipboard(context, locale) },
            )
            DebugActionButton(
                icon = Icons.Filled.Share,
                label = if (state.exporting) "…" else chitalkaString(DebugLogsScreenSpec.I18nKeys.EXPORT),
                enabled = !state.exporting && !state.isEmpty,
                onClick = {
                    viewModel.onExportingChange(true)
                    scope.launch {
                        try {
                            exportLogsToShare(context, locale)
                        } catch (e: Exception) {
                            ChitalkaMirrorLog.e("DebugLogs", "export failed", e)
                        } finally {
                            viewModel.onExportingChange(false)
                        }
                    }
                },
            )
        }
        Spacer(Modifier.size(12.dp))
        if (state.isEmpty) {
            Text(
                chitalkaString(DebugLogsScreenSpec.I18nKeys.EMPTY),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    itemsIndexed(
                        state.entries,
                        key = { i, e -> DebugLogsScreenSpec.listItemKey(e.ts, i) },
                    ) { _, item ->
                        LogLine(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(label)
    }
}

@Composable
private fun LogLine(item: DebugLogEntry) {
    val (badgeColor, onBadge) = levelColors(item.level.wireName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            item.level.wireName,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(badgeColor)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            color = onBadge,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            item.message,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun levelColors(level: String): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (level.uppercase()) {
        "E", "ERROR" -> scheme.errorContainer to scheme.onErrorContainer
        "W", "WARN" -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        "I", "INFO" -> scheme.primaryContainer to scheme.onPrimaryContainer
        else -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }
}

private fun copyLogsToClipboard(context: Context, locale: AppLocale) {
    val body = debugLogFormatExport()
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val title = chitalkaString(context, locale, DebugLogsScreenSpec.I18nKeys.TITLE)
    cm.setPrimaryClip(ClipData.newPlainText(title, body))
}

private suspend fun exportLogsToShare(context: Context, locale: AppLocale) {
    val body = debugLogFormatExport()
    val name = DebugLogsScreenSpec.exportFileName()
    val file = File(context.cacheDir, name)
    withContext(Dispatchers.IO) {
        file.writeText(body, Charsets.UTF_8)
    }
    val uri =
        FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            file,
        )
    val chooserTitle = chitalkaString(context, locale, DebugLogsScreenSpec.I18nKeys.EXPORT_DIALOG_TITLE)
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = DebugLogsScreenSpec.EXPORT_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, chooserTitle)
        }
    context.startActivity(Intent.createChooser(send, chooserTitle))
}
