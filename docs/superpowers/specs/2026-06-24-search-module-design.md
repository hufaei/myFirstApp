# Search Module Design

## Purpose

`Search` belongs to the Content Discovery slice. This module delivers a minimum formal implementation that demonstrates input, history, hot keywords, results, filter entry, empty state, and error state without depending on Home or Discover internals.

## Scope

In scope:

- Search input and submit behavior.
- Query history with trim, duplicate promotion, and maximum size rules.
- Hot keyword entry points.
- Result states: idle, loading, content, empty, error.
- Filter entry for all, articles, offers, tasks, and habits.
- Focused unit tests for state transitions, history rules, and ViewModel behavior.

Out of scope:

- Network search.
- Room or DataStore persistence.
- Pagination.
- Compatibility with old APIs, old UI behavior, or historical data structures.
- Direct dependencies on Home, Discover, Tasks, or Habits implementation details.

## Architecture

Search stays inside `app/src/main/java/com/example/lifelab/feature/search`.

```text
feature/search/
  data/
    InMemorySearchRepository.kt
  domain/
    SearchHistoryPolicy.kt
    SearchRepository.kt
    SearchModels.kt
  presentation/
    SearchRoute.kt
    SearchScreen.kt
    SearchUiState.kt
    SearchViewModel.kt
```

The data flow is:

1. `SearchScreen` emits user actions.
2. `SearchRoute` binds screen callbacks to `SearchViewModel`.
3. `SearchViewModel` updates `SearchUiState` and calls `SearchRepository`.
4. `SearchRepository` returns domain results through `AppResult`.
5. UI renders the resulting stable state.

## Domain Rules

- Blank queries are ignored and do not enter history.
- Submitted queries are trimmed before search and history storage.
- Duplicate history entries are matched case-insensitively and promoted to the front.
- History keeps the latest eight entries.
- Filters are domain values, not free-form strings.
- Empty results are a successful search state, not an error.
- Repository failures become `SearchResultContent.Error`.

## Acceptance Criteria

- Search route no longer renders the platform placeholder.
- Users can type a query and submit it.
- Hot keywords are visible before searching and can start a search.
- History is visible, can start a search, can be cleared, and follows the module rules.
- Results can render content, empty, and error states from `UiState`.
- Filter controls exist and re-run the current submitted query when changed.
- Unit tests cover history rules and ViewModel state transitions for submit, hot keyword, filter, empty, error, and retry.
- No Search implementation depends on another feature package.

## Task Split

1. Domain and tests: create search models, repository contract, and history policy with focused unit tests.
2. ViewModel and tests: implement state transitions against fake repositories.
3. UI route and screen: replace placeholder route with state-driven Compose UI.
4. Integration review: check module boundary, test quality, and runnable verification.
