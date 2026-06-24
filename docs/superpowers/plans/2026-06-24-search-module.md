# Search Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the LifeLab Search module with input, history, hot keywords, results, filters, empty state, and error state.

**Architecture:** Keep Search self-contained under `feature/search` with domain models and repository contract, an in-memory repository implementation, and a state-driven Compose presentation layer. Use the platform-provided ViewModel/coroutine test dependencies and `core.testing.MainDispatcherRule`; do not modify shared Gradle or core contracts in this module.

**Tech Stack:** Kotlin, Jetpack Compose, AndroidX ViewModel, StateFlow, Coroutines, JUnit.

---

## File Structure

- Create: `app/src/main/java/com/example/lifelab/feature/search/domain/SearchModels.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/domain/SearchHistoryPolicy.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/domain/SearchRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/data/InMemorySearchRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchRoute.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchUiState.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchViewModel.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/search/domain/SearchHistoryPolicyTest.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/search/presentation/SearchViewModelTest.kt`

### Task 1: Domain Rules

**Files:**
- Create: `app/src/main/java/com/example/lifelab/feature/search/domain/SearchModels.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/domain/SearchHistoryPolicy.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/domain/SearchRepository.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/search/domain/SearchHistoryPolicyTest.kt`

- [ ] Write failing tests for blank query ignore, duplicate promotion, trim, and max history size.
- [ ] Run `./gradlew :app:testDebugUnitTest --tests "*SearchHistoryPolicyTest"` and confirm the expected missing-class failure.
- [ ] Implement domain models, repository contract, and history policy.
- [ ] Re-run the targeted test and confirm pass.

### Task 2: ViewModel State

**Files:**
- Create: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchUiState.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchViewModel.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/search/presentation/SearchViewModelTest.kt`

- [ ] Write failing ViewModel tests for initial hot keywords/history, query submit, empty result, error result, filter re-search, hot keyword submit, clear history, and retry. Use `com.example.lifelab.core.testing.MainDispatcherRule`.
- [ ] Run `./gradlew :app:testDebugUnitTest --tests "*SearchViewModelTest"` and confirm the expected missing-class failure.
- [ ] Implement `SearchUiState` and `SearchViewModel`.
- [ ] Re-run the targeted ViewModel test and confirm pass.

### Task 3: Repository And UI

**Files:**
- Create: `app/src/main/java/com/example/lifelab/feature/search/data/InMemorySearchRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchRoute.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`

- [ ] Implement an in-memory repository with seed content and hot keywords.
- [ ] Replace placeholder `SearchRoute` with a ViewModel-backed route.
- [ ] Implement `SearchScreen` with input, submit, history, hot keywords, filter chips, content, empty, and error rendering.
- [ ] Run Search unit tests plus `./gradlew :app:testDebugUnitTest`.

### Task 4: Review And Verification

**Files:**
- Review all Search files and Search module docs.

- [ ] Statically verify Search does not import other feature packages.
- [ ] Review tests for behavior value and remove low-value assertions.
- [ ] Run `git diff --check`.
- [ ] Run best available Gradle verification: `./gradlew :app:testDebugUnitTest`, `./gradlew :app:assembleDebug`, and `./gradlew :app:lint` if local SDK allows.
