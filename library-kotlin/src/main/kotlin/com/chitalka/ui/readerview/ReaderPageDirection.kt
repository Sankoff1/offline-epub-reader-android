package com.chitalka.ui.readerview

/** Направление перелистывания, приходящее из WebView через мост. */
enum class ReaderPageDirection(
    val wire: String,
) {
    PREV("prev"),
    NEXT("next"),
    ;

    companion object {
        fun fromWire(value: String): ReaderPageDirection? =
            entries.find { it.wire == value }
    }
}
