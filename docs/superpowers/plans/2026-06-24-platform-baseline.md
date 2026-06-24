# Platform Baseline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the Kotlin JVM template into the LifeLab Android platform baseline with clear app/core/feature boundaries.

**Architecture:** Use a single Android `app` module with feature-first package boundaries. The main session owns platform architecture; feature modules are handed to separate Codex threads only after this baseline is committed.

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose, Navigation Compose, Hilt, JUnit, Compose UI Test.

---

## File Structure

- Create: `app/build.gradle.kts` for Android application configuration.
- Create: `app/src/main/AndroidManifest.xml` for application and activity registration.
- Create: `app/src/main/java/com/example/lifelab/app/LifeLabApplication.kt` for Hilt application setup.
- Create: `app/src/main/java/com/example/lifelab/app/MainActivity.kt` for Compose host.
- Create: `app/src/main/java/com/example/lifelab/app/LifeLabApp.kt` for root app shell.
- Create: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabDestination.kt` for top-level destination contract.
- Create: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt` for root navigation graph.
- Create: `app/src/main/java/com/example/lifelab/core/common/AppError.kt` and `AppResult.kt`.
- Create: `app/src/main/java/com/example/lifelab/core/ui/PlaceholderFeatureScreen.kt`.
- Create: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`.
- Create one placeholder route per feature under `app/src/main/java/com/example/lifelab/feature/<name>/presentation/`.
- Create: `app/src/test/java/com/example/lifelab/core/common/AppResultTest.kt`.
- Create: `app/src/test/java/com/example/lifelab/app/navigation/LifeLabDestinationTest.kt`.
- Create: `app/src/androidTest/java/com/example/lifelab/app/LifeLabAppSmokeTest.kt`.
- Create: `docs/07-coordination/module-thread-prompts.md`.
- Modify: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `.gitignore`, `README.md`, `docs/README.md`, `docs/06-engineering/dev-commands.md`.
- Delete: `src/main/kotlin/Main.kt`.

### Task 1: Android Gradle Skeleton

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `build.gradle.kts`
- Modify: `gradle.properties`
- Create: `app/build.gradle.kts`
- Delete: `src/main/kotlin/Main.kt`

- [ ] **Step 1: Convert settings to Android project structure**

Update `settings.gradle.kts` with plugin repositories, dependency repositories, `rootProject.name = "LifeLab"`, and `include(":app")`.

- [ ] **Step 2: Configure root plugins**

Use root `build.gradle.kts` only for plugin version declarations and no JVM application source set.

- [ ] **Step 3: Configure app module**

Create `app/build.gradle.kts` with Android application, Kotlin Android, Compose, Hilt, and KSP plugins. Use namespace and application id `com.example.lifelab`.

- [ ] **Step 4: Remove JVM template entry**

Delete `src/main/kotlin/Main.kt`.

- [ ] **Step 5: Run static file verification**

Run a PowerShell check that confirms `settings.gradle.kts` includes `:app`, `app/build.gradle.kts` exists, and `src/main/kotlin/Main.kt` no longer exists.

### Task 2: Core Result And Error Model

**Files:**
- Create: `app/src/main/java/com/example/lifelab/core/common/AppError.kt`
- Create: `app/src/main/java/com/example/lifelab/core/common/AppResult.kt`
- Create: `app/src/test/java/com/example/lifelab/core/common/AppResultTest.kt`

- [ ] **Step 1: Write failing result tests**

Add tests for success value exposure and validation error message exposure.

- [ ] **Step 2: Run targeted test**

Run `./gradlew :app:testDebugUnitTest --tests "*AppResultTest"` if Android SDK is available. If not, record the SDK blocker and continue with static verification.

- [ ] **Step 3: Implement minimal model**

Implement `AppError` and `AppResult` as sealed interfaces.

- [ ] **Step 4: Re-run targeted test or static verification**

Use the same command when possible. Otherwise verify files contain the expected sealed interfaces and test names.

### Task 3: Compose Host And Navigation Contract

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/lifelab/app/LifeLabApplication.kt`
- Create: `app/src/main/java/com/example/lifelab/app/MainActivity.kt`
- Create: `app/src/main/java/com/example/lifelab/app/LifeLabApp.kt`
- Create: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabDestination.kt`
- Create: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt`
- Create: `app/src/test/java/com/example/lifelab/app/navigation/LifeLabDestinationTest.kt`

- [ ] **Step 1: Write navigation contract test**

Add a unit test proving the top-level destinations are ordered as `Home`, `Tasks`, `Habits`, `Discover`, `Search`, `Notifications`, `Profile`.

- [ ] **Step 2: Implement destination contract**

Create `LifeLabDestination` with route and title fields plus `topLevelDestinations`.

- [ ] **Step 3: Implement Compose host**

Create application, activity, theme entry, app shell and nav host.

- [ ] **Step 4: Run targeted test or static verification**

Run the navigation unit test if possible. Otherwise statically verify destination routes and files.

### Task 4: Feature Placeholder Boundaries

**Files:**
- Create: `app/src/main/java/com/example/lifelab/core/ui/PlaceholderFeatureScreen.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`
- Create one route file for each feature package:
  - `feature/home/presentation/HomeRoute.kt`
  - `feature/tasks/presentation/TasksRoute.kt`
  - `feature/habits/presentation/HabitsRoute.kt`
  - `feature/discover/presentation/DiscoverRoute.kt`
  - `feature/search/presentation/SearchRoute.kt`
  - `feature/notifications/presentation/NotificationsRoute.kt`
  - `feature/profile/presentation/ProfileRoute.kt`

- [ ] **Step 1: Implement shared placeholder screen**

Create a small reusable screen for module placeholders.

- [ ] **Step 2: Implement feature routes**

Each feature route should call the placeholder screen and contain no business logic.

- [ ] **Step 3: Wire routes into nav host**

Map every `LifeLabDestination` to its route.

- [ ] **Step 4: Static boundary verification**

Run a PowerShell check that all seven feature route files exist under their own package.

### Task 5: UI Smoke Test And Coordination Docs

**Files:**
- Create: `app/src/androidTest/java/com/example/lifelab/app/LifeLabAppSmokeTest.kt`
- Create: `docs/07-coordination/module-thread-prompts.md`
- Modify: `README.md`
- Modify: `docs/README.md`
- Modify: `docs/06-engineering/dev-commands.md`

- [ ] **Step 1: Add Compose smoke test**

Create a basic Compose UI test for the app shell that checks the Home destination text exists.

- [ ] **Step 2: Document module thread prompt template**

Create reusable initial prompts for future module-owner Codex threads.

- [ ] **Step 3: Update documentation indexes**

Link the new design, plan, and coordination docs from existing documentation.

- [ ] **Step 4: Run final verification**

Run `git diff --check`, static structure checks, and the best available Gradle verification for the local environment.
