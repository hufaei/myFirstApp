# Discover Module Design

## Purpose

Discover delivers the first standalone content discovery experience for LifeLab. It turns the previous placeholder route into a minimal production-shaped module that can show mixed content, filter by category, and represent loading, empty, and error states through a stable state model.

This module is intentionally scoped to `feature/discover`. It does not implement Home feed composition, Search integration, pagination, image loading, networking, Room caching, or cross-feature recommendations.

## Scope

Included:

- Mixed article, course, product, and membership content.
- Category filtering for all content, articles, courses, offers, and membership.
- A Discover route, screen, ViewModel, UI state, and UI events.
- An in-memory repository with stable seed content for the current no-backend stage.
- Unit tests for content filtering, repository seed data, list state transitions, and ViewModel behavior.

Excluded:

- Shared `core` model promotion for `FeedItem`.
- Changes to app navigation contracts.
- Changes to Home, Search, Profile, Notifications, Tasks, or Habits internals.
- Historical API, data, or UI compatibility.

## Architecture

Discover follows the project feature-first package structure:

```text
feature/discover/
  data/
  domain/
  presentation/
```

`domain` owns Discover-specific models and repository contracts. The module uses a `DiscoverContent` sealed interface for mixed content:

- `Article`
- `Course`
- `Offer.Product`
- `Offer.Membership`

`data` owns `InMemoryDiscoverRepository`, which provides stable sample content until a real remote or local data source exists.

`presentation` owns `DiscoverRoute`, `DiscoverScreen`, `DiscoverViewModel`, `DiscoverUiState`, and `DiscoverUiEvent`. Route binds the ViewModel to UI events; Screen is pure rendering.

## State Flow

1. `DiscoverRoute` creates or receives `DiscoverViewModel`.
2. `DiscoverViewModel` loads content through `LoadDiscoverContentUseCase`.
3. The use case calls `DiscoverRepository` and applies category filtering.
4. The ViewModel maps results into `DiscoverUiState`.
5. `DiscoverScreen` renders loading, content, empty, or error states.
6. Category selection and retry actions are sent back as `DiscoverUiEvent`.

The list state is finite and explicit:

- `Loading`
- `Content`
- `Empty`
- `Error`

## Category Rules

- `All`: all content in repository order.
- `Articles`: only article content.
- `Courses`: only course content.
- `Offers`: product and membership offers.
- `Membership`: only membership offers.

An empty filtered result is not an error. It is presented as an empty state.

## Testing

Unit tests cover behavior instead of implementation details:

- Category matching rules.
- Use case success, filtering, and repository failure propagation.
- In-memory seed coverage for all content types.
- ViewModel initial load, category selection, empty state, error state, and retry recovery.

Compose instrumentation tests are not required for this minimal module handoff because the current machine is responsible for JVM tests, build, and static validation only. Manual runtime verification can confirm the route visually in a full Android environment.

## Acceptance

Discover is complete for this module handoff when:

- The Discover navigation destination no longer renders the generic placeholder.
- The default Discover view shows mixed article, course, product, and membership content.
- Category selection updates `selectedCategory` and filters the list.
- Empty filtered results show a deliberate empty state.
- Repository failure shows an error state with retry.
- Retry can recover after a transient failure.
- New tests are focused on domain behavior, list state transitions, and ViewModel behavior.
- No Discover work modifies `app/build.gradle.kts`, `core`, app navigation, or other feature internals.
