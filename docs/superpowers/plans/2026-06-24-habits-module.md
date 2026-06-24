# Habits Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Habits placeholder with a minimal formal habit-tracking module.

**Architecture:** Keep all feature behavior inside `feature/habits` using domain/data/presentation subpackages. Use an in-memory repository for this slice and rely on platform-owned shared test dependencies.

**Tech Stack:** Kotlin, Jetpack Compose, ViewModel, StateFlow, Coroutines, JUnit, kotlinx-coroutines-test.

---

## File Structure

- Create: `app/src/main/java/com/example/lifelab/feature/habits/domain/model/*.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/habits/domain/repository/HabitRepository.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/habits/domain/usecase/*.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/habits/data/InMemoryHabitRepository.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsRoute.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsUiState.kt`
- Create: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsViewModel.kt`
- Create tests under `app/src/test/java/com/example/lifelab/feature/habits/**`

### Task 1: Domain And Data

- [ ] Write failing tests for first check-in, duplicate same-day check-in, continued streak, restarted streak, missing habit, and stats calculation.
- [ ] Implement habit models, repository contract, check-in use case, stats use case, and in-memory repository.
- [ ] Run targeted Habits domain/data tests.

### Task 2: ViewModel State

- [ ] Write failing ViewModel tests using `com.example.lifelab.core.testing.MainDispatcherRule`.
- [ ] Implement `HabitsUiState` and `HabitsViewModel`.
- [ ] Cover initial load, check-in state update, duplicate feedback, reminder update, and message clearing.
- [ ] Run targeted ViewModel tests.

### Task 3: Compose Screen

- [ ] Replace the placeholder route with a real route that collects ViewModel state.
- [ ] Implement a minimal screen with stats cards, habit rows, check-in buttons, and reminder controls.
- [ ] Keep UI logic presentation-only and route events back to the ViewModel.

### Task 4: Review And Verification

- [ ] Review diffs for boundary pollution outside Habits and module docs.
- [ ] Run `git diff --check`.
- [ ] Run the best available Gradle unit test command for Habits.
- [ ] Run build/static verification if the local Android SDK allows it.
- [ ] Record any local environment blockers.
