# LifeLab v1.1.1 Notifications Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship the next usable LifeLab slice by making habit reminders real, removing notification UX misdirection, fixing language-switch flicker, tightening the UI, and making search query real local data.

**Architecture:** Keep the app local-first. Habit reminder state remains in Room, and an Android `AlarmManager` + `BroadcastReceiver` layer schedules local daily notifications from that state. Search becomes an in-process aggregator over Room-backed repositories instead of the seeded demo `search_results` cache.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Room, DataStore, Android `AlarmManager`, `NotificationManagerCompat`, GitHub Actions release flow.

---

### Task 1: Real Habit Reminder Notifications

**Files:**
- Create: `app/src/main/java/com/example/lifelab/core/notifications/HabitReminderNotificationScheduler.kt`
- Create: `app/src/main/java/com/example/lifelab/core/notifications/HabitReminderReceiver.kt`
- Create: `app/src/main/java/com/example/lifelab/core/notifications/BootReminderReceiver.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/example/lifelab/app/di/AppModule.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/domain/model/HabitReminder.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/data/HabitEntity.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/data/HabitDao.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/data/RoomHabitRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/habits/domain/HabitReminderPriorityTest.kt`

- [x] Add `HabitReminderPriority` with High/Normal/Low sort weights and notification channel importance mapping.
- [x] Add Room migration 2 -> 3 with `reminder_priority TEXT NOT NULL DEFAULT 'Normal'`.
- [x] Schedule or cancel habit alarms whenever a reminder is changed.
- [x] Add boot receiver to reschedule enabled reminders after reboot.
- [x] Request `POST_NOTIFICATIONS` at the point where a user enables system reminders on Android 13+.
- [x] Verify reminders are sorted by enabled, priority, then time.

### Task 2: Notifications Screen Truthfulness

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModel.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

- [x] Replace misleading generic system-notification switch copy with concrete habit reminder notification copy.
- [x] Keep global notification preference, but make the UI explain that scheduled reminders are controlled from Habits.
- [x] Remove redundant status paragraphs where the switch state is already obvious.

### Task 3: Language Flicker Hotfix

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/core/datastore/AppPreferencesRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/LifeLabApp.kt`
- Test: `app/src/test/java/com/example/lifelab/core/datastore/AppPreferencesRepositoryTest.kt`

- [x] Avoid applying a default System locale before DataStore emits persisted language.
- [x] Only call `AppCompatDelegate.setApplicationLocales` when the target locale differs from current locales.

### Task 4: Real Local Search

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/search/data/RoomSearchRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/data/SearchDao.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/di/AppModule.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/search/data/RoomSearchRepositoryTest.kt`

- [x] Inject task, habit, notification, and discover DAOs into search repository.
- [x] Search actual local data with normalized contains matching across titles, summaries, descriptions, tags, notification bodies, and habit names.
- [x] Keep history and hot keywords from `SearchDao`.
- [x] Tighten search UI so it reads as a practical local search tool, not demo content.

### Task 5: Compact UI And Contrast Pass

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`
- Modify: `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabScaffoldComponents.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

- [x] Increase light-mode contrast while keeping `#89C2FF` and `#E6F7FF` as the visual base.
- [x] Reduce default screen padding and card padding on dense work screens.
- [x] Remove descriptive text where controls are self-explanatory.
- [x] Keep dark-mode in a blue-black range.

### Task 6: Version, Verification, Release

**Files:**
- Modify: `gradle.properties`
- Modify: `app/src/test/java/com/example/lifelab/app/AppIdentityGradleConfigurationTest.kt`

- [x] Bump to `LIFELAB_VERSION_CODE=3` and `LIFELAB_VERSION_NAME=1.1.1`.
- [x] Run `git diff --check`.
- [x] Run local Gradle if Android SDK is available; otherwise rely on GitHub Actions and report local limitation.
- [ ] Push branch, open PR, wait for CI, merge, tag `v1.1.1`, create release, upload APK.
