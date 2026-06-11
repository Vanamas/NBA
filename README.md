# CourtFlow 🏀

An NBA player catalog Android app built with Kotlin, Jetpack Compose and a multi-module Clean Architecture. Browse NBA players in an endlessly scrolling list, open a player's detail and jump straight to the detail of the team they play for.

Player data is provided by the [balldontlie](https://app.balldontlie.io/) API.

## Features

- **Player list** — paginated list of NBA players (35 records per page, loaded automatically as you scroll) showing name, position and current team
- **Player detail** — full player information with a link to their team
- **Team detail** — team information (city, abbreviation, conference, division)

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVI, multi-module Clean Architecture |
| Dependency injection | [Koin](https://insert-koin.io/) |
| Networking | Retrofit + OkHttp + Moshi |
| Pagination | AndroidX Paging 3 |
| Images | Glide Compose |
| Static analysis | Detekt, Ktlint, [Konsist](https://docs.konsist.lemonappdev.com/) |
| Testing | JUnit 4, MockK, Turbine, MockWebServer |

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
./gradlew test                           # run all unit tests
./gradlew :domain:testDebugUnitTest      # run tests of a single module
./gradlew detekt ktlintCheck             # static analysis
```

Architecture rules (package placement of ViewModels and UseCases) are enforced by Konsist tests:

```bash
./gradlew :app:testDebugUnitTest --tests "cz.vanama.courtflow.konsist.ArchitectureTest"
```
