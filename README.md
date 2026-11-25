# Expat Unity (Android)

Android client for [Expat Unity](https://www.expat-unity.de) — browse real-estate complexes, save favorites, view them on a map, open detail (gallery + video), and run a simple investment calculator.

Built with **Kotlin**, **Jetpack Compose**, **Hilt**, **Room**, **Retrofit**, **Media3**, and **Maps Compose**.

## Features

- **Catalog** — adaptive list/detail of complexes from the CMS API
- **Favorites** — local favorites (Room), same list/detail UX
- **Map** — markers for complexes that have coordinates
- **Complex detail** — gallery, video preview, share, favorite toggle
- **Calculator** — investment / yield estimates for an apartment
- **i18n** — English (default), German, Russian, Ukrainian (follows system locale)

## Modules

| Module | Role |
| --- | --- |
| `:app` | Application, navigation, DI wiring |
| `:core:domain` | Models and use cases |
| `:core:data` | Repository implementations |
| `:core:network` | Retrofit API + DTOs |
| `:core:database` | Room entities / DAOs |
| `:core:ui` | Shared Compose UI + theme |
| `:core:media` | ExoPlayer / video preview |
| `:core:testing` | Test fakes and helpers |
| `:feature:catalog` | Catalog + favorites list |
| `:feature:complex` | Complex detail |
| `:feature:map` | Google Map screen |
| `:feature:calculator` | Investment calculator |

## Requirements

- Android Studio (recent stable / canary that supports AGP 9.x)
- JDK **17**
- Android SDK with `compileSdk` / `targetSdk` **37**
- A device or emulator on API **26+**
- Google Maps API key (Maps SDK for Android)

## Setup

1. Clone the repo:

   ```bash
   git clone https://github.com/rundir/expat-unity-android.git
   cd expat-unity-android
   ```

2. Add a Google Maps SDK key (do **not** commit the real value). Preferred: create `secrets.properties` in the project root:

   ```properties
   MAPS_API_KEY=your_maps_sdk_key_here
   ```

   `local.defaults.properties` ships with an empty `MAPS_API_KEY` so the project syncs without a key. Restrict the key in Google Cloud Console to the Maps SDK for Android and your app package / SHA-1.

3. Open the project in Android Studio and sync Gradle, or from the CLI:

   ```bash
   ./gradlew :app:assembleDebug
   ```

4. Run on a device/emulator:

   ```bash
   ./gradlew :app:installDebug
   ```

### API endpoints

| Build type | Base URL |
| --- | --- |
| `debug` | `http://localhost:1337/api/mobile/v1/` |
| `release` | `https://www.expat-unity.de/cms/` |

On an emulator, `localhost` is the emulator itself. Point debug at a reachable CMS (host machine via `10.0.2.2`, or a staging URL) if you need live data while developing.

## Tests

```bash
./gradlew test
```

## Project notes

- Maps key is injected via `${MAPS_API_KEY}` in the app manifest; it is never hard-coded in source.
- `local.properties` and `secrets.properties` are gitignored.
- `AUDIT.md` is an internal engineering audit of known issues; it is not a product changelog.

## License

All rights reserved unless a license file is added later.
