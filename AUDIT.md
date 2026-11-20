# Expat Unity Android audit

Reviewed: 7 November 2026. Follow-ups: 9 November 2026 (`6f54bd8`) and 20 November 2026 (`7758934`). Scope is the current multi-module app, including `core:media` and `feature:map`. This follow-up is a static read of the commits since 9 November. It does not include a device run.

Severity:

- **High** — wrong behavior, a security hole in release, or numbers a user could act on.
- **Medium** — real defect or structural risk, limited blast radius.
- **Low** — maintainability, polish, or hardening.

## What landed since 9 November

Bottom navigation has Catalog, Favorites, and Map. Catalog and Favorites share an adaptive list-detail scaffold. Map plots complexes that have coordinates and opens a full-screen detail. Detail can play a video, toggle a favorite, and share the cover image into the detail pane. Room is at version 5, with auto-migrations through 4→5. Java and Kotlin bytecode are 17. `ComplexDetailDaoTest` and `FavoritesDaoTest` exercise `replaceAll` and favorite toggles on an in-memory database. `VideoPlayerViewModelTest` checks load, same-URL skip, and pause/resume.

The list-detail split, assisted `ComplexViewModel`, and the player factory are in good shape. The player is created only after the user taps play, released in `onCleared`, and not reloaded for the same URL. Coordinates with a null latitude or longitude are dropped instead of becoming a marker. The Maps key is a manifest placeholder, not a literal in source.

## Fix first

1. Stop substituting the Big Buck Bunny sample for a missing video (`VID-1`).
2. Cleartext, body logging, and the release API path are unchanged (`SEC-1`, `SEC-2`, `SEC-3`).
3. The catalog still stores page 1 only (`COR-3`). The map and the favorites tab read that same truncated cache.
4. `I18N-1` is fixed. Tab, video, offline, and favorite strings now match de, ru, and uk, and the Ukrainian unit plural includes `other`.

## New findings

### VID-1 — A missing video URL becomes a sample cartoon

**High.** `ComplexDetailDto.toDomain()` stores a public ExoPlayer sample whenever the CMS sends no URL:

```52:52:core/data/src/main/java/de/expatunity/core/data/DefaultComplexRepository.kt
    videoUrl = videoUrl?.takeIf { it.isNotBlank() } ?: "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
```

That value is written into `complex_details`. Every listing without its own video then shows Big Buck Bunny, and the URL stays in Room until a later response contains a real one. Leave `videoUrl` null and let `ComplexScreen` skip `VideoPreview`.

### VID-2 — Fullscreen locks the whole activity, and playback errors are invisible

**Medium.** `ForceLandscape` sets `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` on the activity, which recreates it. `rememberSaveable` keeps the dialog, but the rest of the app rotates with the player. `onDispose` skips restoring portrait while `isChangingConfigurations` is true, so a rotation during exit can leave the activity landscape.

`VideoPlayerViewModel.load` always sets `playWhenReady = true` and never listens for player errors. A bad URL is a black rectangle. Position survives rotation only because the same `ViewModel` keeps the `ExoPlayer`. Process death starts the file again from the beginning.

### MAP-1 — The map depends on the catalog cache and a fixed camera

**Medium.** `MapViewModel` only observes Room. It never calls `refreshComplexes`. Markers exist after the catalog tab has successfully stored page 1, and only for rows with both latitude and longitude. The camera is fixed on Germany (`51.16, 10.45`, zoom `5.5`) and does not move to the markers. A marker opens the complex only from the info window, not from the marker click.

`com.google.android.geo.API_KEY` is `${MAPS_API_KEY}` via the secrets plugin. `secrets.properties` is not in the repo and not in `.gitignore`. Add it to `.gitignore` before a local key file is committed. There is no checked-in default, so a clean checkout has an empty placeholder.

### NAV-1 — The favorites tab can flash the full catalog

**Medium.** Favorites uses a second `CatalogViewModel` (`key = "catalog-true"`), whose filter starts at `false`. `LaunchedEffect` sets `favoritesOnly` afterwards:

```56:58:feature/catalog/src/main/java/de/expatunity/feature/catalog/CatalogScreen.kt
    LaunchedEffect(favoritesOnly) {
        if (favoritesOnly) viewModel.onFavoritesOnlyChange(favoritesOnly)
    }
```

The first emission can be the unfiltered list. Pass the flag into the view model, or start `_favoritesOnly` from the route argument.

The app `Scaffold` applies only the bottom inset. The map and the catalog top sit under the status bar. `TabPlaceholder` is unused. Catalog and Favorites each call `refresh()` from `init`, so opening Favorites starts a second `replaceAll` of page 1 (`COR-6`).

Detail location from the API is parsed and then dropped. `ComplexDetailEntity` has no coordinates, and the cache mapper leaves `ComplexDetail.location` null. The map uses the list table, so this does not hide markers. The detail model field is unused.

## What changed in older findings

| ID | Status on 20 November |
| --- | --- |
| COR-2 | Still fixed. Loading now shows shimmer placeholders instead of a single spinner. |
| COR-4 | Half fixed. List rows go through `String.toStatus()`. Detail and apartment rows still use `ListingStatus.valueOf`. `ComplexDetailDaoTest` stores the status `"AVAILABLE"`, which is not a `ListingStatus` name and would crash that mapper. |
| ARC-2 | Fixed. Calculator and theme packages match their modules. Java 17 and Kotlin `jvmTarget` 17 are set in the root build. `libs.material` and AppCompat are still unused dependencies on catalog, complex, calculator, map, and `core:media`. |
| UI-1 | Partly fixed. The catalog no longer nests a second copy of the same `Modifier`, and the app no longer wraps only the catalog in a scaffold. `ComplexScreen` still draws `ErrorState` with no scaffold padding. `fallbackUsed` is still unused. |
| UI-2 | Gallery placeholders use `surfaceVariant`. Dots are still raw white. Search is still by name only. `filterBy` is gone from the view model. |
| TEST-1 | Room coverage improved for `replaceAll`, locale isolation, and favorite toggle. Still no test for the repository, parsers, migrations 1→5, the calculator view model, or an empty successful catalog refresh. |
| SEC-1, SEC-2, SEC-3, SEC-4, SEC-5 | Unchanged. Cleartext is still allowed for every build, HTTP bodies are still logged at `BODY`, and release `BASE_URL` is still `https://www.expat-unity.de/cms/` inside `optimization {}`. Video URLs are now another client of that cleartext policy. Backup rules are still the sample templates, while the database holds favorites. |
| COR-1, COR-3, COR-5, COR-6, COR-7 | Unchanged. The forecast math, page-1 refresh, collapsed HTTP errors, overlapping catalog refresh, and `localhost` on the emulator are as they were on 9 November. |
| I18N-1 | Fixed. Default and `feature:complex` `error_fav_failed` are English. de, ru, and uk cover offline copy, favorite errors, add/remove labels, tabs, and video controls. Ukrainian `catalog_units_available` includes `other`. |

## Suggested order of work

| Order | IDs | Outcome |
| --- | --- | --- |
| 1 | VID-1 | Listings without a video do not play a cartoon. |
| 2 | SEC-1, SEC-2, SEC-3, COR-7 | Release talks only to the real HTTPS API, and debug reaches the host machine. |
| 3 | COR-3, NAV-1, MAP-1 | Favorites and the map show the full catalog, the favorites tab does not flash every row, and the camera fits the markers. |
| 4 | COR-1 | The forecast cannot hide closing costs or print a negative loan. |
| 5 | COR-4, COR-5 | A bad cached status cannot crash the detail screen, and failures stay diagnosable. |
