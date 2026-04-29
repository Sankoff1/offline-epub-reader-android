package com.chitalka.ui.topbar

import com.chitalka.navigation.DrawerScreen

/** Поведение верхней панели: показ поиска, кнопок drawer, авто-закрытие поиска при смене экрана. */
object AppTopBarSpec {

    /** Маршруты, где поиск по библиотеке скрыт. */
    val nonSearchableRouteNames: Set<String> =
        setOf(
            DrawerScreen.Settings.routeName,
            DrawerScreen.DebugLogs.routeName,
        )

    fun isDrawerRouteSearchable(routeName: String): Boolean =
        routeName !in nonSearchableRouteNames

    /** Состояние библиотеки, влияющее на видимость кнопок поиска. */
    data class SearchChromeState(
        val bookCount: Int,
        val isSearchOpen: Boolean,
        val searchQuery: String,
    )

    fun shouldShowSearchButton(
        routeName: String,
        state: SearchChromeState,
    ): Boolean =
        isDrawerRouteSearchable(routeName) && state.bookCount > 0 && !state.isSearchOpen

    fun shouldShowSearchInput(
        routeName: String,
        state: SearchChromeState,
    ): Boolean =
        isDrawerRouteSearchable(routeName) && state.isSearchOpen

    fun shouldShowClearQueryButton(
        routeName: String,
        state: SearchChromeState,
    ): Boolean =
        shouldShowSearchInput(routeName, state) && state.searchQuery.isNotEmpty()

    /** При уходе на экран без поиска нужно автоматически закрыть поле. */
    fun shouldAutoCloseSearchForRoute(
        routeName: String,
        isSearchOpen: Boolean,
    ): Boolean =
        !isDrawerRouteSearchable(routeName) && isSearchOpen

    object I18nKeys {
        const val A11Y_OPEN_MENU = "a11y.openMenu"
        const val A11Y_SEARCH = "a11y.search"
        const val A11Y_CLOSE_SEARCH = "a11y.closeSearch"
        const val SEARCH_PLACEHOLDER = "search.placeholder"
    }
}
