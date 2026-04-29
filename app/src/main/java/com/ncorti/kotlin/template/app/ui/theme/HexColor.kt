@file:Suppress("MagicNumber")

package com.ncorti.kotlin.template.app.ui.theme

import androidx.compose.ui.graphics.Color

private const val OPAQUE_BLACK_ARGB: Int = 0xFF000000.toInt()
private const val ARGB_RGB_MASK: Int = 0xFFFFFF
private const val ARGB_FULL_MASK: Long = 0xFFFFFFFFL

/**
 * Парсит HEX-строку (с `#` или без) в [Color]. Допускает RRGGBB и AARRGGBB.
 * При невалидном входе возвращает чёрный — теме лучше деградировать, чем падать на старте.
 */
fun parseHexColor(hex: String): Color {
    val s = hex.trim().removePrefix("#")
    val v = s.toLongOrNull(16) ?: return Color(OPAQUE_BLACK_ARGB)
    val argb: Int =
        when (s.length) {
            6 -> OPAQUE_BLACK_ARGB or (v.toInt() and ARGB_RGB_MASK)
            8 -> (v and ARGB_FULL_MASK).toInt()
            else -> OPAQUE_BLACK_ARGB
        }
    return Color(argb)
}
