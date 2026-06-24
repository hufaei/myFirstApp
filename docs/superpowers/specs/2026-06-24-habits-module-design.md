# Habits Module Design

## 1. Purpose

`Habits` is the habit-tracking part of the Productivity Slice. This slice turns the platform placeholder into a minimal formal feature that supports a habit list, daily check-in, streak calculation, summary stats, and reminder settings.

The module stays inside `feature/habits` and does not define shared database, notification, or navigation contracts. Those should move to `core` only when a second real consumer appears or when the main platform session explicitly owns the shared contract.

## 2. Scope

Included:

- Habit list with sample local data.
- Daily check-in for a habit.
- Same natural-day duplicate check-in protection.
- Streak count derived from check-in dates.
- Summary stats for total habits, checked in today, active reminders, and longest streak.
- Reminder enabled/time settings at the habit level.
- Unit tests for check-in rules, streak boundaries, state transitions, and ViewModel behavior.

Not included:

- Room persistence schema.
- System notification scheduling.
- Cross-feature habit insight cards.
- Historical data migration or old UI/API compatibility.

## 3. Architecture

The module follows the existing feature-first structure:

```text
feature/habits/
  data/
    InMemoryHabitRepository.kt
  domain/
    model/
    repository/
    usecase/
  presentation/
    HabitsRoute.kt
    HabitsScreen.kt
    HabitsUiState.kt
    HabitsViewModel.kt
```

`HabitsRoute` owns ViewModel collection and passes stable state to `HabitsScreen`. `HabitsScreen` renders the content and emits user actions. `HabitsViewModel` calls `CheckInHabitUseCase`, `CalculateHabitStatsUseCase`, and `HabitRepository`.

The current data implementation is intentionally in-memory. This gives the first vertical slice a real behavior loop without creating shared Room contracts before the platform owner has accepted them.

## 4. Domain Rules

- A habit has `id`, `name`, `frequency`, `streakCount`, `lastCheckInDate`, `reminder`, and check-in dates.
- The first check-in on a date is effective.
- A second check-in on the same date returns an already-checked-in result and does not increase streak.
- Streak is derived from consecutive check-in dates ending at the latest effective check-in date.
- If yesterday is checked in, today's check-in continues the streak.
- If there is a missed day before today, today's check-in restarts the streak at one.

## 5. UI State

The page supports the expected states:

- `Loading`: repository flow has not emitted yet.
- `Content`: habits and stats are available.
- `Empty`: repository emits no habits.
- `Error`: reserved for failed operations or future data sources.

One-shot user feedback can be represented as a short message on state for this slice. It is cleared by an explicit event.

## 6. Acceptance

This module is complete for the current slice when:

- The Habits tab no longer shows a placeholder.
- Users can see habits, streaks, check-in controls, stats, and reminder controls.
- Check-in updates list and stats.
- Duplicate same-day check-in gives feedback without changing streak.
- Reminder updates change the target habit and stats.
- Unit tests cover `CheckInHabitUseCase`, streak rules, state transitions, and `HabitsViewModel`.
- No app/core/shared dependency changes are introduced by this module work.
