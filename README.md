# Qaza Tracker

A native Android app (Kotlin) that helps Muslims calculate, track, and
systematically complete missed (Qaza) prayers.

This is a **debt-clearing app, not a daily habit checklist** — the core
interaction is working down a large backlog over months or years, not
checking off "today's tasks." There's deliberately no calendar strip, no
streaks, and no daily-checklist UI.

## Status

Actively in development. Built bottom-up per the project's build order
(data → domain → UI, one screen at a time):

- ✅ **Data layer** — Room entities, DAOs with aggregation queries, and
  in-memory database unit tests.
- ✅ **Domain layer** — the four core use cases (baseline calculation,
  logging completions, applying adjustments, projecting a completion
  date), with unit tests covering leap-year edge cases in the baseline
  calculation.
- ✅ **Dependency injection** — Hilt wired end-to-end (application,
  modules, constructor injection).
- ✅ **Onboarding screen** — baseline entry (exact-dates vs. age-estimate
  toggle), wired to a Hilt-injected ViewModel, shows the calculated
  result on-screen.
- ⏳ **Emulator / visual verification** — not yet run on a device or
  emulator in this environment; build and tests pass, but the UI hasn't
  been eyeballed yet.
- ⏳ **Remaining screens** — baseline summary, main dashboard, batch
  logging, history/ledger — in progress, one at a time.

## Tech Stack

- **Kotlin**, **Jetpack Compose**, **Material 3**
- **Architecture**: MVVM, Repository pattern, a lightweight domain layer
  built from UseCases
- **Room** (+ Room KTX) for persistence — the sole source of truth,
  fully offline
- **Hilt** for dependency injection
- **Kotlin Coroutines / Flow** throughout
- Target: **min SDK 26** (Android 8.0), for `java.time` without
  desugaring

## Architecture Notes

### Event-sourced data model

The most important thing to understand about this codebase: **the
"remaining prayers" count is never stored** as a mutable field anywhere.
It's always derived from three append-only tables:

- `BaselineSnapshot` — one row per prayer type, the starting count from
  either an exact date range or an age-based estimate.
- `AdjustmentLog` — a log of manual corrections, recalculations, and
  (reserved for a future release) exemption periods. Each row has a
  signed `delta`, so entries can increase or decrease the count.
- `CompletionLog` — one row per prayer actually completed.

The remaining count per prayer type is always computed as:

```
remaining = baseline.initialCount + SUM(AdjustmentLog.delta) - COUNT(CompletionLog)
```

This is computed via a Room `Flow` aggregation query, never cached or
written back. The payoff: a full audit trail, safe undo, and a UI that
can always explain "why did my number change" by pointing at the
underlying log rows — instead of trusting a counter that could drift out
of sync with reality.

### Domain layer independence

The domain layer (use cases, `CalculationMethod`/`PrayerType`/
`AdjustmentReason` enums, etc.) has no Android or Room dependency and is
unit-tested as plain Kotlin/JUnit. Date math uses `java.time`
(`LocalDate`, `ChronoUnit.DAYS.between()`) throughout rather than
hand-rolled `years * 365` arithmetic, specifically because that
approximation breaks on leap years — see the leap-year test cases in
`CalculateBaselineUseCaseTest`.

## How to Build

1. Open the project root in Android Studio (a recent version with
   support for AGP 9.x / Kotlin 2.2.x).
2. Let Gradle sync — it will pull Room, Hilt, and Compose dependencies
   via the version catalog (`gradle/libs.versions.toml`).
3. Run the `app` configuration on an emulator or physical device
   (min SDK 26 / Android 8.0+).

From the command line:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## Known Gaps

- **No emulator/AVD has been used to verify the UI yet** in this
  development environment — the app builds and all unit/instrumented
  test sources compile, but nobody has looked at the onboarding screen
  running on an actual device.
- **Dark theme colors are derived, not sourced from the design import.**
  The Claude Design handoff only defined a light palette; the dark
  `ColorScheme` in `Theme.kt` is a same-hue variant put together for
  parity, not part of the original spec.
- **Custom fonts aren't bundled yet.** The design calls for Caprasimo
  (headings) and Figtree (body) via Google Fonts; no `.ttf` files have
  been added to the project, so the type scale currently renders in the
  platform default font.

## Full Project Context

This repo is developed against a local `CLAUDE.md` project-instructions
file (not published here) covering the complete set of scope decisions,
target stack details, the full data schema, screen-by-screen design
direction, and things intentionally being kept out of v1 (no calendar
UI, no adhan/prayer-time features, no login system, etc.). Ask the
maintainer if you need that context.
