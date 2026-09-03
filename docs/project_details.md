# Unspent — Project Details

This file contains comprehensive project information for AI agents working on this codebase.

---

## Overview

**Unspent** is a native Android app (Kotlin/Jetpack Compose) for tracking personal finances in two modules:

1. **Lending Tracker** (primary tab) — Track money lent to and borrowed from specific people. Each person has a net position (positive = they owe you, negative = you owe them). Supports partial settlements and deletion of fully-settled people.

2. **Personal Balance Ledger** (secondary tab) — A simple wallet balance tracker. Set a starting balance, log income (+) and expenses (-), and see your running total.

---

## Architecture

```
MainActivity (tab navigation, Scaffold)
  └─ MainViewModel (AndroidViewModel, single ViewModel for both tabs)
       └─ AppRepository (single data access layer)
            └─ AppDatabase (Room, singleton)
                 ├─ LedgerDao    → ledger_entries table
                 ├─ LendingDao   → lending_entries table
                 ├─ PersonDao    → people table
                 └─ AppSettingDao → app_settings table
```

- **No dependency injection** — ViewModel directly instantiates Repository
- **No navigation component** — Tab state managed with `mutableStateOf(NavTab.LENDING)` in MainActivity
- **No network calls** — 100% offline

---

## Key Files Reference

### Entry Points
- `MainActivity.kt:37` — Activity, sets up Compose content with theme
- `UnspentApplication.kt:9` — Application class, pre-warms Room DB on IO thread

### Data Layer
- `AppDatabase.kt:12` — Room database definition (version 4, 4 entities)
- `AppRepository.kt:17` — Repository with all business logic, v3 migration in init block
- `LedgerEntry.kt:7` — Entity: id, amount, isIncome, label, timestamp
- `LendingEntry.kt:8` — Entity: id, personId, personName, amount, isLentToThem, date, note, isSettled, settledTimestamp, settledAmount
- `PersonEntity.kt:7` — Entity: id, name, createdAt
- `AppSettingEntity.kt:7` — Entity: key (PK), value

### DAOs
- `LedgerDao.kt:13` — getAllEntries (Flow), insertEntry, updateEntry, deleteEntry, deleteById
- `LendingDao.kt:12` — getAllEntries, getEntriesForPersonId, getEntriesForPerson, insertEntry, updateEntry, deleteEntry, deleteById, deleteByPersonId, deleteByPersonName
- `PersonDao.kt:13` — getAllPeople, getPersonById, getPersonByName, insertPerson, updatePerson, deletePerson, deleteById, deleteByName
- `AppSettingDao.kt:11` — getSetting (Flow), getSettingDirect (suspend), setSetting

### ViewModel
- `MainViewModel.kt:24` — AndroidViewModel exposing StateFlows for both tabs
- `PersonLendingSummary` data class at line 16 — Computed per-person summary with netAmount, openEntriesCount, transactions list
- `personSummaries` at line 66 — Complex combine flow that groups entries by person, handles unassigned entries, sorts by absolute net

### UI Screens
- `LendingScreen.kt:83` — Main lending screen with LazyColumn of person cards
- `LendingScreen.kt:472` — QuickAddLendingSheet (2-tap quick add for existing person)
- `LendingScreen.kt:682` — AddNewPersonLendingSheet (new person with duplicate protection)
- `LendingScreen.kt:908` — PersonLedgerSheet (full history, settle, delete for a person)
- `LedgerScreen.kt:74` — Wallet balance ledger screen
- `LedgerScreen.kt:416` — AddLedgerEntrySheet (income/expense entry)

### Components
- `FrostedGlass.kt:70` — FrostedCard (surface container with border)
- `FrostedGlass.kt:95` — FrostedItemCard (list row with optional click)
- `FrostedGlass.kt:131` — UnspentLogo (Canvas-drawn 300° arc brand mark)
- `FrostedGlass.kt:177` — FrostedHeader (top bar with brand icon + tab title)
- `FrostedGlass.kt:231` — FrostedBottomBar (bottom navigation with pill indicators)
- `FrostedGlass.kt:304` — FrostedPillButton (primary/secondary action button)
- `FrostedGlass.kt:361` — SnappyBottomSheetDialog (ModalBottomSheet wrapper)

### Theme
- `Color.kt:6` — Tokyo Night color palette (16 named colors)
- `Color.kt:22` — AppExtendedColors data class (7 custom semantic colors)
- `Theme.kt:23` — TokyoNightColorScheme (Material 3 darkColorScheme)
- `Theme.kt:47` — MyApplicationTheme composable with CompositionLocalProvider
- `Type.kt:9` — Typography scale (displayLarge through labelSmall)

### Utilities
- `CurrencyUtils.kt:17` — CurrencyFormatter object with format(amount, includeDecimals, explicitSign)
- `CurrencyUtils.kt:51` — formatCurrency() top-level helper
- `PlainTextFormatter.kt:6` — stripMarkdown() function (currently unused in app)

---

## Navigation

Two tabs managed by `NavTab` enum in `FrostedGlass.kt:61`:
- `NavTab.LENDING` — "Lent / Borrow" tab (default)
- `NavTab.LEDGER` — "Ledger" tab

Back handler in `MainActivity.kt:60` returns to LENDING tab from LEDGER tab.

---

## Database Migrations

- **v1 → v2**: Unknown (no migration code present)
- **v2 → v3**: People table added; lending entries linked to people via personId. Migration runs once via SharedPreferences flag `migration_v3_complete` in `AppRepository.kt:26-53`
- **v3 → v4**: Unknown (uses `fallbackToDestructiveMigration()`)

---

## Build Configuration

- `build.gradle.kts` (root) — plugins: android.application, kotlin.compose, google.devtools.ksp, roborazzi, secrets, google.services
- `app/build.gradle.kts` — namespace `com.example`, applicationId `com.aistudio.personalledger.lokvx`, minSdk 24, targetSdk 36
- `gradle/libs.versions.toml` — version catalog with 44 library declarations
- `gradle.properties` — JVM args 4GB, parallel builds, config cache, 4 workers max

---

## Testing

- `ExampleUnitTest.kt` — Currency formatting tests (6 assertions)
- `ExampleRobolectricTest.kt` — Startup profiling, sheet open/close crash tests (4 tests)
- `GreetingScreenshotTest.kt` — Roborazzi screenshot capture (Pixel 8, SDK 36)
- Test runner: `AndroidJUnitRunner`
- Robolectric config: SDK 36, NATIVE graphics mode

---

## APK

- `apk/Unspent-v3.14.apk` — Pre-built release APK

---

## Conventions

- **Currency**: Hardcoded to Indian Rupees (Rs.) with comma-separated thousands
- **Theme**: Dark-only (Tokyo Night), no light variant
- **Package**: `com.example` (AI Studio-generated package name)
- **Language**: Kotlin only, no Java source files
- **No comments**: Code is self-documenting; no inline comments in source
