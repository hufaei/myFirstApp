# Notifications Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the LifeLab Notifications module with in-app messages, status transitions, settings association, system notification placeholder, and quality unit tests.

**Architecture:** Keep all business behavior inside `feature/notifications` using domain/data/presentation packages. Use an in-memory repository for the current slice; do not introduce Room, DataStore, Profile dependencies, or OS notification posting yet.

**Tech Stack:** Kotlin, Jetpack Compose, ViewModel, StateFlow, Coroutines, JUnit, kotlin-test.

---

## File Structure

- Create: `app/src/main/java/com/example/lifelab/feature/notifications/domain/NotificationMessage.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/notifications/domain/NotificationSettings.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/notifications/domain/NotificationRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/notifications/domain/ChangeMessageStatusUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/notifications/domain/UpdateNotificationSettingsUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/notifications/data/InMemoryNotificationRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsUiState.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/notifications/domain/ChangeMessageStatusUseCaseTest.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/notifications/domain/UpdateNotificationSettingsUseCaseTest.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModelTest.kt`
Shared dependencies are owned by the platform/main coordination thread. Do not modify `app/build.gradle.kts`, `core/**`, app navigation, or other feature packages from this module plan.

## Task 1: Domain And Repository

**Files:**
- Create/modify only `feature/notifications/domain/**`
- Create/modify only `feature/notifications/data/**`
- Test only `app/src/test/java/com/example/lifelab/feature/notifications/domain/**`

- [ ] Write failing tests for message status transitions: unread to read, read idempotency, archive, archived terminal behavior, and unknown id validation.
- [ ] Write failing tests for notification settings updates.
- [ ] Implement domain models, repository contract, in-memory repository, and use cases.
- [ ] Run targeted domain tests and confirm they pass.
- [ ] Self-review for boundary pollution and unnecessary compatibility logic.

## Task 2: ViewModel And Screen

**Files:**
- Create/modify only `feature/notifications/presentation/**`
- Test only `app/src/test/java/com/example/lifelab/feature/notifications/presentation/**`
- Do not modify `app/build.gradle.kts`; use the platform-provided lifecycle and coroutine test dependencies.

- [ ] Write failing ViewModel tests for initial content load, mark-read state update, archive state update, settings toggle association, empty state, and repository error state.
- [ ] Implement `NotificationsUiState`, events, `NotificationsViewModel`, and pure Compose `NotificationsScreen`.
- [ ] Replace placeholder route with the real route and ViewModel factory.
- [ ] Run targeted presentation tests and confirm they pass.
- [ ] Self-review for low-value tests, excessive UI logic, and dependency direction.

## Task 3: Integration Review And Verification

**Files:**
- Review all files touched by Tasks 1 and 2.

- [ ] Run `git diff --check`.
- [ ] Run notification-targeted unit tests.
- [ ] Run full app unit tests.
- [ ] Run `:app:assembleDebug` when the local Android environment allows it.
- [ ] Static review module boundaries, logic rules, and test quality.
- [ ] Document any remaining risk or local environment blocker.
