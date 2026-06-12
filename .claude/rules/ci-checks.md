# Pre-push CI checks

Before every commit/push, run the exact checks CI runs (`.github/workflows/ci.yml`):

```bash
./gradlew detekt ktlintCheck test assembleDebug
```

Plus `./gradlew :core:designsystem:verifyRoborazziDebug` whenever UI in
`core:designsystem` changed (re-record with `recordRoborazziDebug` first if the
change is intentional, and commit the goldens).

Never push after running tests alone — detekt and ktlint are applied to every
module via `subprojects {}` and CI fails on any finding (`maxIssues: 0`).

Common detekt traps in this repo:

- **LongMethod (max 60 lines)** — split large composables into private
  sub-composables instead of suppressing.
- **MatchingDeclarationName** — a file whose only top-level class/enum doesn't
  match the file name fails; give the declaration its own file.
- ktlint findings are auto-fixable with `./gradlew ktlintFormat`.
