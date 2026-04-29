package com.ncorti.kotlin.template.app.ui.i18n

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.chitalka.i18n.AppLocale
import com.ncorti.kotlin.template.app.ui.LocalChitalkaLocale
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Адаптер для строк Android-ресурсов с уважением к выбранной в приложении локали.
 * Системную локаль Activity не трогаем (нет recreate при смене языка) — вместо этого
 * получаем локализованные `Resources` через `createConfigurationContext` и кешируем их.
 *
 * На Android 13+ выбор языка ещё и пробрасывается в [LocaleManager], чтобы системные
 * экраны (настройки приложения, системные диалоги) видели согласованную локаль.
 */

private fun AppLocale.toJavaLocale(): Locale = Locale.forLanguageTag(code)

private val resourcesCache = ConcurrentHashMap<CacheKey, Resources>()

private data class CacheKey(val locale: AppLocale, val configSignature: Long)

/**
 * Сигнатура «существенных» полей [Configuration] для кеша. Сама `Configuration` мутабельна,
 * её нельзя класть в ключ; следим за тем, что влияет на ресурсы (font scale, тёмный режим,
 * плотность экрана).
 */
private fun configurationSignature(c: Configuration): Long {
    var h = c.fontScale.toRawBits().toLong()
    h = 31L * h + c.uiMode.toLong()
    h = 31L * h + c.densityDpi.toLong()
    return h
}

private fun localizedResources(
    context: Context,
    locale: AppLocale,
    baseConfig: Configuration,
): Resources {
    val key = CacheKey(locale, configurationSignature(baseConfig))
    return resourcesCache.getOrPut(key) {
        val cfg = Configuration(baseConfig).apply { setLocale(locale.toJavaLocale()) }
        context.applicationContext.createConfigurationContext(cfg).resources
    }
}

/** Получение строки по ключу из `*ScreenSpec.I18nKeys`. */
@Composable
fun chitalkaString(key: String): String {
    val locale = LocalChitalkaLocale.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val resources = remember(locale, configuration) {
        localizedResources(context, locale, configuration)
    }
    return remember(resources, key) {
        resources.getString(ChitalkaStringIds.idFor(key))
    }
}

/** Версия с подстановками. Аргументы передаются позиционно. */
@Composable
fun chitalkaString(key: String, vararg args: Any): String {
    val locale = LocalChitalkaLocale.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val resources = remember(locale, configuration) {
        localizedResources(context, locale, configuration)
    }
    // `args` — vararg-массив, его identity меняется при каждой recomposition; format всё равно дешёвый.
    return resources.getString(ChitalkaStringIds.idFor(key), *args)
}

/** Не-Composable вариант — для VM/коллбеков, где есть `Context` и `AppLocale`. */
fun chitalkaString(context: Context, locale: AppLocale, key: String): String {
    val resources = localizedResources(context, locale, context.resources.configuration)
    return resources.getString(ChitalkaStringIds.idFor(key))
}

fun chitalkaString(context: Context, locale: AppLocale, key: String, vararg args: Any): String {
    val resources = localizedResources(context, locale, context.resources.configuration)
    return resources.getString(ChitalkaStringIds.idFor(key), *args)
}

/**
 * Уведомляем платформу о выбранной локали приложения. На Android 13+ это синхронизирует
 * наш внутренний выбор с системным `LocaleManager` (страница настроек приложения, диалоги).
 * На более ранних API — no-op: рантайм UI всё равно идёт через [chitalkaString].
 */
fun applyAppLocaleToSystem(context: Context, locale: AppLocale) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val mgr = context.getSystemService(LocaleManager::class.java) ?: return
    val tags = LocaleList.forLanguageTags(locale.code)
    if (mgr.applicationLocales != tags) {
        mgr.applicationLocales = tags
    }
}
