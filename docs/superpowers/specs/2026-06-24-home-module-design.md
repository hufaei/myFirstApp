# Home Module Design

## 1. Purpose

Home is the entry point for the current Content Discovery slice. Its first deliverable is a minimal formal screen that replaces the platform placeholder with a recommendation area, an aggregated feed, refresh handling, empty state, and error recovery.

This module does not implement Discover or Search internals. It uses local seeded content until the wider content slice introduces real repositories.

## 2. Scope

In scope:

- Home recommendation entries.
- Aggregated feed cards for task summary, habit insight, and discovery teaser.
- Initial loading, pull-style refresh command, empty state, and error state.
- Unit tests for feed building, state transitions, and ViewModel behavior.

Out of scope:

- Pagination.
- Network, Room, DataStore, or cross-feature data access.
- Navigation into Tasks, Habits, Discover, or Search internals.
- Shared app/core contract changes.

## 3. Architecture

Home stays inside `feature/home` and follows the project feature-first package boundary:

```text
feature/home/
  data/
  domain/
  presentation/
```

Domain owns `HomeFeedSeed`, `HomeFeedContent`, `HomeFeedItem`, `HomeFeedRepository`, and `BuildHomeFeedUseCase`.

Data provides `SeedHomeFeedRepository`, a small in-memory repository used as the current formal source.

Presentation owns `HomeUiState`, `HomeUiEvent`, `HomeViewModel`, `HomeRoute`, and `HomeScreen`.

## 4. State Flow

1. `HomeRoute` obtains `HomeViewModel` and observes `uiState`.
2. `HomeViewModel` loads content through `BuildHomeFeedUseCase`.
3. The use case maps seed data into stable feed content.
4. `HomeScreen` renders loading, content, refreshing, empty, or error views.
5. Refresh failures keep existing content and expose the latest error message.
6. Retry from an initial error can replace the error with loaded content.

## 5. Acceptance

- Home no longer renders `PlaceholderFeatureScreen`.
- Recommended entries and feed cards are visible from seeded content.
- Empty seed produces an explicit empty feed item.
- Repository failure is not converted into empty content.
- Initial load failure, refresh failure, and retry success are covered by unit tests.
- Home changes stay within `feature/home`, corresponding Home tests, and this module documentation.
