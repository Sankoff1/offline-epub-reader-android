package com.chitalka.theme

import com.chitalka.library.LastOpenBookPersistence
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private class InMemoryPersistence : LastOpenBookPersistence {
    private val map = mutableMapOf<String, String>()

    override suspend fun getItem(key: String): String? = map[key]

    override suspend fun setItem(key: String, value: String) {
        map[key] = value
    }

    override suspend fun removeItem(key: String) {
        map.remove(key)
    }
}

class ThemePreferencesTest {

    @Test
    fun loadPersistedThemeMode_nullWhenMissing() = runBlocking {
        assertNull(loadPersistedThemeMode(InMemoryPersistence()))
    }

    @Test
    fun loadPersistedThemeMode_parsesLightDark() = runBlocking {
        val p = InMemoryPersistence()
        p.setItem(THEME_MODE_STORAGE_KEY, "light")
        assertEquals(ThemeMode.LIGHT, loadPersistedThemeMode(p))
        p.setItem(THEME_MODE_STORAGE_KEY, "dark")
        assertEquals(ThemeMode.DARK, loadPersistedThemeMode(p))
    }

    @Test
    fun loadPersistedThemeMode_invalidReturnsNull() = runBlocking {
        val p = InMemoryPersistence()
        p.setItem(THEME_MODE_STORAGE_KEY, "LIGHT")
        assertNull(loadPersistedThemeMode(p))
    }

    @Test
    fun persist_roundTrip() = runBlocking {
        val p = InMemoryPersistence()
        persistThemeMode(p, ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, loadPersistedThemeMode(p))
    }

    @Test
    fun toggle_cycles() {
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.toggle())
        assertEquals(ThemeMode.LIGHT, ThemeMode.DARK.toggle())
    }

    @Test
    fun togglePersistedThemeMode_updatesStorage() = runBlocking {
        val p = InMemoryPersistence()
        persistThemeMode(p, ThemeMode.LIGHT)
        val next = togglePersistedThemeMode(p, ThemeMode.LIGHT)
        assertEquals(ThemeMode.DARK, next)
        assertEquals(ThemeMode.DARK, loadPersistedThemeMode(p))
    }

    @Test
    fun themeUiState_exposesColorsForMode() {
        val state = ThemeUiState(ThemeMode.LIGHT)
        assertEquals(lightThemeColors, state.colors)
        assertEquals(darkThemeColors, ThemeUiState(ThemeMode.DARK).colors)
    }
}
