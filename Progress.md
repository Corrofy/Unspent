# Unspent — Project Progress

> Living document tracking the history, current state, and future direction of **Unspent**.
> Intended for future AI agents and contributors to pick up work quickly.

**Current version:** v3.0 (versionCode 4) · **Language:** Kotlin · **UI:** Jetpack Compose

---

## Table of Contents

1. [Version History](#version-history)
2. [Implemented Features](#implemented-features)
3. [Architecture & Design Decisions](#architecture--design-decisions)
4. [Database Schema](#database-schema)
5. [UI / Design System](#ui--design-system)
6. [Testing](#testing)
7. [Known Limitations & Technical Debt](#known-limitations--technical-debt)
8. [Future Work](#future-work)

---

## Version History

### v0.01 — Initial Prototype
- Basic single-screen app scaffold from AI Studio template
- Established Jetpack Compose + Material 3 baseline
- Initial `Local Personal Ledger` data model proof-of-concept
- Debug-only release

### v1.0 — Core Ledger
- **Module 1: Personal Balance Ledger** implemented
  - Add income/expense entries with labels and timestamps
  - Running balance calculation
  - Base (initial) balance setting
- Room database introduced (`local_personal_ledger.db`, v1)
- `CurrencyUtils` created — hardcoded **Indian Rupees (Rs.)** formatting
- Basic list UI for recent activity

### v2.0 — Lending Tracker (People)
- **Module 2: Lending & Borrowing Tracker** implemented
  - `personName`-based entries (no separate person table yet)
  - Add lent/borrowed transactions per name
  - Simple per-name grouping
  - Full/partial settlement logic introduced
- Database migrated to include settlement fields (`isSettled`, `settledAmount`, `settledTimestamp`)
- App Database bumped to v2

### v2.5 — Notes Module
- **Module 3: Simple Notes** implemented
  - Create/edit/delete plain-text notes
  - Optional tags
  - Note search
  - Markdown stripping on save (`PlainTextFormatter`)
- Database updated to v3 with `notes` table

### v3.0 — People Migration & UI Overhaul (Current)
- **People migration** (one-time, run in `AppRepository.init`):
  - Added `PersonEntity` table
  - Existing name-based lending entries grouped & re-pointed to new `personIds`
- **Lending screen rewrite (largest file, ~1255 lines):**
  - Person cards with initials-avatar, net badges, open-entry counts
  - `QuickAddLendingSheet` — zero-typing quick entry for existing contacts
  - `AddNewPersonLendingSheet` — duplicate detection via `AssistChip` suggestions
  - `PersonLedgerSheet` — full per-person history with partial settlement dialog
  - Delete-person flow (only allowed once fully settled)
- **Notes cleanup migration** — existing note titles/content stripped of markdown
- **Tokyo Night "frosted glass" design system** completed:
  - `FrostedCard`, `FrostedItemCard`, `FrostedHeader`, `FrostedBottomBar`, `FrostedPillButton`
  - Custom Canvas-drawn `UnspentLogo` (300° arc coin with "U" bite)
  - Custom adaptive launcher icon with gradient + glow
- Default landing tab set to **LENDING**
- `BackHandler` returns to LENDING tab from LEDGER/NOTES

---

## Implemented Features

### Module 1: Personal Balance Ledger
`LedgerScreen.kt` + `MainViewModel` + `LedgerDao`

- [x] Add expense (−) / income (+) entries with optional label
- [x] Real-time running balance = base + Σ(entries)
- [x] Editable base balance (defaults to `1000.0`)
- [x] Recent activity list ordered newest-first
- [x] Color-coded badges (green income / red expense) with explicit `+`/`−` signs
- [x] Delete entry (with confirmation dialog)
- [x] Empty state with instructional hint

**UX flows:**
- Hero card shows `CURRENT BALANCE` with tap-to-edit base balance
- `AddLedgerEntrySheet` bottom sheet: auto-focused amount field, SPEND(−)/INCOME(+) split buttons

### Module 2: Lending & Borrowing Tracker
`LendingScreen.kt` + `MainViewModel` + `LendingDao` + `PersonDao`

- [x] People ledger with created contacts (`PersonEntity`)
- [x] Per-person transaction history
- [x] Net amount calculation (lent − borrowed)
- [x] Position dashboard: *I lent* (green) vs. *they lent me* (red)
- [x] Full AND partial settlement with configurable amount
- [x] Quick-add for existing persons (no typing)
- [x] New-person flow with real-time duplicate detection (suggestion chips)
- [x] Delete individual entries
- [x] Delete entire person (gated on fully-settled status)
- [x] Unassigned/legacy entries grouped via name-matching fallback

**Important computed logic (`personSummaries`):**
- Groups by `personId` primarily; falls back to matching on `personName` for legacy rows
- Sorts people by absolute net amount descending
- Reports `netAmount`, `openEntriesCount`, and full transaction list per person

### Module 3: Simple Notes
`NotesScreen.kt` + `MainViewModel` + `NoteDao`

- [x] Create / edit / delete notes
- [x] Title + optional tag + multi-line content
- [x] Tag input auto-strips leading `#`
- [x] Full-text `LIKE` search (title or content)
- [x] Reactive search (`flatMapLatest` on search query state)
- [x] Markdown stripped → plain text on save
- [x] Notes ordered by most recent modification
- [x] Distinct empty states for "search found nothing" vs. "no notes yet"

---

## Architecture & Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| UI framework | **100% Jetpack Compose** | No XML layouts anywhere; fast, modern, single source of truth |
| Architecture | **MVVM** | Clear separation: Room → Repository → ViewModel → Compose |
| ViewModel count | **Single `MainViewModel`** | App is small; one VM avoids boilerplate across 3 modules |
| Navigation | **State + `Crossfade`** | Navigation-Compose declared but commented out; tab state is simpler |
| Dependency injection | **None (manual)** | Avoided Hilt/Dagger overhead; `AppRepository` constructed directly in VM |
| Persistence | **Room (SQLite)** | Type-safe, reactive `Flow` support, no cloud dependency |
| Migrations | **`fallbackToDestructiveMigration()`** | Pragmatic for a local tool; data loss risk accepted for simplicity |
| Currency | **Hardcoded `Rs.`** | India-focused; centralized in `CurrencyUtils` to ease future localization |
| Networking | **Retrofit + Moshi declared** | Present in deps but currently **unused** — no `INTERNET` permission in manifest |
| Offline-first | **Yes** | No permissions, no account, no cloud sync; everything stored locally |

---

## Database Schema

Database name: `local_personal_ledger.db` · Version: **3**

### `ledger_entries`
| Column | Type | Notes |
|--------|------|-------|
| `id` | Long | PK, auto-generate |
| `amount` | Double | |
| `isIncome` | Boolean | true = income, false = expense |
| `label` | String | |
| `timestamp` | Long | |

### `lending_entries`
| Column | Type | Notes |
|--------|------|-------|
| `id` | Long | PK, auto-generate |
| `personId` | Long | FK to `people` (may be stale for legacy rows) |
| `personName` | String | Denormalized — used for legacy grouping fallback |
| `amount` | Double | |
| `isLentToThem` | Boolean | true = I lent → they owe me |
| `date` | Long | |
| `note` | String | |
| `isSettled` | Boolean | |
| `settledTimestamp` | Long? | |
| `settledAmount` | Double | |

### `people`
| Column | Type | Notes |
|--------|------|-------|
| `id` | Long | PK, auto-generate |
| `name` | String | Unique-ish; case-insensitive search |
| `createdAt` | Long | |

### `notes`
| Column | Type | Notes |
|--------|------|-------|
| `id` | Long | PK, auto-generate |
| `title` | String | |
| `content` | String | Markdown stripped on save |
| `tag` | String | |
| `createdAt` | Long | |
| `modifiedAt` | Long | |

### `app_settings`
| Column | Type | Notes |
|--------|------|-------|
| `key` | String | PK |
| `value` | String | Key–value store (e.g. `initialBalance`) |

---

## UI / Design System

**Theme:** Tokyo Night (dark-only) — defined in `ui/theme/Color.kt`

| Token | Hex |
|-------|-----|
| Background | `#1A1B26` |
| Surface | `#24283B` |
| Surface Elevated | `#2E344F` |
| Primary (lavender) | `#BB9AF7` |
| Secondary (blue) | `#7AA2F7` |
| Tertiary (cyan) | `#7DCFFF` |
| Income/Positive (green) | `#9ECE6A` |
| Expense/Negative (red) | `#F7768E` |
| Pending (yellow) | `#E0AF68` |
| Text Primary | `#C0CAF5` |
| Text Secondary | `#7982A9` |

**Custom components (`ui/components/FrostedGlass.kt`):**
- `NavTab` enum (LENDING / LEDGER / NOTES) with active+inactive icons
- `FrostedCard` — hero container: shadow, gradient border, radial glow (`RoundedCornerShape(28.dp)`)
- `FrostedItemCard` — list item with gradient border + ripple (`RoundedCornerShape(20.dp)`)
- `UnspentLogo` — Canvas-drawn 300° arc coin (brand mark)
- `FrostedHeader` — logo + app name + current tab
- `FrostedBottomBar` — 3-tab pill navigation
- `FrostedPillButton` — primary/secondary rounded action button

---

## Testing

- **`ExampleUnitTest.kt`** — `formatCurrency()` cases (`Rs. 1,000.00`, `+Rs. 500.50`, `0.0`, negatives, no-decimals, etc.)
- **`ExampleRobolectricTest.kt`** — reads `R.string.app_name` == `"Unspent"` (SDK 36)
- **`GreetingScreenshotTest.kt`** — Roborazzi golden screenshot of `Text("Unspent")` (Pixel 8, SDK 36)
- **`ExampleInstrumentedTest.kt`** — package/applicationId check on device

**Coverage gap:** No tests yet for Room DAOs, ViewModel logic, or the complex `personSummaries` computation. Robolectric + `kotlinx-coroutines-test` are available to close this.

---

## Known Limitations & Technical Debt

1. **No dependency injection** — `AppRepository` instantiated directly in `MainViewModel`; hard to unit-test the ViewModel in isolation.
2. **`fallbackToDestructiveMigration()`** — any future schema change wipes user data. For a released app, this needs real migrations.
3. **Unused dependencies** cluttering the build — Firebase AI, AppCheck, Retrofit/Moshi are declared/active but **not referenced in code**. (`metadata.json` claims `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` but no Gemini code exists yet.)
4. **Single massive `LendingScreen.kt` (~1255 lines)** — overdue for extraction into smaller composables/sheets.
5. **`personName` denormalized on `lending_entries`** — can drift out of sync with `people.name`; legacy rows rely on name-matching fallback.
6. **Hardcoded Indian Rupees** — no locale/currency switcher.
7. **Huge version catalog** — `libs.versions.toml` defines ~40 libraries, most unused/commented-out. Should be pruned.
8. **Package name `com.example` mismatch with `applicationId`** `com.aistudio.personalledger.lokvx` — cosmetic but confusing; consider aligning.
9. **No automated CI** — no GitHub Actions workflow for build/test on push.
10. **No `LICENSE` file** in repo (README references MIT; a `LICENSE` file should be added).

---

## Future Work

### High Priority
- [ ] Add `LICENSE` file (MIT) to repo root
- [ ] Update root `README.md` → `READMEFORGITHUB.md` content (after rename)
- [ ] Invest in real Room migrations (remove destructive fallback) before more releases
- [ ] Write ViewModel + DAO unit tests; migrate toward DI (Hilt or manual factory) to enable testing
- [ ] Add `.github/workflows/ci.yml` for automated build + test on push/PR

### Medium Priority
- [ ] Extract `LendingScreen.kt` into smaller composables and dedicated sheet files
- [ ] Prune unused dependencies from `libs.versions.toml` and `build.gradle.kts`
- [ ] Implement Gemini AI capability (declared in `metadata.json`) or remove the claim
- [ ] Add backup/export of local DB (e.g., share `.db` or JSON export)
- [ ] Add income/expense categories with filters

### Low Priority / Polish
- [ ] Multi-currency support or locale-based currency selection
- [ ] Light theme variant (currently dark-only)
- [ ] App widgets for quick balance glance
- [ ] Editing existing ledger/lending entries (currently only create + delete)
- [ ] FAB-animation polish and haptic feedback on key actions
- [ ] Data charts — spending trends, lending timeline

---

*Last updated: 2026-09-02*
