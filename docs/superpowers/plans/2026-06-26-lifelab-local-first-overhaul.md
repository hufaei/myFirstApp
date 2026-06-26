# LifeLab Local-First Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert LifeLab from an in-memory Material3 demo into a local-first Android learning app with Room persistence, DataStore preferences, Chinese-first i18n, blue/white light and blue-black dark themes, photo records, and an experimental WebView lab.

**Architecture:** Keep the single Android `app` module and existing `app/core/feature` package boundaries. Add `core/database`, `core/datastore`, `core/media`, and shared UI primitives only where multiple features consume them. Feature repositories should use Room/DataStore as local-first sources and keep UI state driven by Flow.

**Tech Stack:** Kotlin, Jetpack Compose Material3, Navigation Compose, ViewModel + StateFlow, Hilt, Room/SQLite, DataStore Preferences, Android Photo Picker, system camera intent with FileProvider, WebView, JUnit/coroutines tests.

---

## Task 1: Shared Dependencies And App Wiring

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/example/lifelab/app/LifeLabApplication.kt`
- Create/modify under: `app/src/main/java/com/example/lifelab/core/database`, `core/datastore`, `core/media`, `core/ui`

- [ ] Add Room, DataStore Preferences, Hilt Navigation Compose, Coil Compose, material icons extended, and AndroidX WebKit dependencies.
- [ ] Add FileProvider metadata and app-specific cache/file path XML for camera output.
- [ ] Add Hilt modules for database, DAO/repository providers, DataStore, preferences, and media storage.
- [ ] Keep `minSdk = 26`, `compileSdk = 35`, and the existing single `app` module structure.
- [ ] Add focused unit tests for preference mapping and photo count limits before production implementation.

## Task 2: Local-First Database

**Files:**
- Create/modify under: `app/src/main/java/com/example/lifelab/core/database`
- Modify feature repositories under: `feature/*/data`
- Update tests under: `app/src/test/java/com/example/lifelab/**`

- [ ] Create `LifeLabDatabase` with entities and DAOs for tasks, habits, habit check-ins, notifications, search history, discover content cache, and photo records.
- [ ] Seed first-run demo data through repository bootstrap logic, not hardcoded UI state.
- [ ] Replace default `InMemory...Repository` construction with Room-backed repositories while preserving existing domain repository interfaces where practical.
- [ ] Keep repositories local-first; no backend API or sync layer in this slice.
- [ ] Add DAO/repository tests for create/update/delete/query behavior and Flow updates.

## Task 3: Preferences, Theme, And Localization

**Files:**
- Create/modify under: `app/src/main/java/com/example/lifelab/core/datastore`
- Modify: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/LifeLabApp.kt`
- Modify/create: `app/src/main/res/values/strings.xml`, `app/src/main/res/values-en/strings.xml`
- Modify Profile feature settings UI

- [ ] Add `AppPreferencesRepository` with `ThemeMode(System/Light/Dark)` and `LanguageMode(System/Zh/En)`.
- [ ] Apply language via Android app locale APIs where available; default/fallback strings are Chinese, English lives in `values-en`.
- [ ] Replace hardcoded user-facing strings in touched screens with `stringResource`.
- [ ] Define LifeLab light/dark color schemes: clean blue-white light mode and deep blue/blue-black dark mode.
- [ ] Add Profile controls for language and theme with persisted selections.

## Task 4: Photo Records

**Files:**
- Create/modify under: `app/src/main/java/com/example/lifelab/core/media`
- Create/modify under: `app/src/main/java/com/example/lifelab/feature/photos` if a feature boundary is useful
- Modify Tasks and Habits presentation/viewmodels/repositories

- [ ] Add `PhotoRecordRepository` keyed by `ownerType(Task/HabitCheckIn)` and `ownerId`, enforcing a maximum of three photos per owner.
- [ ] Store image files in app-specific storage; Room stores metadata and URI/path only.
- [ ] Support Android Photo Picker for gallery selection and system camera capture through FileProvider.
- [ ] Add task editor/detail photo attachments and habit check-in photo attachments.
- [ ] Add reusable photo grid/preview components with loading, empty, remove, and failed-image states.

## Task 5: Feature UI/UX Polish

**Files:**
- Modify feature screens under: `feature/home`, `feature/tasks`, `feature/habits`, `feature/discover`, `feature/search`, `feature/notifications`, `feature/profile`
- Modify app shell/navigation under: `app/navigation` and `app/LifeLabApp.kt`

- [ ] Keep seven bottom tabs but replace letter icons with Material icons.
- [ ] Use Chinese-first labels and concise product copy across all primary screens.
- [ ] Normalize page structure: top header, primary action, content card/list, loading, empty, and error state.
- [ ] Add blue/white visual identity without purple/orange/beige gradients.
- [ ] Keep UI dense and app-like, not marketing-page-like.

## Task 6: WebView Lab

**Files:**
- Create: `app/src/main/java/com/example/lifelab/feature/weblab/**`
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabDestination.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt`
- Modify Home/Profile entry points

- [ ] Add non-bottom route `web_lab`.
- [ ] Load `https://hufaei.github.io/` in a WebView.
- [ ] Support page loading progress, refresh, back within WebView history, error state, and external link handling.
- [ ] Add entry from Profile "实验室" and optionally Home recommendation.

## Task 7: Integration QA

**Files:**
- Update tests and docs touched by the implementation.

- [ ] Run `./gradlew.bat :app:testDebugUnitTest --no-daemon`.
- [ ] Run `./gradlew.bat :app:lintDebug --no-daemon`.
- [ ] Run `./gradlew.bat :app:assembleDebug --no-daemon`.
- [ ] If Android SDK is unavailable locally, record the exact failure and leave CI-compatible commands in the handoff.
- [ ] Review that no feature directly accesses Room/DataStore from Compose UI and no photo binary is stored in SQLite.
