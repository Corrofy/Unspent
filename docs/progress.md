# Unspent — Progress

Current version: **3.14** (versionCode 8)

---

## What Is Done

### Core Features (Complete)
- [x] Personal Balance Ledger — set starting balance, add income/expense entries, running balance
- [x] Lending & Borrowing Tracker — add people, log lent/borrowed, net position per person
- [x] Partial & full settlement of lending entries
- [x] Person deletion (only when fully settled)
- [x] Lending entry deletion
- [x] Ledger entry deletion
- [x] Duplicate person name protection (case-insensitive) with suggestion chips
- [x] 3-tier modal system: QuickAdd, AddNewPerson, PersonLedger
- [x] Real-time net position calculation (owed to me vs I owe)

### Data Layer (Complete)
- [x] Room database with 4 tables (v4 schema)
- [x] Singleton database with pre-warm on Application.onCreate
- [x] Repository pattern with all CRUD operations
- [x] v3 migration logic for People table (one-time SharedPreferences migration)
- [x] AppSettingEntity for key-value storage (initial_balance)

### UI / Design (Complete)
- [x] Tokyo Night dark theme with extended color palette
- [x] Frosted glass UI components (FrostedCard, FrostedItemCard, FrostedPillButton)
- [x] Custom brand mark (UnspentLogo — 300° arc with 60° bite)
- [x] SnappyBottomSheetDialog (Material 3 ModalBottomSheet, skipPartiallyExpanded)
- [x] FrostedHeader with brand icon and current tab title
- [x] FrostedBottomBar with pill-shaped active indicator
- [x] Smooth Crossfade tab transitions (180ms)
- [x] Custom XML animations for dialogs and bottom sheets
- [x] Edge-to-edge support with status bar padding

### Utilities (Complete)
- [x] CurrencyUtils — Rs. formatter with decimals, explicit sign, integer modes
- [x] PlainTextFormatter — markdown-to-plain-text stripper

### Testing (Partial)
- [x] Currency formatting unit tests (6 cases)
- [x] Robolectric startup profiling test (DB, Repo, VM, Activity timing)
- [x] QuickAddLendingSheet rapid open/close crash test
- [x] AddNewPersonLendingSheet rapid open/close crash test
- [x] AddLedgerEntrySheet rapid open/close crash test
- [x] Roborazzi screenshot test framework (Pixel 8, SDK 36)

### Build / Config (Complete)
- [x] Gradle KTS with version catalog (libs.versions.toml)
- [x] Secrets plugin (.env / .env.example convention)
- [x] Signing configs (debug + release)
- [x] ProGuard rules file (placeholder)
- [x] APK build (Unspent-v3.14.apk present)

---

## What Can Be Done (Future Work)

### High Priority
- [ ] **Edit existing entries** — Currently entries can only be deleted, not edited in-place
- [ ] **Search & filter** — Search people by name, filter lending entries by date/status
- [ ] **Date-based grouping** — Group lending/ledger entries by date (today, this week, this month)
- [ ] **Export data** — CSV/JSON export of all lending and ledger entries
- [ ] **Notifications** — Reminder notifications for unsettled lending entries (e.g., "Rahul owes you Rs. 500 for 7 days")

### Medium Priority
- [ ] **Categories/tags for ledger** — Tag ledger entries (Food, Transport, Salary, etc.) with filtering
- [ ] **Multi-currency support** — Currency selector per entry, configurable base currency
- [ ] **Recurring transactions** — Auto-generate recurring entries (rent, subscriptions)
- [ ] **Statistics dashboard** — Monthly spending/income charts, lending summary graphs
- [ ] **Dark/Light theme toggle** — Currently dark-only; add light theme variant
- [ ] **Onboarding screens** — First-run walkthrough explaining the two modules

### Low Priority
- [ ] **Cloud backup/restore** — Optional encrypted backup to local file or cloud
- [ ] **Settlement history log** — Dedicated view of all past settlements with timestamps
- [ ] **Split transactions** — Split a single expense across multiple people
- [ ] **Photo attachments** — Attach photos (receipts, screenshots) to entries
- [ ] **Widgets** — Home screen widget showing current balance or net lending position
- [ ] **Biometric lock** — App lock with fingerprint/face unlock

### Technical / Code Quality
- [ ] **Migrate from AndroidViewModel to Hilt/Dagger** — DI for cleaner testability
- [ ] **Add Compose UI tests** — End-to-end navigation and interaction tests
- [ ] **Room schema export** — Enable schema export for migration testing
- [ ] **Add lint baseline** — Capture existing lint warnings and fix incrementally
- [ ] **CI/CD pipeline** — GitHub Actions for automated builds and test runs
- [ ] **Accessibility audit** — Add content descriptions, test with TalkBack
- [ ] **Localization** — Multi-language support (strings.xml extraction)
- [ ] **ProGuard minification** — Enable R8 minification for release builds

### Known Issues / Technical Debt
- `fallbackToDestructiveMigration()` in AppDatabase — migrations wipe data on schema change
- `PlainTextFormatter.kt` is unused in the app (likely leftover from a removed feature)
- Google Services plugin is commented out — Firebase integration is not active
- `metadata.json` has `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` but Gemini API is not used in code
- Currency is hardcoded to Rs. (Indian Rupee) — not configurable
