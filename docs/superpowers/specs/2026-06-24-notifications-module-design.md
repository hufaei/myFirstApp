# Notifications Module Design

## 1. Purpose

`Notifications` belongs to the `Account And Notifications Slice`. Its current goal is to replace the platform placeholder with a minimal formal implementation for the current account notification surface.

This module must cover:

- in-app message list
- message status changes
- notification settings association for the current account
- system notification integration placeholder
- empty and error states

This module does not implement Android OS notification posting, Profile internals, login, Room, DataStore, remote APIs, migration, or historical compatibility.

## 2. Boundaries

Default changes stay under:

- `app/src/main/java/com/example/lifelab/feature/notifications/**`
- `app/src/test/java/com/example/lifelab/feature/notifications/**`
- `app/src/androidTest/java/com/example/lifelab/feature/notifications/**`

Shared dependencies, `core/**`, app navigation, and other feature packages are owned by the platform/main coordination thread. This module must request those changes in handoff notes instead of modifying them directly.

## 3. Architecture

Use the existing single-module, feature-first structure:

```text
feature/notifications/
  data/
  domain/
  presentation/
```

Domain owns `NotificationMessage`, `NotificationStatus`, `NotificationSettings`, repository contracts, and use cases. Data provides an in-memory repository with deterministic default data. Presentation owns `NotificationsViewModel`, `NotificationsUiState`, events, and pure Compose screen rendering.

The module uses this flow:

1. `NotificationsRoute` obtains a `NotificationsViewModel`.
2. `ViewModel` observes repository flows.
3. UI emits `NotificationsUiEvent`.
4. `ViewModel` calls use cases.
5. Use cases validate transitions and update the repository.
6. `UiState` renders loading, content, empty, and error modes.

## 4. Domain Rules

Message status values:

- `Unread`
- `Read`
- `Archived`

Rules:

- Marking an unread message as read changes it to `Read`.
- Marking an already read message as read is idempotent.
- Archived messages are terminal for read actions.
- Archiving unread or read messages changes status to `Archived`.
- Unknown message ids return a validation failure.

Settings:

- `NotificationSettings` represents current-account preferences inside this module.
- `inAppMessagesEnabled` controls whether the list is available.
- `systemNotificationsEnabled` controls the system notification integration placeholder.
- Disabling in-app messages yields an empty-state style screen with settings context, not a crash.

## 5. UI States

`NotificationsUiState` exposes:

- `isLoading`
- `messages`
- `settings`
- `systemIntegration`
- `errorMessage`

Screen behavior:

- Loading: progress indicator and stable loading text.
- Content: message list, unread/read/archive actions, settings toggles, and system integration placeholder.
- Empty: clear message when no visible messages exist or in-app messages are disabled.
- Error: stable error text and retry action.

## 6. Acceptance Criteria

- Notifications route no longer uses `PlaceholderFeatureScreen`.
- In-app messages render with title, body, category, created time label, and status label.
- Unread messages can be marked read.
- Unread or read messages can be archived.
- Archived messages are not shown in the active list.
- System notification integration placeholder reflects the current setting.
- Empty and error states are renderable by pure screen state.
- Unit tests cover status transitions, settings updates, and ViewModel state changes.
- Tests avoid low-value negative UI assertions.

## 7. Verification

Run the best available local verification:

```powershell
./gradlew.bat :app:testDebugUnitTest --tests "*notifications*"
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:assembleDebug
git diff --check
```

If Android SDK or Gradle environment blocks a command, record the exact blocker in the final handoff.
