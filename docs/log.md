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
