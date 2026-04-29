package com.ncorti.kotlin.template.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode

private fun ThemeColors.toLightScheme(): ColorScheme {
    val bg = parseHexColor(background)
    val surface = parseHexColor(menuBackground)
    val primary = parseHexColor(topBar)
    val onPrimary = parseHexColor(topBarText)
    val onBg = parseHexColor(text)
    val onSurfaceVariant = parseHexColor(textSecondary)
    val interactive = parseHexColor(interactive)
    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = interactive,
        onPrimaryContainer = onBg,
        secondary = interactive,
        onSecondary = onBg,
        background = bg,
        onBackground = onBg,
        surface = surface,
        onSurface = onBg,
        surfaceVariant = surface,
        onSurfaceVariant = onSurfaceVariant,
    )
}

private fun ThemeColors.toDarkScheme(): ColorScheme {
    val bg = parseHexColor(background)
    val surface = parseHexColor(menuBackground)
    val primary = parseHexColor(topBar)
    val onPrimary = parseHexColor(topBarText)
    val onBg = parseHexColor(text)
    val onSurfaceVariant = parseHexColor(textSecondary)
    val interactive = parseHexColor(interactive)
    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = interactive,
        onPrimaryContainer = onBg,
        secondary = interactive,
        onSecondary = onBg,
        background = bg,
        onBackground = onBg,
        surface = surface,
        onSurface = onBg,
        surfaceVariant = surface,
        onSurfaceVariant = onSurfaceVariant,
    )
}

@Composable
fun ChitalkaMaterialTheme(
    mode: ThemeMode,
    colors: ThemeColors,
    content: @Composable () -> Unit,
) {
    val scheme =
        when (mode) {
            ThemeMode.LIGHT -> colors.toLightScheme()
            ThemeMode.DARK -> colors.toDarkScheme()
        }
    MaterialTheme(
        colorScheme = scheme,
        content = content,
    )
}
