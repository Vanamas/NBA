# Autopilot log

## [2026-06-11 21:34] audit | První health check projektu
- Vytvořena wiki (log.md, roadmap.md, health.md) se souhlasem uživatele.
- Build FAILED: androidx.core 1.19.0 vyžaduje compileSdk 37, všech 8 modulů má 36; blokuje i unit testy. SDK 37 je lokálně nainstalované.
- Projekt není git repozitář (.gitignore ale existuje a je rozumný).
- Kvalita jinak vysoká: 0 TODO, 18 test souborů, čistá Clean Architecture s Konsist vynucením, kvalitní README.
- Detekt/ktlint nepokrývají moduly (jen root) — „0 issues" je falešná nula.
- Metrics: test count = neměřitelné (build rozbitý); lint issues = 0 (falešná); outdated deps = 0; locale parity = 1/1.
- Next: cyklus 1 — opravit compileSdk, git init + commit, změřit baseline.

## [2026-06-11 21:38] cycle | Cycle 1 — oprava buildu, git init, baseline metriky
- compileSdk 36 → 37 ve všech 8 modulech; `./gradlew assembleDebug test` → BUILD SUCCESSFUL (33 s).
- Opraven .gitignore (`/build` → `build/` — modulové build adresáře se stagovaly; přidáno .kotlin/, .remember/, .maestro/).
- git init (main), initial commit 3c9b377 (129 souborů), remote origin = github.com/Vanamas/NBA (dodal uživatel), pushnuto; zapnuto delete-branch-on-merge dle globálních instrukcí.
- Metrics: test count = 40 (0 failures, 0 skipped, 17 tříd); lint issues = 0 (falešná nula — nepokrývá moduly); outdated deps = 0; locale parity = 1/1.
- Next: P1 — rozšířit detekt/ktlint na všechny moduly a srovnat reálný počet issues.

## [2026-06-11 21:58] cycle | Cycle 2 — detekt/ktlint na všech modulech, vše opraveno
- Root build.gradle.kts: subprojects { detekt + ktlint }, detekt config ukotven na rootProject config/detekt/detekt.yml.
- Naměřeno před opravou: 83 detekt issues + ~431 ktlint prohřešků (do té doby neviditelné — falešná nula).
- ktlintFormat opravil ~95 % mechanicky; .editorconfig přidán (ktlint_function_naming_ignore_when_annotated_with=Composable).
- detekt.yml (schváleno uživatelem): Compose-aware ignoreAnnotated (Composable/Preview*) u FunctionNaming, UnusedPrivateMember, LongParameterList, MagicNumber + ignorePropertyDeclaration (hex barvy).
- Error handling (schváleno, reálná oprava): nová domain.error.DataException; data vrstva mapuje IOException/HttpException přes safeApiCall; PlayerPagingSource chytá konkrétní typy; ViewModely catch(DataException) — generic catch už nepolyká CancellationException. Do :data přidán implementation(libs.retrofit) (zviditelnění už používané knihovny kvůli HttpException).
- Smazány template ExampleUnitTest.kt + ExampleInstrumentedTest.kt (schváleno).
- Verifikace: ./gradlew detekt ktlintCheck test assembleDebug → BUILD SUCCESSFUL.
- Metrics: test count = 39 (0 failures; −1 za smazaný template test); detekt+ktlint issues = 0 (reálná nula, pokrývá všech 8 modulů); outdated deps = 0; locale parity = 1/1.
- Next: P2 — convention plugins (duplikace build konfigurace), CI na GitHub Actions.

## [2026-06-11 22:05] cycle | Cycle 3 — CI na GitHub Actions, prošetření Gradle deprecations
- Vytvořen .github/workflows/ci.yml: detekt + ktlintCheck → test → assembleDebug na push/PR do main; ubuntu-latest, Temurin 21, gradle/actions/setup-gradle (cache), concurrency cancel-in-progress, upload test reportů při failu.
- Ověřeno, že build projde bez local.properties (CI simulace s ANDROID_HOME): :core:network má fallback getProperty(key, "") — žádná úprava buildu nebyla potřeba.
- Gradle deprecations (--warning-mode all + deprecation.trace): jediný nález — ReportingExtension.file(String) z pluginu detekt 1.23.8 (DetektPlugin.apply), odstranění v Gradle 10. Náš kód čistý; oprava = upgrade detektu (gatováno, do backlogu).
- Roborazzi verify v CI záměrně neběží (riziko cross-OS rozdílů goldens macOS vs. Linux) — do backlogu.
- Verifikace: ./gradlew detekt ktlintCheck test assembleDebug → BUILD SUCCESSFUL; první CI běh zelený (run 27374131290).
- Follow-up v rámci cyklu: GitHub anotace o Node 20 deprecation (vynucený přechod na Node 24 už 16. 6. 2026) → bump actions: checkout v6, setup-java v5, setup-gradle v6, upload-artifact v7 (verze ověřeny přes gh api releases/latest).
- Metrics: test count = 39 (0 failures); detekt+ktlint issues = 0; outdated deps = 0; locale parity = 1/1; Gradle deprecations = 1 (třetí strana).
- Next: P2 — convention plugins (s CI už bezpečnější), případně detekt 2.x upgrade.
