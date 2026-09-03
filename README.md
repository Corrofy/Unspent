# Unspent

A 100% local, high-performance personal lending tracker and wallet balance ledger for Android.

Track who owes you, who you owe, and your daily wallet balance — all stored on-device with zero cloud dependency.

---

## Features

- **Lending & Borrowing Tracker** — Add people, log lent/borrowed transactions, see net positions at a glance, settle partial or full amounts, and delete settled people.
- **Personal Balance Ledger** — Set a starting wallet balance, log income and expenses, and track your running balance in real-time.
- **Duplicate Protection** — Real-time name matching when adding new people prevents accidental duplicates.
- **Partial Settlement** — Settle transactions in full or in part with a flexible settle dialog.
- **Frosted Glass UI** — Custom Material 3 components with a Tokyo Night dark theme, smooth animations, and bottom sheet dialogs.
- **100% Offline** — Room database on-device, no network, no Firebase, no analytics.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (Material 3) |
| Database | Room 2.7.0 (4 tables, version 4) |
| Architecture | MVVM (AndroidViewModel + Repository + DAO) |
| Build | Gradle KTS, AGP 9.1.1, KSP 2.3.5 |
| Testing | JUnit 4, Robolectric 4.16.1, Roborazzi 1.59.0 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |
| Current Version | 3.14 (versionCode 8) |

---

## Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt              # Entry point, tab navigation, Scaffold
├── UnspentApplication.kt        # Application class, DB pre-warm
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt       # Room DB (4 entities, singleton)
│   │   ├── LedgerDao.kt         # DAO for ledger_entries
│   │   ├── LendingDao.kt        # DAO for lending_entries
│   │   ├── PersonDao.kt         # DAO for people
│   │   └── AppSettingDao.kt     # DAO for app_settings
│   ├── model/
│   │   ├── LedgerEntry.kt       # Ledger entry entity
│   │   ├── LendingEntry.kt      # Lending entry entity
│   │   ├── PersonEntity.kt      # Person entity
│   │   └── AppSettingEntity.kt  # Key-value settings entity
│   └── repository/
│       └── AppRepository.kt     # Single data access layer
├── ui/
│   ├── MainViewModel.kt         # ViewModel for both modules
│   ├── components/
│   │   └── FrostedGlass.kt      # Reusable UI components
│   ├── lending/
│   │   └── LendingScreen.kt     # Lending/borrowing screen + sheets
│   ├── ledger/
│   │   └── LedgerScreen.kt      # Wallet balance ledger screen
│   └── theme/
│       ├── Color.kt             # Tokyo Night palette + extended colors
│       ├── Theme.kt             # Compose theme + CompositionLocal
│       └── Type.kt              # Typography scale
└── util/
    ├── CurrencyUtils.kt         # Rs. currency formatter
    └── PlainTextFormatter.kt    # Markdown-to-plain-text stripper
```

---

## Database Schema (Room, v4)

| Table | Columns |
|---|---|
| `ledger_entries` | id (PK), amount, isIncome, label, timestamp |
| `lending_entries` | id (PK), personId (FK), personName, amount, isLentToThem, date, note, isSettled, settledTimestamp, settledAmount |
| `people` | id (PK), name, createdAt |
| `app_settings` | key (PK), value |

---

## Getting Started

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio
2. Select **Open** and choose this project directory
3. Allow Android Studio to fix any incompatibilities
4. Create a `.env` file in the project root with your Gemini API key (see `.env.example`)
5. Remove this line from `app/build.gradle.kts`: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run on an emulator or physical device (API 24+)

---

## Build & Test

```bash
# Unit tests
./gradlew testDebugUnitTest

# Robolectric + screenshot tests
./gradlew testDebugUnitTest --tests "com.example.ExampleRobolectricTest"
./gradlew testDebugUnitTest --tests "com.example.GreetingScreenshotTest"

# Lint
./gradlew lint
```

---

## License

This project is private. Do not distribute without permission.
