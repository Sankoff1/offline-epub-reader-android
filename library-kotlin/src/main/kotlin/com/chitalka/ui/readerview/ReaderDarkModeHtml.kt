package com.chitalka.ui.readerview

import com.chitalka.theme.ThemeColors

/**
 * Initial paint темы: вставляет в `<head>` блок стилей на CSS-переменных
 * `--chitalka-bg` / `--chitalka-fg`. На лету эти переменные перезаписывает
 * JS-обработчик `__chitalkaApplyTheme` (см. `injectedScrollBridge.js`).
 *
 * Запекание в HTML нужно только чтобы избежать «вспышки» белого фона
 * до первой инжекции скриптов моста после `onPageFinished`.
 */
fun injectDarkReaderHead(
    html: String,
    colors: ThemeColors,
): String {
    val block = buildInitialThemeStyleBlock(colors)
    val headClose = Regex("</head>", RegexOption.IGNORE_CASE).find(html)
        ?: return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/>$block</head><body>$html</body></html>"
    return html.substring(0, headClose.range.first) + block + html.substring(headClose.range.first)
}

private fun buildInitialThemeStyleBlock(colors: ThemeColors): String =
    """<style type="text/css" id="chitalka-reader-dark">
:root{--chitalka-bg:${colors.background};--chitalka-fg:${colors.text};color-scheme:dark;}
html{background:var(--chitalka-bg)!important;}
body{background:var(--chitalka-bg)!important;color:var(--chitalka-fg)!important;}
a{color:${colors.topBarText}!important;}
p,h1,h2,h3,h4,h5,h6,li,td,th,div,span,blockquote,figcaption,dd,dt,label{color:inherit!important;}
pre,code,samp,kbd{background:rgba(255,255,255,0.08)!important;color:inherit!important;}
table{color:inherit!important;}
</style>"""
