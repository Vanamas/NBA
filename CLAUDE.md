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
./gradlew detekt ktlintCheck             # static analysis (runs in every module)
./gradlew :core:designsystem:verifyRoborazziDebug   # screenshot tests against goldens
./gradlew :core:designsystem:recordRoborazziDebug   # re-record goldens after intended visual changes
./gradlew :core:network:openApiGenerate             # (re)generate the API client from the OpenAPI spec
./gradlew :core:network:updateApiSpec               # refresh the committed spec snapshot from balldontlie.io
./gradlew :app:generateReleaseBaselineProfile       # (re)generate the Baseline Profile on a managed device
```

detekt and ktlint are applied to **all modules** via `subprojects {}` in the root `build.gradle.kts`; config lives in `config/detekt/detekt.yml` (maxIssues: 0). The mandatory pre-push check sequence is defined in `.claude/rules/ci-checks.md`.

## Architecture

Multi-module Clean Architecture with MVI. Dependency direction (enforced by convention and partly by Konsist tests in `app/src/test/.../konsist/ArchitectureTest.kt`):

```
app ──► feature:players, feature:teams ──► domain
        feature:settings ──► core:common, core:designsystem
        data ──► domain, core:network
        (feature modules also use core:designsystem; app, data, the
        feature modules and core:designsystem share cross-cutting
        primitives from core:common)
        baselineprofile ──► app (com.android.test producer module)
```

- **`domain`** — pure business layer: models (`Player`, `Team`, `Game`), repository interfaces (incl. `GameRepository`), `*UseCase` classes such as `GetTeamGamesUseCase` (must live in `domain.usecase` package — Konsist rule). Failure classification (`DataErrorKind`/`DataException`) lives in `core:common`, not here.
- **`core:common`** — shared kernel used across layers: `error` (`DataErrorKind`, `DataException` — the cross-layer failure contract thrown by `data` and mapped to messages in `core:designsystem`; rate-limit (429) failures also carry the `x-ratelimit-reset` epoch for retry scheduling), `connectivity` (`ConnectivityObserver` + Android implementation), `time` (`countdownSeconds` plus `RateLimitRetryController` — a per-ViewModel countdown that auto-retries after an HTTP 429 using the `x-ratelimit-reset` header, shared by the players/teams list and detail ViewModels), `settings` (DataStore-backed **user-preference stores** — e.g. `ThemePreferencesStore`; these are a deliberate cross-cutting concern here, named `*Store` not `*Repository`, and are **not** domain repositories — single-feature stores do not belong in `core:common`), and the `coreCommonModule` Koin module.
- **`data`** — repository implementations, DTO→domain mappers (`toDomain()` extension functions), `PlayerPagingSource` (Paging 3, cursor-based pagination via the API's `nextCursor`), and a `data/local` package (`CourtFlowDatabase`, `PlayerEntity`/`TeamEntity`/`RemoteKeyEntity`/`CacheMetadataEntity`/`GameEntity`, DAOs) with entity↔domain mappers and `PlayerRemoteMediator`. **All cached reads are offline-first with a uniform 1-day TTL**: a single `cache_metadata` table (`CacheMetadataEntity`/`CacheMetadataDao`) stores `lastFetchedAt` per logical resource keyed by `CacheKeys` (`players`, `teams`, `player:{id}`, `games:{teamId}`), and `CachePolicy.isStale(lastFetchedAt, now)` is the staleness rule (both in the `data.cache` package). Each read serves Room while fresh, refetches when stale or on pull-to-refresh, and falls back to the cache on a failed refresh — the player list via `PlayerRemoteMediator.initialize()` (`SKIP`/`LAUNCH_INITIAL_REFRESH`), the team list (`getTeams`), player detail (`getPlayerById`, reusing `PlayerEntity`), team detail (`getTeamById`, freshness shared with the `teams` key) and recent games (`GameRepositoryImpl`, 45-day window, `games:{teamId}`). **Player search and team rosters stay network-only** via `PlayerPagingSource`. The TTL clock is an injectable `nowMillis: () -> Long = System::currentTimeMillis` (so TTL is deterministically testable); repos that take it are bound manually in Koin (see DI note).
- **`core:network`** — OkHttp/Retrofit setup; the Retrofit interface (`NBAApi`) and Moshi models are **generated by OpenAPI Generator** from the official balldontlie definition (`core/network/openapi/nba.yml`, a committed snapshot of `https://www.balldontlie.io/openapi/nba.yml` — refresh with `./gradlew :core:network:updateApiSpec`) into `build/generated/openapi` (package `...core.network.generated`) — edit the spec, never the output. Generation runs automatically before compile/KSP/ktlint tasks (`openApiGenerate`); ktlint skips the generated sources via a filter. The official spec marks all model fields optional, so `data` mappers enforce non-null invariants (`requireNotNull` for ids, `orEmpty()` for texts). Base URL: `https://api.balldontlie.io/` via `BuildConfig.BALLDONTLIE_BASE_URL` (overridable per build type/flavor; generated paths already carry the `nba/v1/` prefix).
- **`core:designsystem`** — shared Compose theme and components (Material 3).
- **`feature:*`** — three feature modules: `feature:players` and `feature:teams` (each with `list/` and `detail/` packages) and `feature:settings` (a single appearance + per-app-language screen). Each screen has three core files: `*Contract.kt`, `*Screen.kt`, `*ViewModel.kt`, plus optional internal helper-composable files (e.g. `TeamRoster.kt`). `feature:settings` depends only on `core:common` (for `ThemePreferencesStore`/`ThemeMode`) and `core:designsystem`, not `domain`.
- **`app`** — `MainActivity`, navigation, Koin startup.
- **`baselineprofile`** — a `com.android.test` producer module that drives `:app` through a startup + scroll journey on a Gradle Managed Device (`pixel6Api34`, headless emulator), records the AOT-compile hints and emits the Baseline Profile consumed by `:app` (`baselineProfile(project(":baselineprofile"))` + `androidx.profileinstaller`). `BaselineProfileGenerator` records it (`./gradlew :app:generateReleaseBaselineProfile`); `ScrollBenchmark` measures cold-start frame timing with/without the profile.

### MVI pattern

Each screen's `*Contract.kt` defines three types: a `*State` data class, a sealed `*Intent` class, and a sealed `*Effect` class (one-shot events like navigation). ViewModels expose `uiState: StateFlow<State>` + `uiEffect: SharedFlow<Effect>` and take input only through a single `onIntent(intent)` function. The initial data load happens in the ViewModel's `init` (no `Load*` intent; failures are recovered via a `Retry` intent); detail ViewModels take the entity id as a constructor parameter injected with Koin `parametersOf` from the screen. Screens collect `uiState` with `collectAsStateWithLifecycle()` and `uiEffect` inside `repeatOnLifecycle(STARTED)`. Each screen file has three layers: a **public** stateful `*Screen` composable (ViewModel wiring, effects), an **internal** stateless `*Screen` overload (Scaffold + top bar, pure params) and an **internal** stateless `*Content` composable (pure `state` + callbacks) — UI tests target the `*Content` variant, previews render the stateless `*Screen` overload and use `@PreviewLightDark` (+ `@PreviewScreenSizes` for the success state). All other composables in the file are `private`. Follow this structure for any new screen.

### Dependency injection (Koin, not Hilt)

One Koin module per layer plus one per feature: `coreCommonModule`, `coreNetworkModule`, `dataModule`, `domainModule`, `playersFeatureModule`, `teamsFeatureModule`, `settingsFeatureModule` (each feature module hosts its own ViewModel bindings in a `di/` package), plus the app-level `appModule` — all registered in `CourtFlowApplication`. `dataModule` also provides the Room database (destructive migration — the cache is always refetchable) and the DAOs (`TeamDao`, `CacheMetadataDao`, `GameDao`). Repositories that take the defaulted `nowMillis: () -> Long` clock (`Player`/`Team`/`GameRepositoryImpl`) are bound manually with `single<...> { ...Impl(get(), ...) }` rather than the constructor DSL — `singleOf`/`constructor DSL` would try to resolve the `() -> Long` from the graph and fail. The project was migrated from Hilt to Koin; do not introduce Hilt annotations. ViewModels are obtained in composables via `koinViewModel()`.

### Navigation

`app/.../navigation/NavGraph.kt` uses Navigation 3: a `NavDisplay` over `rememberNavBackStack` with `@Serializable` `Destination` keys (`Destination.kt`, they implement `NavKey`). The back stack survives rotation/process death; per-entry decorators keep saved state (scroll positions) and scope ViewModels to their destination. The `NavDisplay` sits inside a `NavigationSuiteScaffold` with two top-level destinations (Players, Teams — bottom bar on phones, navigation rail on larger windows); the selected item is derived from the top of the back stack, which always stays rooted at `PlayerList` (the Teams item does pop-to-root + `add(TeamList)`). A `ListDetailSceneStrategy` renders list and detail side by side on expanded windows — each section has its own `sceneKey` (`"players"`/`"teams"`), without which the strategy would merge both lists into one scene and pick the wrong detail placeholder. Detail screens take a `showBackButton` flag and hide their back arrow when shown in a pane next to their list. `Destination.Settings` is a pushed (non-top-level) destination opened from the list top bar. Deep links live in `DeepLink.kt` (scheme `courtflow://`, hosts `player/{id}`, `team/{id}`, `players`, `teams`, `settings`); `MainActivity` seeds the back stack via `DeepLink.initialBackStack`, static launcher shortcuts (Players, Teams only) are declared in `res/xml/shortcuts.xml`, and parsing is covered by `DeepLinkTest`. Navigation flows are covered by `CourtFlowNavGraphTest` (Robolectric + Koin-mocked use cases).

## Testing

Unit tests use JUnit 4 + MockK + Turbine (flow assertions) + `kotlinx-coroutines-test`; the network layer is tested with MockWebServer using JSON fixtures matching the real API envelope (`{"data": ...}`). Compose UI tests live in feature modules' `src/test` and run on the JVM via Robolectric (`@Config(sdk = [35])`, `createComposeRule`) against the stateless `*Content` composables; content below the fold needs `performScrollTo()` before asserting. Kotest assertions (`shouldBe`) are available alongside JUnit asserts. Screenshot tests use Roborazzi (chosen over Paparazzi, which is alpha-only and lags AGP releases); goldens live in `core/designsystem/src/test/screenshots` and must be committed. Screenshot content must stay deterministic — pass an empty `imageUrl`, never a real URL. Room DAOs are tested with Robolectric against an in-memory database; `PlayerRemoteMediatorTest` covers the mediator, and repository paging tests use `androidx.paging:paging-testing` (`asSnapshot`). TTL read-through repos are tested by passing a controllable `nowMillis = { now }` clock and advancing `now` across calls (fresh-serves-cache / stale-refetches / failed-refresh-serves-stale); a repo that reads Room on a `setQueryCoroutineContext(testDispatcher)` database must drive tests with `runTest(testDispatcher)`, never `runBlocking` (a test-dispatcher-bound Room query never resumes under `runBlocking`). Konsist tests in `:app` enforce package placement of ViewModels and UseCases — keep them passing when adding classes. The project aims at TDD with full coverage of business logic and ViewModels.

## Conventions

- Images are loaded with **Glide Compose** (`GlideImage`), not Coil — the project was deliberately migrated.
- JSON via Moshi with KSP codegen (`moshi-kotlin-codegen`).
- All dependency versions belong in `gradle/libs.versions.toml` (version catalog); module build files reference `libs.*` aliases only.
- Library modules apply the `courtflow.android.library` convention plugin (from the `build-logic` included build) instead of declaring `compileSdk`/`minSdk`/test runner themselves — shared Android config lives in `build-logic/src/main/kotlin/AndroidLibraryConventionPlugin.kt`.
- Package root: `cz.vanama.courtflow`, each module appends its path (e.g. `cz.vanama.courtflow.feature.players`).
