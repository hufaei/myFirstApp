# Profile Module Design

## 1. Purpose

`Profile` belongs to the `Account And Notifications Slice`. The current delivery target is a minimal formal implementation for the account surface, not a full identity system.

This module provides:

- Guest login-state placeholder
- Signed-in profile information model
- User preference state
- Theme setting entry
- Notification setting entry
- Profile screen replacing the platform placeholder

## 2. Scope

Included:

- Profile domain models for session, user, overview and preferences
- Module-local repository contract and in-memory implementation
- Profile ViewModel with stable `UiState` and event handling
- Compose `Route` and `Screen` for the Profile tab
- Unit tests for preference defaults, login placeholder rules and ViewModel state updates

Excluded:

- Real authentication
- Token storage
- Remote account API
- DataStore persistence
- Notifications feature internals
- Historical UI or data compatibility

## 3. Architecture

Profile remains inside the single Android `app` module:

```text
feature/profile/
  data/
  domain/
  presentation/
```

The dependency flow is:

`ProfileRoute -> ProfileViewModel -> ProfileRepository -> InMemoryProfileRepository`

The repository is module-local because Profile is the only current consumer. If another feature later needs the same preference state, the shared contract can be proposed to `core` by the coordinating session.

## 4. Domain Model

`ProfileSession` expresses login state:

- `Guest`
- `SignedIn(ProfileUser)`

`ProfileUser` contains current-account display information:

- `id`
- `displayName`
- `email`
- `membershipLabel`
- `avatarInitial`

`UserPreference` contains:

- `themeMode`
- `notificationEnabled`
- `defaultTaskFilter`
- `contentInterestTags`

`ProfileOverview` is the display-ready account summary. Guest overview must use stable placeholder text and must not invent an email or account identity.

## 5. Presentation

`ProfileUiState` contains:

- `isLoading`
- `overview`
- `preference`

`ProfileUiEvent` contains:

- `ThemeModeSelected`
- `NotificationEnabledChanged`
- `DefaultTaskFilterSelected`
- `ContentInterestTagsChanged`

No `UiEffect` is needed in this slice because there is no one-shot message or navigation event.

## 6. UI

The Profile screen should show:

- Account header with avatar initial, display name, description and optional email
- Preference area for theme mode
- Notification toggle
- Default task filter controls
- Interest tag summary
- Notification settings entry as a local settings row only

Profile must not import or call notifications feature internals.

## 7. Testing

Required unit coverage:

- `UserPreference` default values
- Preference updates preserve unchanged fields
- Guest session produces stable placeholder overview
- Signed-in session uses real user information
- ViewModel initial load maps repository state into UI state
- ViewModel events update theme, notifications, task filter and interest tags

ViewModel tests must use `com.example.lifelab.core.testing.MainDispatcherRule`.

## 8. Acceptance

This module is complete when:

- Profile tab no longer renders the generic platform placeholder
- Guest account state renders a formal placeholder
- Preference controls are backed by ViewModel state
- Unit tests cover the required domain and ViewModel behavior
- Profile changes do not modify other feature internals
- Any required shared dependency or core contract change is reported instead of implemented inside this module
