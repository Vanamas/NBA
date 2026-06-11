# Roadmap

## P0
- [backlog] Opravit build — compileSdk 36 → 37 ve všech 8 modulech (androidx.core 1.19.0 to vyžaduje; SDK 37 je nainstalované)
- [backlog] git init + initial commit — projekt nemá žádnou verzovací historii, .gitignore už existuje

## P1
- [backlog] Rozšířit detekt + ktlint na všechny moduly (dnes analyzují jen root → falešná nula) a vyřešit nalezené issues
- [backlog] Změřit a zapsat baseline metriky (počet testů, detekt issues) po opravě buildu

## P2
- [backlog] Zavést convention plugins — compileSdk/minSdk/targetSdk je zduplikované v 8 build souborech
- [backlog] Prošetřit Gradle deprecation warnings („incompatible with Gradle 10")
- [backlog] Zvážit CI (GitHub Actions: build + test + detekt) až bude repo na GitHubu
