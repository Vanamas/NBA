# Roadmap

## P0
- [done] Opravit build — compileSdk 36 → 37 ve všech 8 modulech (androidx.core 1.19.0 to vyžaduje; SDK 37 je nainstalované) (cycle 1)
- [done] git init + initial commit — navíc připojen remote github.com/Vanamas/NBA, pushnuto, zapnuto delete-branch-on-merge (cycle 1)

## P1
- [done] Rozšířit detekt + ktlint na všechny moduly a vyřešit nalezené issues — 83+431 nálezů opraveno, 0 zbývá; bonus: DataException error model, CancellationException bug fix (cycle 2)
- [done] Změřit a zapsat baseline metriky po opravě buildu — 40 testů / 0 failures (cycle 1)

## P2
- [backlog] Zavést convention plugins — compileSdk/minSdk/targetSdk je zduplikované v 8 build souborech
- [done] Prošetřit Gradle deprecation warnings — jediný nález: ReportingExtension.file z detekt pluginu 1.23.8; náš kód čistý (cycle 3)
- [done] Zvážit CI (GitHub Actions: build + test + detekt) — ci.yml: lint + testy + build na push/PR do main (cycle 3)
- [backlog] Upgrade detekt na verzi bez ReportingExtension.file deprecation (detekt 2.x) — blokuje budoucí Gradle 10; změna závislosti = gatováno
- [backlog] Zvážit Roborazzi verify v CI — nutno ověřit determinismus goldens mezi macOS (lokál) a Linux (CI)
