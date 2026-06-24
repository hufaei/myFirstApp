# Profile Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the LifeLab Profile module to the current account and notifications slice target.

**Architecture:** Keep Profile self-contained under `feature/profile` with domain, data and presentation packages. Use a module-local repository for the current slice and expose state through a ViewModel-backed Compose screen.

**Tech Stack:** Kotlin, Jetpack Compose, ViewModel, StateFlow, Coroutines test, JUnit.

---

## File Structure

- Create: `app/src/main/java/com/example/lifelab/feature/profile/domain/ProfileModels.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/profile/domain/ProfileRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/profile/data/InMemoryProfileRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt`
- Create tests under `app/src/test/java/com/example/lifelab/feature/profile/**`

### Task 1: Domain And Data

**Files:**
- Create: `app/src/main/java/com/example/lifelab/feature/profile/domain/ProfileModels.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/profile/domain/ProfileRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/profile/data/InMemoryProfileRepository.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/profile/domain/UserPreferenceTest.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/profile/domain/ProfileOverviewTest.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/profile/data/InMemoryProfileRepositoryTest.kt`

- [x] Write failing tests for default preferences, login-state overview and repository updates.
- [x] Run the targeted tests and record the local Android SDK blocker if Gradle cannot configure.
- [x] Implement the minimal domain and repository code.
- [ ] Re-run targeted tests after the local SDK is available.

### Task 2: ViewModel

**Files:**
- Create: `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileViewModel.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/profile/presentation/ProfileViewModelTest.kt`

- [x] Write failing ViewModel tests using `MainDispatcherRule`.
- [x] Implement `ProfileUiState`, `ProfileUiEvent` and `ProfileViewModel`.
- [x] Run the targeted ViewModel tests or record the local SDK blocker.

### Task 3: Compose UI

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt`

- [x] Replace the generic placeholder with a Profile route bound to `ProfileViewModel`.
- [x] Implement a pure `ProfileScreen` for account header and preference controls.
- [x] Keep notification settings as a local entry row without depending on notifications internals.

### Task 4: Integration Review And Verification

**Files:**
- Review all Profile module files and Profile tests.

- [ ] Check that only Profile module files and Profile design/plan docs changed.
- [ ] Check that `app/build.gradle.kts` has no Profile-local diff.
- [ ] Run `git diff --check`.
- [ ] Run the best available Gradle verification.
- [ ] Report verification evidence and any local environment blockers.
