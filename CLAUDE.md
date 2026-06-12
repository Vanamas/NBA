# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CourtFlow — an NBA player catalog Android app. The original assignment (in Czech) is in `docs/goal.md`: a paginated list of NBA players (35 per page) from the [balldontlie.io](https://app.balldontlie.io/) API, with player detail and team detail screens. Kotlin, MVI, Jetpack Compose, Retrofit, Glide.

The API requires an Authorization header. The key is read from `local.properties` (`balldontlie.apiKey=...`) and injected as `BuildConfig.BALLDONTLIE_API_KEY` in `:core:network` (see its `build.gradle.kts`); `NetworkModule.kt` attaches it via an OkHttp interceptor. Without a real key the app builds but every request returns 401.

## Commands

```bash
./gradlew assembleDebug                  # build the app
./gradlew test                           # all unit tests
./gradlew :domain:testDebugUnitTest      # unit tests of one module
./gradlew :feature:players:testDebugUnitTest --tests "cz.vanama.courtflow.feature.players.list.PlayerListViewModelTest"   # single test class
./gradlew :app:testDebugUnitTest --tests "cz.vanama.courtflow.konsist.ArchitectureTest"   # Konsist architecture rules
./gradlew detekt ktlintCheck             # static analysis (see caveat below)
./gradlew :core:designsystem:verifyRoborazziDebug   # screenshot tests against goldens
./gradlew :core:designsystem:recordRoborazziDebug   # re-record goldens after intended visual changes
```

Caveat: detekt and ktlint are applied only in the root `build.gradle.kts` without a `subprojects {}` block, so they currently do **not** analyze module sources. Detekt config lives in `config/detekt/detekt.yml` (maxIssues: 0).

## Architecture

Multi-module Clean Architecture with MVI. Dependency direction (enforced by convention and partly by Konsist tests in `app/src/test/.../konsist/ArchitectureTest.kt`):

```
app ──► feature:players, feature:teams ──► domain ──► core:common
        data ──► domain, core:network
        (feature modules also use core:designsystem)
```

- **`domain`** — pure business layer: models (`Player`, `Team`), repository interfaces, `*UseCase` classes (must live in `domain.usecase` package — Konsist rule).
- **`data`** — repository implementations, DTO→domain mappers (`toDomain()` extension functions), and `PlayerPagingSource` (Paging 3, cursor-based pagination via the API's `nextCursor`).
- **`core:network`** — Retrofit `BallDontLieApi`, Moshi DTOs, OkHttp setup. Base URL: `https://api.balldontlie.io/v1/`.
- **`core:designsystem`** — shared Compose theme and components (Material 3).
- **`feature:*`** — one package per screen (`list/`, `detail/`), each with three files: `*Contract.kt`, `*Screen.kt`, `*ViewModel.kt`.
- **`app`** — `MainActivity`, navigation, Koin startup.

### MVI pattern

Each screen's `*Contract.kt` defines three types: a `*State` data class, a sealed `*Intent` class, and a sealed `*Effect` class (one-shot events like navigation). ViewModels expose `uiState: StateFlow<State>` + `uiEffect: SharedFlow<Effect>` and take input only through a single `onIntent(intent)` function. Each screen file is split into a stateful `*Screen` composable (ViewModel wiring, effects, Scaffold) and a stateless `*Content` composable (pure `state` + callbacks) — UI tests target the `*Content` variant. Follow this structure for any new screen.

### Dependency injection (Koin, not Hilt)

One Koin module per layer plus one per feature: `coreNetworkModule`, `dataModule`, `domainModule`, `playersFeatureModule`, `teamsFeatureModule` (each feature module hosts its own ViewModel bindings in a `di/` package) — all registered in `CourtFlowApplication`. The project was migrated from Hilt to Koin; do not introduce Hilt annotations. ViewModels are obtained in composables via `koinViewModel()`.

### Navigation

`app/.../navigation/NavGraph.kt` uses a simple manual back stack (`mutableStateListOf<Destination>` + `when` over the last entry) with destinations defined in `Destination.kt`. Navigation 3 / adaptive libraries are declared as dependencies but the graph itself is hand-rolled.

## Testing

Unit tests use JUnit 4 + MockK + Turbine (flow assertions) + `kotlinx-coroutines-test`; the network layer is tested with MockWebServer using JSON fixtures matching the real API envelope (`{"data": ...}`). Compose UI tests live in feature modules' `src/test` and run on the JVM via Robolectric (`@Config(sdk = [35])`, `createComposeRule`) against the stateless `*Content` composables; content below the fold needs `performScrollTo()` before asserting. Kotest assertions (`shouldBe`) are available alongside JUnit asserts. Screenshot tests use Roborazzi (chosen over Paparazzi, which is alpha-only and lags AGP releases); goldens live in `core/designsystem/src/test/screenshots` and must be committed. Screenshot content must stay deterministic — pass an empty `imageUrl`, never a real URL. Konsist tests in `:app` enforce package placement of ViewModels and UseCases — keep them passing when adding classes. Per `.agent/plan.md`, the project aims at TDD with full coverage of business logic and ViewModels.

## Conventions

- Images are loaded with **Glide Compose** (`GlideImage`), not Coil — the project was deliberately migrated.
- JSON via Moshi with KSP codegen (`moshi-kotlin-codegen`).
- All dependency versions belong in `gradle/libs.versions.toml` (version catalog); module build files reference `libs.*` aliases only.
- Package root: `cz.vanama.courtflow`, each module appends its path (e.g. `cz.vanama.courtflow.feature.players`).
