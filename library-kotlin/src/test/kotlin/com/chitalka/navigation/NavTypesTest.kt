package com.chitalka.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavTypesTest {

    @Test
    fun fromRouteName() {
        assertEquals(DrawerScreen.Cart, DrawerScreen.fromRouteName("Cart"))
        assertNull(DrawerScreen.fromRouteName("Unknown"))
    }

}
