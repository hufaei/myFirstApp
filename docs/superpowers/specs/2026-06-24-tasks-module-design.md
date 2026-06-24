# Tasks Module Design

## 1. Purpose

`Tasks` is the first concrete business module in the Productivity Slice. This module must replace the platform placeholder with a minimal but formal task workflow that exercises domain rules, state-driven UI, use cases, repository boundaries, and ViewModel tests.

This design covers only the `Tasks` module. It does not introduce shared database, DataStore, reminder, habit, or cross-feature behavior.

## 2. Scope

This delivery includes:

- Task list with loading, content, empty, and error-capable state model
- Task detail view
- Create and edit form
- Complete and restore actions
- List filtering by all, active, and completed tasks
- Domain model, repository contract, in-memory repository implementation, and use cases inside `feature/tasks`
- Focused unit tests for use cases, state transitions, and ViewModel behavior

This delivery does not include:

- Room schema or persisted storage
- Notifications or reminders
- Cross-feature task suggestions
- Historical compatibility for old task APIs, models, or UI behavior

## 3. Architecture

The module stays inside:

```text
app/src/main/java/com/example/lifelab/feature/tasks/
  data/
  domain/
  presentation/
```

`presentation` depends on `domain`. `data` implements the `domain` repository contract. Domain code has no dependency on Compose or Android UI.

The current route remains `TasksRoute(contentPadding)`, so the root navigation graph does not need a new task-detail route. List, detail, and editor screens are module-internal modes controlled by `TaskListViewModel`.

Until the platform branch introduces feature-level dependency injection wiring, the production route uses the module-local in-memory repository as the minimal data source. The ViewModel remains testable through injected use cases, and the repository implementation stays inside `feature/tasks/data`.

## 4. Domain Model

`Task` fields:

- `id`
- `title`
- `description`
- `status`
- `priority`
- `dueAt`
- `tags`
- `createdAt`
- `updatedAt`

Rules:

- Blank titles are rejected.
- Archived tasks are not part of the current minimal UI.
- Completed tasks can be restored to active.
- Completing or restoring a missing task returns a storage failure.
- Editing a missing task returns a storage failure.
- `dueAt == null` means no due date.

## 5. State And UI

The ViewModel exposes one stable `TasksUiState`.

Important state dimensions:

- `isLoading`
- `tasks`
- `selectedFilter`
- `selectedTask`
- `mode`: list, detail, or editor
- `editor`
- `message`

The UI should render useful positive states:

- Empty list text when no task matches the selected filter
- Task rows with title, status, priority, and due label
- Detail fields and complete/restore actions
- Editor fields and save action

## 6. Acceptance Criteria

- Opening the Tasks destination shows a real task list UI instead of the placeholder.
- A user can create a task with title, description, priority, tags, and optional due label.
- A user can open task detail from the list.
- A user can edit an existing task and see updated data in list/detail.
- A user can complete an active task.
- A user can restore a completed task.
- A user can filter all, active, and completed tasks.
- Core domain use cases have focused unit tests for success and failure paths.
- ViewModel tests cover initial load, create, edit, complete, restore, and filter state transitions.
- Tests avoid low-value negative UI assertions and do not test implementation details such as internal collection storage.

## 7. Shared Dependency Ownership

`app/build.gradle.kts`, `core/**`, and shared test contracts are owned by the platform controller thread. The Tasks module consumes `com.example.lifelab.core.testing.MainDispatcherRule` and shared dependencies from the platform branch, but does not modify those shared files.
