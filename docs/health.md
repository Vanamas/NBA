# Project Health — 2026-06-11

## Summary
Architektura a hygiena kódu jsou nadprůměrné (čistá multi-modulová Clean Architecture, MVI, 0 TODO, commitnuté screenshot goldeny, dobré README). Projekt má ale dva kritické problémy: **build je rozbitý** (androidx.core 1.19.0 vyžaduje compileSdk 37, všech 8 modulů má 36 — neprojde ani assemble, ani unit testy) a **projekt není pod gitem** (žádná historie, žádný rollback).

## Build & tests
- `./gradlew assembleDebug` → **BUILD FAILED**: `checkDebugAarMetadata` — `androidx.core:core(-ktx):1.19.0` vyžaduje compileSdk ≥ 37; všech 8 modulů deklaruje `compileSdk = 36`. SDK platform `android-37.0` je lokálně nainstalovaný, oprava je tedy levná.
- `./gradlew test` → **BUILD FAILED** ze stejného důvodu (`checkDebugUnitTestAarMetadata`). 18 testovacích souborů existuje, ale počet testů teď nelze změřit.
- Žádné `@Ignore` testy. CI neexistuje (není ani git repo).

## Dependencies
- Verze jsou čerstvé (Kotlin 2.2.10, AGP 9.2.1, Gradle 9.4.1). Right-edge update androidx.core 1.19.0 právě rozbil build — viz výše.
- Gradle hlásí deprecation warnings („incompatible with Gradle 10") — neblokující.

## Code quality
- 0 výskytů TODO/FIXME/HACK v Kotlin zdrojích.
- **detekt a ktlint reálně neanalyzují žádný modul** — pluginy jsou aplikované jen v root `build.gradle.kts` bez `subprojects {}` (potvrzeno v CLAUDE.md). „0 issues" je falešný signál.
- `compileSdk` (a spol.) je zduplikovaný v 8 build souborech — chybí convention plugins.

## Structure & architecture
- Čistá závislostní hierarchie vynucovaná Konsist testy. Bez výhrad.
- Secrets hygiena OK: API klíč v `local.properties` (ignorován), žádné hardcoded klíče.

## Repo hygiene
- **Není git repozitář.** `.gitignore` připravený a rozumný, ale `git init` nikdy neproběhl. Veškerá dosavadní práce je bez historie a bez zálohy.

## Docs & DX
- README kvalitní (setup, API klíč, architektura). CLAUDE.md aktuální a poctivý. `docs/goal.md` = zadání.
- Jediná lokalizace (`values/`), strings jen v `:app` — pro rozsah úlohy OK.

## Metrics (pro mezicyklové srovnání)
- test count: **40** (0 selhání, 0 skipped; 17 tříd, testDebugUnitTest) — změřeno po opravě buildu v cyklu 1
- lint/detekt issues: 0 (ale nástroje moduly nepokrývají — falešná nula)
- outdated deps: 0 známých
- locale parity: 1/1
- compileSdk duplikace: 8 souborů (nyní všechny 37)

## Risks & tech debt
1. Rozbitý build blokuje veškerou další práci (P0).
2. Absence gitu = jakákoli chyba je nevratná (P0).
3. Detekt/ktlint nepokrývají moduly — statická analýza je iluze (P1).
4. Duplikace build konfigurace v 8 modulech (P2).

## Suggested next moves
1. Opravit build: `compileSdk 36 → 37` ve všech 8 modulech, ověřit assemble + testy.
2. `git init` + initial commit — zafixovat funkční stav.
3. Rozšířit detekt/ktlint na všechny moduly a srovnat reálný počet issues.

## Nezkontrolováno
- Instrumentované testy / běh na zařízení, velikost APK, release/R8 konfigurace.
