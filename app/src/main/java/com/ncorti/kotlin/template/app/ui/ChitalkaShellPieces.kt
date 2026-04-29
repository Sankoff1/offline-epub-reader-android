package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.chitalka.navigation.DrawerNavigationSpec
import com.chitalka.navigation.DrawerScreen
import com.chitalka.navigation.drawerLabelI18nPath
import com.chitalka.theme.ThemeMode
import com.chitalka.ui.firstlaunch.FirstLaunchModalSpec
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

internal fun DrawerScreen.icon(): ImageVector =
    when (this) {
        DrawerScreen.ReadingNow -> Icons.Filled.Home
        DrawerScreen.BooksAndDocs -> Icons.AutoMirrored.Filled.List
        DrawerScreen.Favorites -> Icons.Filled.Favorite
        DrawerScreen.Cart -> Icons.Filled.Delete
        DrawerScreen.DebugLogs -> Icons.Filled.Build
        DrawerScreen.Settings -> Icons.Filled.Settings
    }

@Composable
internal fun ChitalkaDrawerContent(
    selected: DrawerScreen,
    onSelect: (DrawerScreen) -> Unit,
) {
    val drawerColor =
        when (LocalChitalkaThemeMode.current) {
            ThemeMode.LIGHT -> Color(0xFFE3E1DA)
            ThemeMode.DARK -> Color(0xFF26303F)
        }
    val flushSide = CornerSize(0.dp)
    ModalDrawerSheet(
        drawerShape = MaterialTheme.shapes.large.copy(
            topStart = flushSide,
            bottomStart = flushSide,
        ),
        drawerContainerColor = drawerColor,
        drawerTonalElevation = 0.dp,
    ) {
        Column(Modifier.padding(top = 24.dp, bottom = 12.dp)) {
            DrawerNavigationSpec.drawerScreenOrder.forEach { screen ->
                NavigationDrawerItem(
                    selected = screen == selected,
                    onClick = { onSelect(screen) },
                    icon = {
                        Icon(
                            screen.icon(),
                            contentDescription = null,
                        )
                    },
                    label = { Text(chitalkaString(screen.drawerLabelI18nPath)) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(),
                )
            }
        }
    }
}

@Composable
internal fun WelcomeDialog(
    welcomePickerHint: String?,
    onPick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        // Закрытие только через кнопки «Выбрать .epub» / «Отмена»: back и тап по scrim не дисмиссят.
        onDismissRequest = { },
        icon = {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(chitalkaString(FirstLaunchModalSpec.I18nKeys.MESSAGE)) },
        text = {
            welcomePickerHint?.let { key ->
                Text(
                    chitalkaString(key),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onPick) {
                Text(chitalkaString(FirstLaunchModalSpec.I18nKeys.PICK_EPUB))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(chitalkaString(FirstLaunchModalSpec.I18nKeys.CANCEL))
            }
        },
    )
}
