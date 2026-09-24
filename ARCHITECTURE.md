# Qaza Tracker — Low-Level Architecture

## Package Structure

```
com.zilehasnain.qazatracker/
├── MainActivity.kt                    # App entry point, Compose root
├── QazaTrackerApp.kt                  # @HiltAndroidApplication
├── QazaNavHost.kt                     # All screen routes & navigation
│
├── data/
│   ├── local/
│   │   ├── QazaDatabase.kt            # Room database, all entities
│   │   ├── dao/
│   │   │   ├── BaselineSnapshotDao.kt
│   │   │   ├── AdjustmentLogDao.kt
│   │   │   ├── CompletionLogDao.kt
│   │   │   └── PrayerLedgerDao.kt     # Aggregation query: remaining count
│   │   ├── entity/
│   │   │   ├── BaselineSnapshot.kt
│   │   │   ├── AdjustmentLog.kt
│   │   │   └── CompletionLog.kt
│   │   ├── Converters.kt              # Instant ⇄ Long converters
│   │   └── migration/                 # Room migrations (future)
│   │
│   ├── repository/
│   │   ├── QazaRepositoryImpl.kt       # Implements domain.repository.QazaRepository
│   │   └── DataStorePreferencesManager.kt  # DataStore wrapper for notifications
│   │
│   └── di/
│       ├── DatabaseModule.kt          # @Provides Room, DAOs
│       └── RepositoryModule.kt        # @Binds repository interface
│
├── domain/
│   ├── model/
│   │   ├── PrayerType.kt              # Enum: FAJR, DHUHR, ASR, MAGHRIB, ISHA
│   │   ├── CalculationMethod.kt       # Enum: EXACT_DATES, AGE_ESTIMATE
│   │   ├── AdjustmentReason.kt        # Enum: MANUAL_CORRECTION, EXEMPTION, RECALCULATION
│   │   ├── BaselineCalculation.kt     # Result of calculated baseline
│   │   ├── CompletionEntry.kt         # Single logged prayer
│   │   ├── RemainingPrayerCount.kt    # Aggregated remaining per prayer type
│   │   ├── CompletionProjection.kt    # Sealed: AlreadyCaughtUp, InsufficientData, Estimated
│   │   └── NotificationFrequency.kt   # Enum: NEVER, DAILY, WEEKLY, BIWEEKLY
│   │
│   ├── repository/
│   │   └── QazaRepository.kt          # Interface for data access
│   │
│   ├── usecase/
│   │   ├── CalculateBaselineUseCase.kt     # Exact dates & age estimate modes
│   │   ├── LogCompletionUseCase.kt         # Single + batch logging
│   │   ├── ApplyAdjustmentUseCase.kt       # Manual corrections
│   │   ├── ProjectCompletionDateUseCase.kt # Pace-based projection
│   │   ├── ConfirmBaselineUseCase.kt       # Persist reviewed baseline
│   │   ├── ObserveHistoryUseCase.kt        # Merge completions + adjustments
│   │   └── ScheduleNotificationsUseCase.kt # Reschedule WorkManager
│   │
│   └── notification/
│       ├── NotificationScheduler.kt   # Interface for scheduling
│       └── NotificationFrequency.kt   # Frequency ↔ interval mapping
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt                   # Color constants (Terracotta, Sage, Cream, etc.)
│   │   ├── Theme.kt                   # lightColorScheme + darkColorScheme
│   │   ├── Type.kt                    # Typography scale (h1–h6, body, caption)
│   │   └── Dimens.kt                  # Spacing & corner radius constants
│   │
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt        # Root composable (Hilt-wired)
│   │   ├── OnboardingContent.kt       # Stateless content composable
│   │   ├── OnboardingViewModel.kt     # @HiltViewModel, state holder
│   │   └── OnboardingScreenTest.kt    # Compose UI tests
│   │
│   ├── baseline_summary/
│   │   ├── BaselineSummaryScreen.kt
│   │   ├── BaselineSummaryContent.kt
│   │   ├── BaselineSummaryViewModel.kt
│   │   └── BaselineSummaryScreenTest.kt
│   │
│   ├── dashboard/
│   │   ├── DashboardScreen.kt         # Home screen
│   │   ├── DashboardContent.kt
│   │   ├── DashboardViewModel.kt      # @HiltViewModel
│   │   └── DashboardScreenTest.kt     # All 3 projection states
│   │
│   ├── batch_logging/
│   │   ├── BatchLoggingScreen.kt
│   │   ├── BatchLoggingContent.kt
│   │   ├── BatchLoggingViewModel.kt
│   │   └── BatchLoggingScreenTest.kt
│   │
│   ├── adjustment_dialog/
│   │   ├── AdjustmentDialog.kt        # AlertDialog component
│   │   ├── AdjustmentDialogState.kt
│   │   └── AdjustmentDialogStateTest.kt
│   │
│   ├── history/
│   │   ├── HistoryScreen.kt
│   │   ├── HistoryContent.kt
│   │   ├── HistoryViewModel.kt
│   │   └── HistoryScreenTest.kt
│   │
│   ├── settings/
│   │   ├── SettingsScreen.kt          # Hilt-wired screen + stateless content + ViewModel
│   │   └── SettingsScreenTest.kt
│   │
│   └── navigation/
│       ├── Routes.kt                  # All route definitions
│       └── AppViewModel.kt            # Decides nav start destination (based on hasBaseline)
│
├── notification/
│   ├── QazaNotificationWorker.kt      # @HiltWorker, posts notification
│   ├── NotificationChannels.kt        # Notification channel setup
│   ├── WorkManagerNotificationScheduler.kt  # Implements NotificationScheduler
│   ├── NotificationPermissionGate.kt  # DataStore-backed one-time flag
│   ├── NotificationPermissionPolicy.kt # API-level gating logic (API 33+ check)
│   ├── NotificationFrequencyTest.kt   # Frequency mapping tests
│   └── NotificationPermissionPolicyTest.kt # API-level gating tests
│
└── di/
    ├── DatabaseModule.kt              # Room database, DAOs
    ├── RepositoryModule.kt            # @Binds repository + NotificationScheduler
    └── DataStoreModule.kt             # DataStore singleton
    
(QazaTrackerApp.kt is marked with @HiltAndroidApplication; 
 WorkManager Configuration.Provider is defined there, not in a separate module)
```

## Data Flow

### Event-Sourced Remaining Count (Core Design)

```
BaselineSnapshot (calculated once)
     ↓
AdjustmentLog (delta updates)   CompletionLog (each prayer logged)
     ↓                                ↓
     └────────────────┬───────────────┘
                      ↓
            PrayerLedgerDao aggregation query:
            remaining = baseline + SUM(adjustments) - COUNT(completions)
                      ↓
                Flow<RemainingPrayerCount>
                      ↓
            DashboardViewModel observes
```

### Screen Navigation Flow

```
Onboarding
    ↓ (Continue)
BaselineSummary
    ↓ (Start tracking)
Dashboard (home, cleared from back stack)
    ├→ History (☰ icon)
    ├→ Settings (gear icon)
    ├→ Batch Logging ("Log multiple days" button)
    └→ Adjustment Dialog (✏ edit icon per prayer row)
```

### Notification Scheduling Flow

```
SettingsScreen → ScheduleNotificationsUseCase
                 ↓
              persist to DataStore (frequency)
                 ↓
        WorkManagerNotificationScheduler
                 ↓
    PeriodicWorkRequest(interval based on frequency)
                 ↓
         QazaNotificationWorker (on schedule)
                 ↓
         POST_NOTIFICATIONS permission check
                 ↓
         NotificationChannels.REMINDERS channel
                 ↓
         System notification bar
```

## Key Design Patterns

### 1. Event-Sourced Data (No Mutable Counters)

Never store `remaining_prayers` as a single integer. Always compute it:
```kotlin
// Room query
remaining = baseline.initialCount + SUM(adjustmentLog.delta) - COUNT(completionLog)
```

Benefits:
- Full audit trail (every change is logged)
- No silent data corruption (computed fresh per query)
- Safe undo (just add a negative adjustment)
- Transparent "why did the number change?" (view the ledger)

### 2. MVVM with Hilt Injection

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: QazaRepository,
    private val projectionUseCase: ProjectCompletionDateUseCase
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = // ...
}
```

### 3. Reactive State with Flow

All screens observe Room queries as `Flow<T>`, never imperatively fetch data:
```kotlin
val remaining: Flow<RemainingPrayerCount> = repository.observeRemaining()
// Composable auto-updates when underlying DB changes
```

### 4. Stateless Content Composables (Planned Pattern, Not Yet Implemented)

Current structure: each screen file (`DashboardScreen.kt`, `SettingsScreen.kt`, 
etc.) contains both the Hilt-wired screen root and the stateless content composable 
in the same file, not split.

Planned pattern (for future refactoring):
- `XyzScreen.kt` — Hilt-wired, observes ViewModel, calls callbacks
- `XyzContent.kt` — stateless, pure (no Hilt, no Android), takes state + callbacks → UI
  
Benefit: content composables would be testable in Compose UI tests without Hilt.
Current implementation still uses Compose UI tests against the full Hilt-wired screen.

### 5. Manual Adjustment via Event Log (Not Mutations)

User edits a prayer count → creates an `AdjustmentLog` entry:
```kotlin
ApplyAdjustmentUseCase(
    prayerType = FAJR,
    delta = -100,  // user reduced baseline by 100
    reason = MANUAL_CORRECTION,
    note = "Recounted more carefully"
)
// This creates one AdjustmentLog row, leaves BaselineSnapshot unchanged
// Remaining count auto-updates via aggregation query
```

### 6. Compile-Time Fiqh Configuration (No Madhab Selector)

Madhab/Witr handling is a single const in `domain/model/` or compile-time 
configuration. v1: hardcoded as 5 prayers. Future: could become a 
BuildConfig variable or a user setting via a new screen.

## Testing Strategy

### Unit Tests (No Android)
- Domain use cases (especially CalculateBaselineUseCase with leap years)
- NotificationFrequency frequency-to-interval mapping
- NotificationPermissionPolicy API-level gating
- Run with `./gradlew testDebugUnitTest`

### Database Tests (Robolectric + In-Memory Room)
- DAO CRUD + aggregation queries
- Event-sourcing math (baseline + adjustments - completions)
- Negative deltas, batch isolation, per-prayer-type isolation
- Run with `./gradlew testDebugUnitTest` (same suite)

### Compose UI Tests (instrumented, on emulator/device)
- Screen state rendering (all three projection states)
- Button/icon clicks and navigation
- Dialog open/close and input validation
- Run with `./gradlew connectedAndroidTest` after `adb install`

### Manual QA (on emulator or real device)
- Full user flow: onboarding → baseline → dashboard → logging → history → settings
- Back navigation (confirm nothing loops, onboarding clears correctly)
- Dark mode toggle
- Notification permission prompt (first launch only)
- Settings frequency change (reschedules immediately)

## Dependency Injection

### Hilt Modules

**DatabaseModule** (`data/di/DatabaseModule.kt`):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(context: Context): QazaDatabase = 
        Room.databaseBuilder(context, QazaDatabase::class.java, "qaza.db")
            .addTypeConverter(Converters())
            .build()
    
    @Provides fun provideBaselineSnapshotDao(db: QazaDatabase) = db.baselineSnapshotDao()
    // ... other DAOs
}
```

**RepositoryModule** (`data/di/RepositoryModule.kt`):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindQazaRepository(impl: QazaRepositoryImpl): QazaRepository
    
    @Binds
    fun bindNotificationScheduler(
        impl: WorkManagerNotificationScheduler
    ): NotificationScheduler
}
```

**DataStoreModule** (`data/di/DataStoreModule.kt`):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStore(context: Context): DataStore<Preferences> = 
        PreferenceDataStoreFactory.create(scope = CoroutineScope(...)) {
            context.preferencesDataStoreFile("qaza_prefs")
        }
}
```

**WorkManager Hilt Configuration** (in `QazaTrackerApp.kt`):
```kotlin
@HiltAndroidApp
class QazaTrackerApp : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration() = 
        Configuration.Builder()
            .setWorkerFactory(HiltWorkerFactory())
            .build()
    
    // Also creates notification channel and schedules default notifications on cold start
}
```

## Key Files to Know

| File | Purpose |
|------|---------|
| `QazaTrackerApp.kt` | @HiltAndroidApplication, notification channel setup, WorkManager config |
| `MainActivity.kt` | App entry point, runtime permission request on first launch |
| `QazaDatabase.kt` | Room setup, entity declarations |
| `PrayerLedgerDao.kt` | The critical aggregation query (remaining count formula) |
| `QazaRepository.kt` / `QazaRepositoryImpl.kt` | Single source of truth for data access |
| `CalculateBaselineUseCase.kt` | Leap-year-safe date math, most important domain logic |
| `DashboardScreen.kt` | Main home screen after onboarding |
| `QazaNavHost.kt` | All routes + navigation logic |
| `QazaNotificationWorker.kt` | @HiltWorker, posts reminder notification on schedule |
| `NotificationPermissionPolicy.kt` | API 33+ gating for POST_NOTIFICATIONS prompt |

## Common Patterns

### Observing a Flow in a ViewModel

```kotlin
private val _uiState = MutableStateFlow(DashboardUiState())
val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

init {
    viewModelScope.launch {
        repository.observeRemainingCounts().collect { remaining ->
            _uiState.update { it.copy(rows = remaining.perPrayerType) }
        }
    }
}
```

### Persisting to DataStore

```kotlin
suspend fun setNotificationFrequency(frequency: NotificationFrequency) {
    dataStore.edit { prefs ->
        prefs[NOTIFICATION_FREQUENCY_KEY] = frequency.name
    }
}
```

### Creating an Event-Sourced Adjustment

```kotlin
suspend fun applyAdjustment(prayerType: PrayerType, delta: Int, note: String?) {
    require(delta != 0) { "Cannot apply zero adjustment" }
    database.adjustmentLogDao().insert(
        AdjustmentLog(
            prayerType = prayerType,
            delta = delta,
            reason = AdjustmentReason.MANUAL_CORRECTION,
            note = note,
            timestamp = Instant.now()
        )
    )
    // Dashboard's Flow automatically emits new aggregated count
}
```

## Known Limitations & Future Improvements

1. **No migration framework yet** — Room schema is V1. Add versioning + 
   migration files as schema evolves (in `data/local/migration/`)
2. **No analytics** — could add Firebase or local event logging
3. **No crash reporting** — consider Crashlytics for production
4. **Female exemption data model exists but no UI** — ready for v2
5. **AdMob integration not started** — needs `AdProvider` interface in 
   `domain/ad/` + wiring in `RepositoryModule` before integration
6. **UI strings not externalized** — hardcoded in Compose files, not in 
   `res/values/strings.xml`. RTL verification/translation would require 
   first externalizing all UI copy to string resources.
7. **No offline/online sync** — but intentionally offline-only per spec

## Remaining Pre-Launch Work

1. **Privacy policy** — write careful, honest policy for Play Store (90 min)
2. **App icon** — design or find icon (30 min–1 hour)
3. **Play Store screenshots** — grab from emulator (10 min)
4. **Release signing** — create keystore, set up signing in gradle (15 min)
5. **RTL verification** — test with Arabic/Urdu system language (requires 
   first externalizing UI strings to res/values/strings.xml for proper 
   translation support) (30 min setup + testing time)
6. **Manual QA** — full end-to-end testing (1 hour)
