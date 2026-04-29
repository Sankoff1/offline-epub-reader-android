# Offline EPUB Reader

Офлайн-ридер EPUB под Android. Импорт через системный пикер, локальная
библиотека на SQLite, чтение в WebView с JS-мостом, Compose поверх.

Проект мульти-модульный: `app` (Android-аппликейшн), `library-android`
(SQLite, EPUB, picker), `library-kotlin` (чистый JVM — домен и UI-контракты).

## Быстрый старт

```bash
./gradlew :app:installDebug
```

Нужен JDK 17 и Android SDK с platform `android-36`. На винде —
`gradlew.bat`. Если что-то отвалилось — `./gradlew --stop && clean`.

APK:

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

Лежит в `app/build/outputs/apk/`. Версия и `applicationId` тянутся из
`gradle.properties` (`APP_VERSION_NAME`, `APP_VERSION_CODE`, `APP_ID`).

## Архитектура

`app` зависит от обоих библиотечных модулей. `library-android` зависит
от `library-kotlin`. `library-kotlin` ни от чего проектного не зависит и
не тянет Android SDK — это нижний слой, и именно поэтому он отдельный
модуль: всё, что туда складывается, тестируется обычным `:test` без
эмулятора.

В `library-kotlin` живут доменные типы (`LibraryBookRecord`,
`ReadingProgress`), навигационные контракты, спецификации экранов
(`ReaderScreenSpec`, `BookListScreenSpec`), JS-мост ридера
(`ReaderBridgeScripts`, `ReaderBridgeMessages`), i18n и тема. В
`library-android` — `StorageService` (SQLite), `EpubService`,
`EpubPickerAndroid`, `SharedPreferencesKeyValueStore`,
`ReaderNavCoordinator`. В `app` — Compose-UI, `NavHost` и экран чтения
с `WebView`. Граница между модулями жёсткая: появился `import android.*`
в `library-kotlin` — значит, файл лежит не там.

`NavHost` плоский: два маршрута — `Main` и `Reader/{bookId}/{bookPath}`.
Drawer-вкладки — это локальное состояние внутри `Main`, а не вложенный
backstack. Иначе `back` из ридера уносил бы не туда.

Room сознательно не ставили: схема маленькая, миграции писать руками
короче, чем разбирать аннотации, плюс не хочется тащить KSP в сборку.
Поэтому `StorageService` сидит на `SQLiteOpenHelper` напрямую
(`ChitalkaSqliteOpenHelper`), курсоры маппятся по ординалам в
`StorageServiceMappers`.

## EPUB

WebView выбран по простой причине: EPUB — это HTML/CSS/картинки в ZIP, и
свой рендерер тут писать смысла нет. Тёмная тема инжектируется как CSS
через `ReaderDarkModeHtml`, чтобы не моргала между загрузкой страницы и
применением Compose-темы.

Импорт работает так:

1. `EpubPickerAndroid` через `ActivityResultContracts.OpenDocument`
   возвращает URI.
2. `importEpubToLibrary` копирует файл во внутреннее хранилище и
   распаковывает в `filesDir/books/<bookId>/`. SAF-URI после этого не
   нужен — пользователь может удалить исходник.
3. `EpubService.open()` на `Dispatchers.IO` читает `container.xml`,
   парсит OPF/spine, достаёт метаданные. UTF-8 BOM в `container.xml` и
   OPF разбирается отдельно (`EpubIo.kt`) — на это уже наступали.

Перед загрузкой главы в WebView `EpubChapterPrep` одним проходом
`StringBuilder` переписывает относительные `<img src>` в
`file://`-URI к распакованному дереву. Раньше тут тормозили большие
главы; one-pass + регэкспы уровня файла укладываются в десятки
миллисекунд. DOM-парсер был бы избыточен — нам нужно только переписать
ссылки.

Глава грузится через `loadDataWithBaseURL`, baseUrl указывает в
каталог главы — относительные шрифты и CSS работают без модификаций.

Анимация перехода между главами — два WebView-слоя (A/B). На неактивном
готовится следующая глава, по `transitionProgress` идёт alpha + сдвиг +
shade, потом слои меняются ролями. Без этого был фликер: новая глава
появлялась раньше, чем успевала отрисоваться. Математика
(`outgoingPageTranslateXPx` и т.д.) вынесена в
`ReaderScreenSpecTransitions` отдельно от композаблов — её можно
покрывать юнит-тестами.

Прогресс хранится как `lastChapterIndex` + `scrollOffset` +
`scrollRangeMax` (смещение и максимум прокрутки внутри главы). Доля
прочитанного для списка библиотеки — позиция главы плюс смещение внутри,
формула в `LibraryListProgressFraction`. Иначе прогресс прыгал бы
рывками по главам.

Запись прогресса дебаунсится (`schedulePersist` / `persistNow` в
`ReaderScreenStateOps`) — на каждом кадре в SQLite не пишем.

## Ошибки

Граница data ↔ UI проходит по строкам. В `library-kotlin` лежат
`StorageErrorCodes` и `EpubErrorCodes` со стабильными константами
(`STORAGE_ERR_*`, `EPUB_*`). Исключения из data-слоя несут эти коды как
`message`, без локализации:

```kotlin
class StorageServiceError(code: String) : Exception(code)
```

Текст для пользователя собирается на стороне UI — `storageErrorMessage(locale, code)`
и `ReaderScreenSpec.readerOpenErrorMessage(...)`. Нужно это для двух
вещей: data-слой не должен знать про текущую локаль, и в логах остаётся
машинно-читаемый код, который можно фильтровать без NLP.

DB-операции обёрнуты в `withDb`/`withReadDb`, они гоняют лямбду через
`mapDbException` — `SQLiteException` превращается в `StorageServiceError`
с правильным кодом. Сломанный EPUB на импорте даёт `EpubServiceError`,
маппится через `readerOpenErrorMessage`. Все долгие suspend-операции
EPUB обёрнуты в `WithTimeout` (`com.chitalka.utils.withTimeout`) — на
патологически больших OPF лучше упасть с понятной ошибкой, чем повиснуть
со спиннером.

Глобальных `try { … } catch (Exception) { }` вокруг корутин ридера
нет — если EPUB кривой непредсказуемо, корутина падает с
`EpubServiceError`, UI рисует error-экран с кнопкой назад. Тихие
`catch (_: Exception) {}` в проекте запрещены.

## Производительность

`ChitalkaSqliteOpenHelper.onConfigure` включает WAL,
`synchronous=NORMAL` и `temp_store=MEMORY`. WAL даёт чтение
параллельно с записью — нужно, когда прогресс пишется на фоне, а UI
листает список. `synchronous=NORMAL` — компромисс по скорости в обмен
на риск потерять последнюю транзакцию при крахе ОС. Прогресс не
критичные данные, потеря одной записи допустима. **Не использовать
такой режим для оплаты или синхронизации**, если когда-нибудь
появятся.

Главный поток на IO не ходит. Импорт, парсинг, подготовка глав — на
`Dispatchers.IO`. Список книг загружается из БД один раз (по
`listRefreshNonce`); поиск фильтрует уже загруженный список через
`remember(rawBooks, normalizedSearchQuery)`, в SQLite не лезет.

Перерисовка панели отладочных логов коалесцируется через
`AtomicBoolean + delay(DEBUG_LOG_RELOAD_COALESCE_MS)`. Без этого шторм
`console.*` из WebView триггерил `reload()` per-line — ловили это в
`ChitalkaDebugLogsPane`.

## Production

Внешней телеметрии нет. Логи трёхслойные: `android.util.Log` идёт в
logcat плюс зеркалится в кольцевой `DebugLog` через `ChitalkaMirrorLog`;
`installConsoleCapture` забирает stdout/stderr в тот же буфер;
`WebChromeClient.onConsoleMessage` пишет туда же `console.*` со
страницы книги. Видно во вкладке drawer'а **Debug logs** —
`ChitalkaDebugLogsPane`: очистить, скопировать в clipboard,
экспортировать файлом. Идея в том, что пользователь, которому что-то
сломалось на конкретной книге, может выгрузить лог сам и прислать
без `adb`.

Точка интеграции для внешнего sink (Sentry/Crashlytics) — те же
`installConsoleCapture` + `ChitalkaMirrorLog`. Сейчас они только
агрегируют.

Релиз сейчас собирается без R8 (`isMinifyEnabled = false`). Перед
выкладкой в стор — включить и проверить ProGuard-правила Compose и
Navigation. Подпись в репозитории не лежит, конфигурируется локально
или в CI.

Lint в `app` настроен жёстко: `warningsAsErrors = true`, `abortOnError =
true`. Лучше упасть на CI, чем выкатить тёплое.

Тулчейн: AGP 8.13.2, Kotlin 2.3.21, Compose BOM 2025.12.01, JVM 17.
`compileSdk` / `targetSdk` — 36, `minSdk` — 26. Полный каталог в
`gradle/libs.versions.toml`.

## Что может сломаться

- **Android System WebView** обновляется отдельно от приложения. На
  старых сборках Android главы могут рендериться криво, повлиять на
  это мы не можем (только встраивать собственный движок, что большая
  работа).
- **DRM не поддерживается.** Adobe Adept и прочие зашифрованные EPUB не
  откроются. Не планируется.
- **Продвинутый EPUB 3** — SMIL, scripted content, MathML — обрабатывается
  как обычный HTML. Озвучка и интерактивность не работают.
- **Distribution.** Релиз без minify раздут и не обфусцирован. Нужно
  включать R8 перед стором.
- **Двойное хранение EPUB.** Держим и копию исходника, и распакованное
  дерево. Если коллекция большая, занимает место. Можно перейти на
  on-the-fly распаковку с кэшем, но пока не упирались.
- **Тесты.** Юнит-тесты в `library-kotlin` есть, инструментальный один
  (`StorageServiceInstrumentedTest`). UI ридера регрессии ловятся
  глазами.

## Тесты и проверки

```bash
./gradlew :library-kotlin:test                        # JVM-юнит
./gradlew :library-android:connectedAndroidTest       # инструментальный, нужен девайс
./gradlew :app:lintDebug
./gradlew detekt
```
