# Qaza Tracker

A native Android app (Kotlin) that helps Muslims calculate, track, and
systematically complete missed (Qaza) prayers.

This is a **debt-clearing app, not a daily habit checklist** — the core
interaction is working down a large backlog over months or years, not
checking off "today's tasks." There's deliberately no calendar strip and no
daily-checklist UI. The dashboard shows one small consecutive-days streak,
derived from the completion log and worded gently (a lapse reads as "log a
prayer today to start a streak", never as a failure).

## Status

All five core screens from the build order are implemented and have been
live-verified on a Pixel 8 emulator (API 37), plus two additions beyond
the original screen list:

- ✅ **Data layer** — Room entities, DAOs with aggregation queries, and
  in-memory database unit tests.
- ✅ **Domain layer** — the core use cases (baseline calculation, logging
  completions, applying adjustments, projecting a completion date,
  scheduling notifications), with unit tests covering leap-year edge
  cases in the baseline calculation.
- ✅ **Dependency injection** — Hilt wired end-to-end (application,
  modules, constructor injection, `@HiltWorker`).
- ✅ **Onboarding** — baseline entry (exact-dates vs. age-estimate
  toggle).
- ✅ **Baseline summary** — calculated totals per prayer type, editable
  before confirming.
- ✅ **Dashboard** — total remaining count, per-prayer progress rows, an
  estimated completion date (colour-coded by how far off it is, with a
  timeline and a tap-through pace breakdown), quick single-tap logging,
  manual adjustments, and a brief celebration card when the last missed
  prayer of a type is completed.
- ✅ **Batch logging** — multi-day, multi-prayer-type logging in one
  action.
- ✅ **History/ledger** — a merged, batch-collapsing timeline of
  completions, adjustments and milestones.
- ✅ **Statistics** *(beyond the original screen list)* — per-month
  completions for each prayer type as a bar chart and progress rows, with
  previous/next month navigation. Reachable from the chart icon on the
  Dashboard.
- ✅ **Achievements** *(beyond the original screen list)* — a trophy-icon
  screen grouping what's been unlocked: prayer-type milestones, the current
  and best streak, and 25/50/75/100% progress badges, each with the day it
  was unlocked. Derived from the existing logs, so nothing extra is stored.
- ✅ **Home-screen widget** *(beyond the original screen list)* — a compact
  3x3 widget with all five prayer types, what's left of each, and a +1
  button per row; it logs through the same use case as the app and stays in
  sync in both directions via the shared database.
- ✅ **Daily inspiration** *(beyond the original screen list)* — a verse and a hadith for each day
  from a built-in offline library (32 of each), with Previous/Next/Random browsing, share and
  copy, and a Saved tab. The day's pick is derived from the date, so it stays put all day and
  changes tomorrow; only bookmarks are stored (database v3, additive migration).
- ✅ **Settings** *(beyond the original screen list)* — notification
  frequency (DataStore Preferences-backed), a full-data JSON export,
  app version, and an About block. Reachable via a gear icon next to
  History on the Dashboard.
- ✅ **Reminder notifications** *(beyond the original screen list)* —
  WorkManager-scheduled reminders (Never/Weekly/Bi-weekly/Daily) tied to
  the Settings preference, rescheduled immediately on change, with a
  one-time `POST_NOTIFICATIONS` prompt on API 33+.
- ✅ **Custom fonts** — Caprasimo (headings) and Figtree (body) bundled
  as real `FontFamily` resources.
- ⏳ **Ad monetization** — not started; see Known Gaps below.

## Tech Stack

- **Kotlin**, **Jetpack Compose**, **Material 3**
- **Architecture**: MVVM, Repository pattern, a lightweight domain layer
  built from UseCases
- **Room** (+ Room KTX) for persistence — the sole source of truth,
  fully offline
- **Hilt** for dependency injection, including `androidx.hilt:hilt-work`
  for injecting WorkManager workers
- **DataStore Preferences** for app settings (notification frequency,
  a one-time permission-prompt flag) — Room remains the sole source of
  truth for prayer data itself
- **WorkManager** for periodic reminder notifications
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

A fourth append-only table, `prayer_milestones` (added in database v2), records
each moment every missed prayer of a type was made up. It's a log of something
that happened, not a counter, and it isn't part of the formula below. The v1→v2
migration is purely additive and covered by a test that upgrades a real v1
database and checks nothing was lost.

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

### Notification scheduling

`ScheduleNotificationsUseCase` persists the chosen frequency to
DataStore *and* (re)schedules a `PeriodicWorkRequest` in one step, so
the two can never drift apart. A `NotificationScheduler` domain
interface keeps WorkManager entirely out of the domain layer — the real
implementation (`WorkManagerNotificationScheduler`) lives under
`notification/`, alongside the worker itself and a pure, unit-tested
`NotificationFrequency -> Duration` mapping. On every cold start,
`QazaTrackerApp` re-applies whatever frequency is currently stored, so a
fresh install's default (Weekly) is actually scheduled without the user
having to open Settings first.

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

- **Ad monetization isn't implemented.** CLAUDE.md scopes the app as
  free with light ads, gated behind an `AdProvider` interface so SDK
  calls never touch domain/data code directly — that interface doesn't
  exist yet.
- **Dark theme colors are derived, not sourced from the design import.**
  The Claude Design handoff only defined a light palette; the dark
  `ColorScheme` in `Theme.kt` is a same-hue variant put together for
  parity, not part of the original spec.
- **No runtime-permission rationale UI.** The one-time
  `POST_NOTIFICATIONS` prompt (API 33+) uses the system dialog directly
  with no explanatory screen beforehand; denying it just means reminder
  notifications silently never fire — nothing else in the app is
  affected.
- **Female exemption periods are reserved but not exposed.** Per
  CLAUDE.md, `AdjustmentReason.EXEMPTION` exists in the schema so this
  can be added later without a data migration, but there's no v1 UI for
  it.

## Full Project Context

This repo is developed against a local `CLAUDE.md` project-instructions
file (not published here) covering the complete set of scope decisions,
target stack details, the full data schema, screen-by-screen design
direction, and things intentionally being kept out of v1 (no calendar
UI, no adhan/prayer-time features, no login system, etc.). Ask the
maintainer if you need that context.
