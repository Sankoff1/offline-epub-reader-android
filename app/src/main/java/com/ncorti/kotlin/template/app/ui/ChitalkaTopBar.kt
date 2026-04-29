@file:Suppress("LongParameterList")

package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chitalka.navigation.DrawerScreen
import com.chitalka.navigation.drawerLabelI18nPath
import com.chitalka.ui.topbar.AppTopBarSpec
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString

private val SEARCH_FIELD_HEIGHT = 40.dp
private val SEARCH_FIELD_CORNER = 20.dp
private val SEARCH_FIELD_PADDING_H = 12.dp
private const val SEARCH_FIELD_BG_ALPHA = 0.16f
private const val SEARCH_FIELD_PLACEHOLDER_ALPHA = 0.7f
private val SEARCH_ICON_SIZE = 18.dp
private val SEARCH_ICON_GAP = 10.dp
private val CLEAR_BUTTON_GAP = 4.dp
private val CLEAR_BUTTON_SIZE = 28.dp
private val CLEAR_ICON_SIZE = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChitalkaTopBar(
    selected: DrawerScreen,
    searchChrome: AppTopBarSpec.SearchChromeState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onClearQuery: () -> Unit,
) {
    val showSearchInput = AppTopBarSpec.shouldShowSearchInput(selected.routeName, searchChrome)
    TopAppBar(
        title = {
            if (showSearchInput) {
                CompactSearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = chitalkaString(AppTopBarSpec.I18nKeys.SEARCH_PLACEHOLDER),
                    showClear = AppTopBarSpec.shouldShowClearQueryButton(
                        selected.routeName,
                        searchChrome,
                    ),
                    onClear = onClearQuery,
                )
            } else {
                Text(
                    chitalkaString(selected.drawerLabelI18nPath),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        navigationIcon = {
            if (showSearchInput) {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = chitalkaString(AppTopBarSpec.I18nKeys.A11Y_OPEN_MENU),
                    )
                }
            } else {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = chitalkaString(AppTopBarSpec.I18nKeys.A11Y_OPEN_MENU),
                    )
                }
            }
        },
        actions = {
            if (!showSearchInput &&
                AppTopBarSpec.shouldShowSearchButton(selected.routeName, searchChrome)
            ) {
                IconButton(onClick = onOpenSearch) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = chitalkaString(AppTopBarSpec.I18nKeys.SEARCH_PLACEHOLDER),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun CompactSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    showClear: Boolean,
    onClear: () -> Unit,
) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SEARCH_FIELD_HEIGHT)
            .clip(RoundedCornerShape(SEARCH_FIELD_CORNER))
            .background(onPrimary.copy(alpha = SEARCH_FIELD_BG_ALPHA))
            .padding(horizontal = SEARCH_FIELD_PADDING_H),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = onPrimary,
            modifier = Modifier.size(SEARCH_ICON_SIZE),
        )
        Spacer(Modifier.width(SEARCH_ICON_GAP))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onPrimary.copy(alpha = SEARCH_FIELD_PLACEHOLDER_ALPHA),
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = onPrimary,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                ),
                cursorBrush = SolidColor(onPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }
        if (showClear) {
            Spacer(Modifier.width(CLEAR_BUTTON_GAP))
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(CLEAR_BUTTON_SIZE),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    tint = onPrimary,
                    modifier = Modifier.size(CLEAR_ICON_SIZE),
                )
            }
        }
    }
}
