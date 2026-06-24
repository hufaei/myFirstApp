# Discover Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Discover placeholder with a minimal production-shaped mixed content list with category filtering, empty state, error state, and focused unit tests.

**Architecture:** Keep all business and UI implementation inside `feature/discover`. Use `domain` for models and filtering use case, `data` for an in-memory repository, and `presentation` for Route, Screen, ViewModel, state, and events.

**Tech Stack:** Kotlin, Jetpack Compose, ViewModel, StateFlow, Coroutines test, JUnit, `MainDispatcherRule`.

---

## File Structure

- Create: `app/src/main/java/com/example/lifelab/feature/discover/domain/DiscoverContent.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/domain/DiscoverCategory.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/domain/DiscoverRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/domain/LoadDiscoverContentUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/data/InMemoryDiscoverRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverRoute.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverScreen.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverUiState.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverViewModel.kt`
- Create tests under `app/src/test/java/com/example/lifelab/feature/discover/**`

## Task 1: Domain And Data

**Files:**
- Create: `app/src/main/java/com/example/lifelab/feature/discover/domain/**`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/data/**`
- Test: `app/src/test/java/com/example/lifelab/feature/discover/domain/**`
- Test: `app/src/test/java/com/example/lifelab/feature/discover/data/**`

- [x] Define Discover content models for article, course, product offer, and membership offer.
- [x] Define category values and filtering rules.
- [x] Add repository contract.
- [x] Add load use case that filters successful repository results and propagates failures.
- [x] Add in-memory repository seed data covering every content type.
- [x] Add focused unit tests for filtering, failure propagation, and seed coverage.

## Task 2: Presentation And ViewModel

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverRoute.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverScreen.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverUiState.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverViewModel.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/discover/presentation/**`

- [x] Replace placeholder route with a real ViewModel-backed route.
- [x] Add finite list state for loading, content, empty, and error.
- [x] Add UI events for category selection and retry.
- [x] Render title, category controls, mixed content cards, empty state, and error retry.
- [x] Add ViewModel tests using `com.example.lifelab.core.testing.MainDispatcherRule`.

## Task 3: Module Review And Verification

**Files:**
- Review all Discover module files and Discover docs.

- [x] Check no module work changes `app/build.gradle.kts`, `core`, app navigation, or other feature internals.
- [x] Check tests assert meaningful behavior and avoid low-value negative UI assertions.
- [x] Run `git diff --check`.
- [x] Run `./gradlew :app:testDebugUnitTest --tests "*Discover*"`; local execution is blocked by missing Android SDK configuration.
- [x] Run static structure checks for Discover source and test files.
