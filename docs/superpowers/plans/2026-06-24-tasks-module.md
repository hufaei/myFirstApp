# Tasks Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Tasks placeholder with a minimal formal task workflow covering list, detail, create/edit, complete/restore, and filters.

**Architecture:** Keep all business implementation inside `feature/tasks` with `data`, `domain`, and `presentation` packages. Use an in-memory repository for this slice and keep route-level navigation internal to the module to avoid cross-module changes.

**Tech Stack:** Kotlin, Jetpack Compose, AndroidX ViewModel, Coroutines Flow, JUnit, kotlinx-coroutines-test.

---

## File Structure

- Create: `app/src/main/java/com/example/lifelab/feature/tasks/domain/Task.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/domain/TaskRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/domain/CreateTaskUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/domain/UpdateTaskUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/domain/CompleteTaskUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/domain/RestoreTaskUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/domain/GetTaskDetailUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/domain/ObserveTasksUseCase.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/data/InMemoryTaskRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksUiState.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TaskListViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksRoute.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/tasks/domain/TaskUseCaseTest.kt`
- Create: `app/src/test/java/com/example/lifelab/feature/tasks/presentation/TaskListViewModelTest.kt`
- Consume shared ViewModel and coroutine test dependencies from the platform branch. Do not modify `app/build.gradle.kts`.

### Task 1: Domain And Data

**Files:**
- Create/modify only `app/src/main/java/com/example/lifelab/feature/tasks/domain/**`
- Create/modify only `app/src/main/java/com/example/lifelab/feature/tasks/data/**`
- Create/modify only `app/src/test/java/com/example/lifelab/feature/tasks/domain/**`

- [ ] **Step 1: Write failing use case tests**

Cover create title validation, update missing task failure, complete task state transition, and restore task state transition.

- [ ] **Step 2: Run targeted tests and confirm red**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "*TaskUseCaseTest"`

- [ ] **Step 3: Implement domain model, repository contract, use cases, and in-memory repository**

Keep storage implementation deterministic and feature-local.

- [ ] **Step 4: Re-run targeted tests and confirm green**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "*TaskUseCaseTest"`

### Task 2: ViewModel State

**Files:**
- Create/modify only `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksUiState.kt`
- Create/modify only `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TaskListViewModel.kt`
- Create/modify only `app/src/test/java/com/example/lifelab/feature/tasks/presentation/**`
- Do not modify `app/build.gradle.kts`; if dependencies are missing, stop and report the platform baseline mismatch.

- [ ] **Step 1: Write failing ViewModel tests**

Cover initial load, filter switching, create, edit, complete, and restore. Use `com.example.lifelab.core.testing.MainDispatcherRule`.

- [ ] **Step 2: Run targeted tests and confirm red**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "*TaskListViewModelTest"`

- [ ] **Step 3: Implement Tasks UI state and ViewModel**

Expose stable `StateFlow<TasksUiState>` and event methods used by the Compose route.

- [ ] **Step 4: Re-run targeted tests and confirm green**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "*TaskListViewModelTest"`

### Task 3: Compose Route And Screen

**Files:**
- Modify only `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksRoute.kt`
- Read but do not modify domain/data files unless integration reveals a compile issue.

- [ ] **Step 1: Replace placeholder route**

Render list, detail, and editor modes from `TasksUiState`.

- [ ] **Step 2: Wire user actions**

Connect create, edit, complete, restore, filter, detail, back, and save actions to `TaskListViewModel`.

- [ ] **Step 3: Keep UI tests out unless a stable local Android UI runner is available**

Prefer ViewModel tests for state transitions in this slice. Document manual UI verification steps if instrumentation cannot run locally.

### Task 4: Integration And Final Verification

**Files:**
- Review all changed files.
- No new feature-internal files outside `feature/tasks`.

- [ ] **Step 1: Static review**

Check boundary pollution, business rule placement, test quality, and Compose state flow.

- [ ] **Step 2: Run verification**

Run:

```powershell
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:assembleDebug
git diff --check
```

- [ ] **Step 3: Commit**

Commit only the Tasks module changes and Tasks design/plan docs.
