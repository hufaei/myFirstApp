# LifeLab v1.2 Quality Release Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship LifeLab v1.2.0 with reliable local notifications, richer habits, useful search/discovery flows, cleaner UI, and a controlled release process.

**Architecture:** Keep the current single-module local-first Android architecture. Work stays in existing `app`, `core`, and `feature` package boundaries, with Room/DataStore/Hilt/Compose patterns matching the current codebase.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, Android `AlarmManager`, Android notification permission APIs, JUnit coroutine tests.

## Global Constraints

- Do not start APK packaging until a release commit and tag `v1.2.0` both exist.
- Version target is `LIFELAB_VERSION_CODE=4` and `LIFELAB_VERSION_NAME=1.2.0`.
- Keep tests concise and core; do not add broad UI tests.
- Do not add tests centered on `not have`, `not exist`, or `not visible` behavior.
- Avoid unrelated refactors and avoid multi-module migration.
- Preserve user/unrelated work; never revert unrelated changes.
- Keep implementation local-first; no backend, payment, login, or remote push notification work.

---

### Task 1: Notification Reliability And Truthful Settings

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/core/notifications/HabitReminderNotificationScheduler.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsUiState.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Test: `app/src/test/java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModelTest.kt`

**Interfaces:**
- Consumes: existing `NotificationSettings`, `NotificationsUiState`, and habit reminder scheduling behavior.
- Produces: a notification screen that distinguishes in-app messages, habit reminders, and Android notification permission.

- [ ] **Step 1: Write failing ViewModel/state test**

Add a focused test proving that global notification preference still disables visible in-app messages while preserving a system integration state that can describe blocked delivery. Keep the test on state behavior, not UI layout.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.notifications.presentation.NotificationsViewModelTest'`

- [ ] **Step 3: Implement state and copy**

Add fields or derived state needed for reader-facing notification status. Update strings so the page says habit reminders are controlled from Habits and Android permission may block delivery.

- [ ] **Step 4: Update notification UI**

Show in-app messages and habit reminder/system permission information as separate rows or sections. Keep the UI compact and avoid adding a fake system notification switch that does not request Android permission.

- [ ] **Step 5: Verify task**

Run: `./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.notifications.*'`

- [ ] **Step 6: Commit**

Commit message: `fix: clarify notification permission state`

---

### Task 2: Habit Create And Edit

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/domain/repository/HabitRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/data/InMemoryHabitRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/data/RoomHabitRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsUiState.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Test: `app/src/test/java/com/example/lifelab/feature/habits/presentation/HabitsViewModelTest.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/habits/data/RoomHabitRepositoryTest.kt`

**Interfaces:**
- Consumes: existing `Habit`, `HabitReminder`, `HabitRepository.saveHabit`, and `HabitsViewModel` state flow.
- Produces: create/edit habit flow with name, reminder enabled, time, and priority.

- [ ] **Step 1: Write failing ViewModel tests**

Add tests for creating a habit and editing an existing habit. Assert resulting habit names/reminder values and active reminder count. Do not assert UI absence.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.habits.presentation.HabitsViewModelTest'`

- [ ] **Step 3: Add minimal state model**

Add a small editor state to `HabitsUiState` for create/edit mode. Keep mode simple: list plus optional editor.

- [ ] **Step 4: Implement repository/ViewModel behavior**

Use `saveHabit` for both create and update. Generate stable local IDs for new habits using a simple sanitized name/timestamp pattern.

- [ ] **Step 5: Add Compose controls**

Add an icon-led create action in the Habits header and a compact editor surface for name, reminder enabled, reminder time, and priority.

- [ ] **Step 6: Verify task**

Run: `./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.habits.*'`

- [ ] **Step 7: Commit**

Commit message: `feat: add habit create and edit flow`

---

### Task 3: Search And Discovery Details

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabDestination.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchUiState.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverUiState.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Test: `app/src/test/java/com/example/lifelab/feature/search/presentation/SearchViewModelTest.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/discover/presentation/DiscoverViewModelTest.kt`

**Interfaces:**
- Consumes: existing search results and discover content domain models.
- Produces: result/detail state so search and discovery are no longer dead-end lists.

- [ ] **Step 1: Write failing state tests**

Add one search test for selecting a result and one discover test for opening a content detail. Assert selected IDs/detail state.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.search.presentation.SearchViewModelTest' --tests 'com.example.lifelab.feature.discover.presentation.DiscoverViewModelTest'`

- [ ] **Step 3: Implement selected result/content state**

Add selected result/content state in ViewModels. Keep details local and reversible with a back action.

- [ ] **Step 4: Add detail UI**

Add compact detail surfaces with title, summary/body, type/category, and one relevant action when safe. Do not add network or payment behavior.

- [ ] **Step 5: Verify task**

Run: `./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.search.*' --tests 'com.example.lifelab.feature.discover.*'`

- [ ] **Step 6: Commit**

Commit message: `feat: add search and discovery details`

---

### Task 4: Visual Polish Pass

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`
- Modify: `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabScaffoldComponents.kt`
- Modify: `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabActionComponents.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverScreen.kt`

**Interfaces:**
- Consumes: existing Compose routes and shared LifeLab components.
- Produces: quieter, more consistent productivity UI without broad behavior changes.

- [ ] **Step 1: Scan current colors and component usage**

Run: `rg -n "Color\\(|0xFF|RoundedCornerShape|padding\\(" app/src/main/java/com/example/lifelab/core app/src/main/java/com/example/lifelab/feature`

- [ ] **Step 2: Adjust theme and shared components**

Reduce blue dominance in light mode, keep dark mode readable, and preserve Material 3. Keep card radius at 8dp or less unless existing Material shapes already apply.

- [ ] **Step 3: Tighten major screens**

Reduce redundant explanatory copy, normalize screen padding, keep repeated items as cards, and avoid nested cards.

- [ ] **Step 4: Run compile/tests**

Run: `./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.home.*' --tests 'com.example.lifelab.feature.tasks.*' --tests 'com.example.lifelab.feature.habits.*'`

- [ ] **Step 5: Commit**

Commit message: `style: polish lifelab productivity surfaces`

---

### Task 5: Integration, Review, Version, And Release Gate

**Files:**
- Modify: `gradle.properties`
- Modify: `app/src/test/java/com/example/lifelab/app/AppIdentityGradleConfigurationTest.kt`
- Modify: `README.md`
- Modify: `docs/07-coordination/integration-log.md`

**Interfaces:**
- Consumes: completed Tasks 1-4.
- Produces: reviewed branch with version `1.2.0`, release commit, and tag `v1.2.0`.

- [ ] **Step 1: Review all worker commits**

Use code-review stance. Findings lead, with file/line references. Fix Critical/Important issues before versioning.

- [ ] **Step 2: Run verification**

Run:

```bash
git diff --check
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

- [ ] **Step 3: Bump version**

Set:

```properties
LIFELAB_VERSION_CODE=4
LIFELAB_VERSION_NAME=1.2.0
```

Update `AppIdentityGradleConfigurationTest` to assert those exact values.

- [ ] **Step 4: Commit release commit**

Commit message: `chore: release lifelab v1.2.0`

- [ ] **Step 5: Create release tag**

Run: `git tag v1.2.0`

- [ ] **Step 6: Only now start APK packaging**

After the release commit and tag exist, build or trigger the release APK path. If local release signing is unavailable, use the documented GitHub Actions release workflow.
