# CourtFlow 🏀

An NBA player catalog Android app built with Kotlin, Jetpack Compose and a multi-module Clean Architecture. Browse NBA players in an endlessly scrolling list, open a player's detail and jump straight to the detail of the team they play for.

Player data is provided by the [balldontlie](https://app.balldontlie.io/) API.

## Features

- **Player list** — paginated list of NBA players (35 records per page, loaded automatically as you scroll) showing name, position and current team
- **Player search** — debounced search field backed by the API's `search` query parameter
- **Player detail** — full player information with a link to their team
- **Team list** — all 30 NBA teams, reachable from the player list top bar
- **Team detail** — team information (city, abbreviation, conference, division), cached in memory so it works offline after the first load
- **Error handling** — failed first loads show a message with a Retry button instead of a blank screen

## Images

The balldontlie API returns **no images** — no player photos and no team logos, in any pricing tier (verified against both the docs and the live API). The app therefore generates deterministic placeholder art via [DiceBear](https://www.dicebear.com) (free, keyless): the player/team **id is used as the generation seed**, so the same entity always renders the same avatar/emblem.

Images are loaded with **Glide Compose** with a loading placeholder icon, an error-state icon and a circular crop. Screenshot tests pass an empty `imageUrl` so goldens stay deterministic.

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVI, multi-module Clean Architecture |
| Dependency injection | [Koin](https://insert-koin.io/) |
| Networking | Retrofit + OkHttp + Moshi |
| Pagination | AndroidX Paging 3 |
| Images | Glide Compose + [DiceBear](https://www.dicebear.com) placeholders |
| Static analysis | Detekt, Ktlint, [Konsist](https://docs.konsist.lemonappdev.com/) |
| Testing | JUnit 4, MockK, Turbine, MockWebServer, Robolectric, Roborazzi (screenshots), Kotest assertions |

## Architecture

```
app ─────► feature:players ──► domain ──► core:common
     ────► feature:teams   ──► core:designsystem
     ────► data ──► domain, core:network
```

- **`domain`** — business models, repository interfaces and use cases (pure Kotlin logic)
- **`data`** — repository implementations, DTO→domain mappers, Paging 3 sources
- **`core:network`** — Retrofit API definition and DTOs for the balldontlie API
- **`core:designsystem`** — shared Material 3 theme and reusable components
- **`feature:*`** — one module per feature; each screen follows the MVI contract pattern (`State` / `Intent` / `Effect`)

## Getting started

### Prerequisites

- Android Studio (latest stable)
- JDK 11+
- A free API key from [balldontlie.io](https://app.balldontlie.io/)

### Setup

1. Clone the repository and open it in Android Studio.
2. Add your API key to `local.properties` in the project root:

   ```properties
   balldontlie.apiKey=YOUR_API_KEY
   ```

   The key is injected into `BuildConfig` of the `:core:network` module at build time.

> [!IMPORTANT]
> Without a valid API key the app builds fine but every request returns `401 Unauthorized`, so all lists stay empty.

3. Build and run:

   ```bash
   ./gradlew assembleDebug
   ```

## Development

```bash
./gradlew test                                       # run all unit tests
./gradlew :domain:testDebugUnitTest                  # run tests of a single module
./gradlew detekt ktlintCheck                         # static analysis
./gradlew :core:designsystem:verifyRoborazziDebug    # screenshot tests against goldens
./gradlew :core:designsystem:recordRoborazziDebug    # re-record goldens after intended visual changes
```

Architecture rules (package placement of ViewModels and UseCases) are enforced by Konsist tests:

```bash
./gradlew :app:testDebugUnitTest --tests "cz.vanama.courtflow.konsist.ArchitectureTest"
```
