package com.chitalka.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerNavigationSpecTest {

    @Test
    fun drawerScreenOrder_matchesNavigatorOrder() {
        assertEquals(
            listOf(
                "ReadingNow",
                "BooksAndDocs",
                "Favorites",
                "Cart",
                "DebugLogs",
                "Settings",
            ),
            DrawerNavigationSpec.drawerScreenOrder.map { it.routeName },
        )
    }

    @Test
    fun drawerLabelI18nPath_isSetForAllScreens() {
        assertEquals("drawer.readingNow", DrawerScreen.ReadingNow.drawerLabelI18nPath)
        assertEquals("drawer.books", DrawerScreen.BooksAndDocs.drawerLabelI18nPath)
        assertEquals("drawer.favorites", DrawerScreen.Favorites.drawerLabelI18nPath)
        assertEquals("drawer.cart", DrawerScreen.Cart.drawerLabelI18nPath)
        assertEquals("drawer.debugLogs", DrawerScreen.DebugLogs.drawerLabelI18nPath)
        assertEquals("drawer.settings", DrawerScreen.Settings.drawerLabelI18nPath)
    }
}
