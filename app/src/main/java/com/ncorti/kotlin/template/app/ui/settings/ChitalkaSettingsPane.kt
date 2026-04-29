package com.ncorti.kotlin.template.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chitalka.i18n.AppLocale
import com.chitalka.screens.settings.SettingsScreenSpec
import com.chitalka.theme.ThemeMode
import com.ncorti.kotlin.template.app.BuildConfig
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

private val SETTINGS_PADDING = 16.dp
private val SETTINGS_GAP = 16.dp
private val CARD_CORNER_RADIUS = 16.dp
private val CARD_ELEVATION = 1.dp
private val CARD_ICON_TITLE_GAP = 12.dp
private val CARD_HEADER_BODY_GAP = 12.dp

@Composable
fun ChitalkaSettingsPane(
    viewModel: SettingsViewModel,
    locale: AppLocale,
    themeMode: ThemeMode,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(locale) { viewModel.onExternalLocaleChange(locale) }
    LaunchedEffect(themeMode) { viewModel.onExternalThemeModeChange(themeMode) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SETTINGS_PADDING),
        verticalArrangement = Arrangement.spacedBy(SETTINGS_GAP),
    ) {
        SettingsCard(
            icon = Icons.Filled.Build,
            title = chitalkaString(SettingsScreenSpec.I18nKeys.THEME_SECTION),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    chitalkaString(SettingsScreenSpec.I18nKeys.DARK_THEME),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = state.themeMode == ThemeMode.DARK,
                    onCheckedChange = { dark ->
                        viewModel.onThemeModeChange(if (dark) ThemeMode.DARK else ThemeMode.LIGHT)
                    },
                )
            }
        }

        SettingsCard(
            icon = Icons.Filled.Settings,
            title = chitalkaString(SettingsScreenSpec.I18nKeys.LANGUAGE_SECTION),
        ) {
            LanguageDropdown(
                selected = state.locale,
                onSelect = viewModel::onLocaleChange,
            )
        }

        SettingsCard(
            icon = Icons.Filled.Info,
            title = chitalkaString(SettingsScreenSpec.I18nKeys.VERSION_LABEL),
        ) {
            Text(
                BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CARD_ELEVATION),
    ) {
        Column(Modifier.padding(SETTINGS_PADDING)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(CARD_ICON_TITLE_GAP))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(CARD_HEADER_BODY_GAP))
            content()
        }
    }
}
