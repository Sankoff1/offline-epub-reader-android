package com.ncorti.kotlin.template.app.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chitalka.i18n.APP_LOCALES
import com.chitalka.i18n.AppLocale
import com.chitalka.screens.settings.SettingsScreenSpec
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

private val DROPDOWN_CORNER_RADIUS = 16.dp
private val TRIGGER_TONAL_ELEVATION = 2.dp
private val ROW_PADDING_H = 16.dp
private val ROW_PADDING_V = 14.dp
private const val ANIM_MS = 180
private const val DIVIDER_ALPHA = 0.4f

@Composable
internal fun LanguageDropdown(
    selected: AppLocale,
    onSelect: (AppLocale) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = DIVIDER_ALPHA)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DROPDOWN_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = TRIGGER_TONAL_ELEVATION,
    ) {
        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                (fadeIn(tween(ANIM_MS)) togetherWith fadeOut(tween(ANIM_MS)))
            },
            label = "languageDropdownContent",
        ) { isExpanded ->
            if (!isExpanded) {
                CollapsedTrigger(
                    title = chitalkaString(SettingsScreenSpec.languageOptionKey(selected)),
                    onClick = { expanded = true },
                )
            } else {
                ExpandedList(
                    selected = selected,
                    dividerColor = dividerColor,
                    onPick = { loc ->
                        expanded = false
                        if (loc != selected) onSelect(loc)
                    },
                )
            }
        }
    }
}

@Composable
private fun CollapsedTrigger(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ROW_PADDING_H, vertical = ROW_PADDING_V),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpandedList(
    selected: AppLocale,
    dividerColor: Color,
    onPick: (AppLocale) -> Unit,
) {
    Column {
        APP_LOCALES.forEachIndexed { index, loc ->
            if (index > 0) HorizontalDivider(color = dividerColor)
            LocaleOptionRow(
                title = chitalkaString(SettingsScreenSpec.languageOptionKey(loc)),
                isSelected = loc == selected,
                onClick = { onPick(loc) },
            )
        }
    }
}

@Composable
private fun LocaleOptionRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val rowBg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable(onClick = onClick)
            .padding(horizontal = ROW_PADDING_H, vertical = ROW_PADDING_V),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

