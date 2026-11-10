# Expat Unity Android audit

Reviewed: 7 November 2026. Follow-up: 9 November 2026, through commit `6f54bd8` (`add unit tests for ObserveCatalogUseCase in core:domain`). Scope is the multi-module app (`app`, `core:domain`, `core:data`, `core:network`, `core:database`, `core:ui`, `core:testing`, `feature:catalog`, `feature:complex`, `feature:calculator`). This is a static review plus the unit tests named below. It does not include a device run or a release build.

On 9 November these tests passed: `:core:domain:test`, `:feature:catalog:testDebugUnitTest`, `:feature:complex:testDebugUnitTest`.

Severity:

- **High** — wrong behavior, a security hole in release, or numbers a user could act on.
- **Medium** — real defect or structural risk, limited blast radius.
- **Low** — maintainability, polish, or hardening.

## What changed since 7 November

Favorites are stored in Room (`favorites`, schema 3, `AutoMigration(2, 3)`) and toggled from each catalog card. A "favorites only" chip filters that list. Search and the chip are combined in `ObserveCatalogUseCase`, which also reports `hasAnyComplexes` so an active filter is not mistaken for an empty catalog. Catalog and complex view models now call domain use cases instead of `ComplexRepository` directly. `core:data` owns the repositories. `CatalogDestination` lives in `de.expatunity.feature.catalog`.

The new filter logic is the right shape. `ObserveCatalogUseCase` applies the query and the favorites chip together, keeps favorite ids on the result, and sets `hasAnyComplexes` from the unfiltered list. `ObserveCatalogUseCaseTest` covers no filter, query plus favorites, and a filter that matches nothing while complexes still exist. `CatalogSearchTest` covers the same combinations through the view model, including removing the last favorite. `CatalogViewModelTest` covers a successful toggle and a failed write that leaves the previous ids in place. Favorite ids are not locale-specific, which matches a complex id that is stable across languages. Each toggle runs in a Room transaction, so two taps serialize instead of interleaving a read and a write.

Most of the other new use cases (`ObserveComplexesUseCase`, `ObserveComplexUseCase`, `RefreshComplexesUseCase`, `RefreshComplexUseCase`, `ObserveFavoriteIdsUseCase`, `ToggleFavoriteUseCase`) forward one repository call. `ObserveCatalogUseCase` is the one that adds behavior. That split is consistent; it does not by itself fix loading, paging, or error mapping.

## Resolved since 7 November

| ID | What changed |
| --- | --- |
| COR-2 | `_refreshing` is an input of `uiState`'s `combine`. An empty successful refresh can leave `Loading` and become `Content(empty)`. A filter that hides every row stays on `Content` because `hasAnyComplexes` is computed before filtering. |
| ARC-1 | `DefaultComplexRepository` and `DefaultFavoritesRepository` live in `core:data`. `core:network` no longer depends on Room, the database module, or `lifecycle-viewmodel-compose`. `SystemLocaleProvider` uses `javax.inject.Inject`. |
| ARC-2, in part | `CatalogDestination` is `de.expatunity.feature.catalog.CatalogDestination`, and the app imports it. `core:network` uses `compileSdk { version = release(37) }`. |
| QUAL-1, in part | Domain models are in their own files. Unused imports noted on 7 November are gone. The "move this to the catalog module" comment is gone. |

`COR-2` still has no view-model test for the empty-success path. The domain test covers "filter matches nothing" only.

## Fix first

1. Stop shipping cleartext and body logging in release (`SEC-1`, `SEC-2`).
2. Point release at the same mobile API path debug uses (`SEC-3`), and use `10.0.2.2` or a LAN host instead of `localhost` for the emulator (`COR-7`).
3. Load every catalog page (`COR-3`). Favorites-only cannot show a complex that was never stored.
4. Make the 10-year forecast honest: show assumptions, clamp the loan, and stop using `Double` for money (`COR-1`).
5. Map unknown cached statuses the same way the network mapper does, and keep the original exception (`COR-4`, `COR-5`).
6. Translate the new favorite strings (`I18N-1`).

## Open findings

### SEC-1 — Cleartext traffic is allowed in every build

**High.** Unchanged. `app/src/main/AndroidManifest.xml` sets `android:usesCleartextTraffic="true"` on `<application>`. Debug needs HTTP only for a local CMS. Release still accepts `http://` image and API URLs.

Restrict cleartext with a network security config: allow it for `localhost` / `10.0.2.2` in debug only, and set `usesCleartextTraffic="false"` on release.

### SEC-2 — HTTP bodies are logged in release

**High.** Unchanged. `NetworkModule.okHttp()` always adds `HttpLoggingInterceptor` at `Level.BODY`.

```23:25:core/network/src/main/java/de/expatunity/core/network/NetworkModule.kt
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()
```

Install the interceptor only in debug, and use `BASIC` or `HEADERS` even then.

### SEC-3 — Release base URL does not match the mobile API

**High.** Unchanged.

| Build | `BASE_URL` |
| --- | --- |
| debug | `http://localhost:1337/api/mobile/v1/` |
| release | `https://www.expat-unity.de/cms/` |

`MobileApi` calls `complexes` and `complexes/{id}` relative to that base. Release requests go to `/cms/complexes`. The release field is still nested inside `optimization { }` in `app/build.gradle.kts`. `buildConfigField` belongs on the build type. `optimization.packageScope` still lists only `androidx.**`, `kotlin.**`, and `kotlinx.**`.

### SEC-4 — CMS markdown and image URLs are rendered as-is

**Medium.** Unchanged. `ComplexScreen` passes `description` into `Markdown(...)`. Cover and gallery URLs go straight to Coil. Combined with `SEC-1`, an `http://` image is loaded in the clear.

### SEC-5 — Backup rules are still the sample, and the database now holds favorites

**Medium.** `allowBackup="true"` is set, and both backup XML files are still the Android Studio templates. Default Auto Backup includes `expatunity.db`. On 7 November that file was only a listing cache. It now also stores the user's favorites. Excluding the whole database would drop favorites on restore. Either back up the database on purpose, or move favorites to their own file and exclude only the cache tables.

### COR-1 — The 10-year forecast can show a misleading return

**High.** Unchanged. `InvestmentCalculator` still uses `Double`. Closing costs disappear when cash is lower than those costs. The loan can be negative. Tax is applied only as a refund when taxable income is negative. AfA (0.8 building share, 2.5%), rent growth (0), and appreciation (2%) are hardcoded in `CalculatorViewModel` and never shown. `YieldCalculator` is unused.

The domain test still locks one happy path only (`318_000` price, `70_000` cash, expenses `0`).

### COR-3 — Only the first page is stored

**High.** Unchanged, and it now limits favorites too. `refreshComplexes` always requests page 1:

```115:120:core/data/src/main/java/de/expatunity/core/data/DefaultComplexRepository.kt
    override suspend fun refreshComplexes(locale: String) {
        val fresh = try {
            api.complexes(locale, 1).items.map { it.toDomain() }
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) { throw AppException(e.toErrorType()) }
        dao.replaceAll(locale, fresh.map { it.toEntity(locale) })
```

`total` is ignored. Search and the favorites chip both filter that truncated list in memory. A favorite whose complex is only on page 2 never appears.

### COR-4 — Cached status uses `valueOf`, the network path does not

**Medium.** Unchanged. `String.toStatus()` in `DefaultComplexRepository` maps an unknown API status to `ListingStatus.UNKNOWN`. The cache mappers in `core/data/.../toDomain.kt` and `toEntity.kt` call `ListingStatus.valueOf`. An unexpected stored status crashes the collector.

### COR-5 — Failures lose their cause, and every HTTP status is "server"

**Medium.** Unchanged. `AppException` does not keep the cause. `toErrorType()` maps every `HttpException` to `Server`. The catalog snackbar still shows `error_offline_cached` for every refresh failure while content is on screen, including a 500. `ComplexScreen` still has one cached-data string. Favorite writes are separate: a failed toggle emits `FavoriteUpdateFailed` and does not pretend the device is offline. That part is right.

### COR-6 — Catalog refresh is still not single-flight

**Medium.** Unchanged. `CatalogViewModel.refresh()` does not cancel the previous job. `ComplexViewModel.load()` does. `replaceAll` is one transaction, so a single call cannot commit an empty table, but two overlapping refreshes are last-writer-wins. Detail observation still invalidates every collector on any write to `complex_details`, `complex_images`, or `complex_apartments`, then runs three queries. Images and apartments still have no foreign key to `complex_details`. Favorites also have no foreign key, so an id remains after the complex disappears from the cache. The favorites-only chip hides it, because the row is no longer in the list.

### COR-7 — Debug base URL points at the phone, not the computer

**Medium.** Unchanged. `http://localhost:1337` on an emulator is the emulator. The host machine is `10.0.2.2`.

### ARC-2 — Packages and unused UI dependencies

**Low.** Still open, smaller than on 7 November.

- `feature:calculator` code lives in `de.expatunity.calculator`.
- Theme files live in `core:ui` under `de.expatunity.app.ui.theme`.
- `core:database`, `core:data`, `core:testing`, and `core:ui` depend on AppCompat and Material. `core:data` also depends on the Retrofit serialization converter and does not use it. `core:ui` depends on Material 3 twice: once via the Compose BOM and once as `androidx-material3` pinned to 1.4.0. `ui-tooling-preview` is declared both through the BOM and as a pinned 1.12.1.
- Android library modules set Java 11 and mostly skip an explicit Kotlin `jvmTarget`.

### UI-1 — Catalog insets and the complex error state

**Medium.** Unchanged. `ExpatUnityApp` wraps the catalog in a `Scaffold`, and `CatalogScreen` adds another. The same `modifier` is passed to that inner `Scaffold` and used again on the `Box`. A `Modifier` belongs in one place.

`ComplexScreen` still does not pad the error branch under the top app bar. `CalculatorScreen` still prints its title twice, clears the result on invalid input with no message, and does not keep edits in `SavedStateHandle`. `fallbackUsed` is still stored and never shown. The favorites chip is not in `SavedStateHandle` either, so process death resets it. Rotation keeps it, because the view model survives.

### UI-2 — Search still matches the name only

**Low.** Filtering moved to `ObserveCatalogUseCase`, and it still matches `name` only. A city such as "Leipzig" returns nothing. `catalog_no_results` has no format argument, but the call site still passes `query`. The English copy is "Search is empty"; German, Russian, and Ukrainian say "no matches". The empty-state branch itself is correct: a non-blank query wins, then the favorites chip, then the empty catalog.

Prices still use `%1$,.0f €`. Gallery dots and the hero image still use raw white and light gray. The card image content description is still the complex name, so TalkBack reads it twice. The new favorite button does have add/remove descriptions. The search field still has a placeholder and no label.

`CatalogViewModel` still has a private `filterBy` that nothing calls. Search behavior lives in `ObserveCatalogUseCase`. The leftover function will drift if someone edits it.

### I18N-1 — Offline copy, Ukrainian plurals, and the new favorite strings

**Medium.** `error_offline_cached` is still English in `values-de`, `values-ru`, and `values-uk`. Ukrainian `catalog_units_available` still omits `other`.

The favorites feature adds new gaps:

- `error_fav_failed` is the Russian sentence "Не удалось изменить избранное" in the default, German, and Ukrainian files. Russian is the only locale where that string matches the language.
- `action_add_favorite` and `action_remove_favorite` are English in `de`, `ru`, and `uk`.
- `catalog_favorites_only` and `catalog_no_favorites` are translated in all four locales.

`SystemLocaleProvider` still has the dead Elvis on `Locale.getDefault().language`. The locale is still captured once in each view model.

### PERF-1 — Images and the catalog refresh do more work than they need

**Medium.** Unchanged. No OkHttp cache, no validators, Coil has no explicit decode size. One apartment write still wakes every open detail. `org.gradle.parallel` is still commented out. The build is now ten modules with `core:data`.

### TEST-1 — Catalog behavior is tested; the repository and the calculator are not

**Medium.** Catalog coverage improved. `ObserveCatalogUseCaseTest`, the favorites cases in `CatalogSearchTest`, and the toggle cases in `CatalogViewModelTest` failed to exist on 7 November, and they pass now.

Still open:

- `CalculatorViewModelTest` still asserts the default state twice and builds `SavedStateHandle(mapOf("id" to "42"))`, which is not the calculator route.
- Instrumented tests are still the Android Studio templates.
- Nothing tests `DefaultComplexRepository`, `DefaultFavoritesRepository`, the DTO parsers, `toErrorType`, Room `replaceAll`, or migrations 1→2 and 2→3.
- `FakeComplexRepository.refreshComplexes` still does not write the list.
- `YieldCalculatorTest` still covers gross yield only.
- There is no view-model test that an empty successful refresh ends on `Content(empty)` (`COR-2`).

### QUAL-1 — Small cleanups that remain

**Low.** `ComplexDetailDao` still has a commented-out `observe`. `Theme.kt` and `Type.kt` are still the Compose template. Comments still mix Russian and English (`libs.versions.toml`, calculator KDoc, a Russian comment in `CatalogViewModelTest`). There is still no README, CI workflow, Detekt, or ktlint. `core:testing` is still an Android library that pulls AppCompat and Material so JVM tests can see fakes.

## What is already solid

- Feature modules depend on a JVM domain layer. Repositories sit in `core:data`. HTTP stays in `core:network`.
- Offline cache is deliberate: observe Room, refresh into Room, keep showing cached rows when refresh fails.
- Room schema is exported. Version is 3, with auto-migrations 1→2 and 2→3. Favorites are a new table, not extra columns on the listing cache.
- The catalog distinguishes "nothing loaded", "nothing matched", and "no favorites", and that distinction is unit-tested.
- JSON uses `ignoreUnknownKeys = true`.
- Navigation destinations for catalog, complex, and calculator are `@Serializable`.
- Search is debounced (300 ms, immediate when cleared) and list items are keyed.
- `SharingStarted.WhileSubscribed(5_000)` avoids collecting Room while the screen is in the back stack for long.
- The only manifest permission is `INTERNET`. The launcher activity is the only exported component.
- Favorite failures are reported separately from catalog refresh failures.

## Suggested order of work

| Order | IDs | Outcome |
| --- | --- | --- |
| 1 | SEC-1, SEC-2, SEC-3, COR-7 | A release build talks only to the real HTTPS API, and debug reaches the host machine. |
| 2 | COR-3, COR-6 | The catalog stores every page, and a refresh cannot race itself. Favorites then match the full list. |
| 3 | COR-1 | The forecast cannot print a negative loan or hide closing costs and tax. |
| 4 | COR-4, COR-5, I18N-1 | Failures stay diagnosable, and favorite and offline copy match the locale. |
| 5 | SEC-4, SEC-5, UI-1 | Content policy, backup of favorites, and insets match a production app. |
| 6 | TEST-1, PERF-1, ARC-2 | Repository and calculator regressions are caught, and data modules stop depending on UI libraries. |
