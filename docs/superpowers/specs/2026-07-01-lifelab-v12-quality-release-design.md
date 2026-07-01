# LifeLab v1.2 Quality Release Design

## 1. Goal

LifeLab v1.2 turns the current integrated demo into a more credible daily-use slice. The release must fix the system notification gap, make habits richer than a static check-in list, improve search and discovery usefulness, and reduce the current "demo cards everywhere" visual feeling.

This is still a local-first learning app. The release should stay inside the existing single `:app` module and current `app/core/feature` package boundaries.

## 2. Scope

v1.2 includes five workstreams:

1. Notification reliability: users can understand and grant system notification permission, habit reminders do not silently fail, and notification settings describe what the app actually controls.
2. Habit enrichment: users can create and edit habits with reminder settings instead of only using seed data.
3. Search and discovery usefulness: search results and discovery content lead somewhere useful, with lightweight details and actions rather than dead-end cards.
4. Visual polish: shared theme/components and the main work screens become quieter, denser, and more consistent.
5. Release readiness: version is bumped to `1.2.0`, core tests pass, lint passes, review is completed, and release ordering is enforced.

Out of scope:

- Backend, login, payments, cloud sync, or remote push notifications.
- Broad UI automation tests.
- Large architecture rewrites or Gradle multi-module migration.
- Full commercial product completeness.

## 3. Notification Design

Habit reminders remain local Android notifications scheduled from Room-backed habit state. The app must stop treating stored reminder state as proof that system notifications can be posted.

The UI should expose three concepts clearly:

- In-app messages: the message center list inside LifeLab.
- Habit reminders: scheduled local reminders controlled from Habits.
- Android notification permission: the system gate required on Android 13+.

When permission is missing, the app should show a direct, readable explanation and an action to request permission from the relevant screen. If permission is denied, the app should not claim reminders are fully active. Existing reminders may remain stored, but the UI must surface that delivery is blocked by system permission.

## 4. Habit Design

Habits should support:

- Create habit with name.
- Optional reminder enablement.
- Reminder time using the existing simple time adjustment pattern.
- Reminder priority using High, Normal, Low.
- Edit existing habit name and reminder settings.

The first version does not need complex recurrence, habit categories, deletion undo, analytics charts, or remote sync. Keep the model close to the current `Habit`, `HabitReminder`, `HabitRepository`, and `HabitsViewModel` flow.

## 5. Search And Discovery Design

Search should stop feeling like a static result list. Selecting a result should either navigate to a relevant app surface or open a lightweight local detail view when no exact destination exists.

Discovery should provide a lightweight detail experience for articles, courses, products, and membership offers. Details can be local-only and based on existing seeded content. This should not introduce network calls or payments.

## 6. Visual Design

The app should feel like a quiet productivity tool, not a marketing page or a loose demo collection.

Direction:

- Reduce light-mode blue dominance.
- Keep Material 3 and existing component structure.
- Use restrained background and card colors.
- Make dense work screens easier to scan.
- Keep cards for repeated content items, not for every page section.
- Keep buttons icon-led where an icon is obvious.
- Avoid decorative gradients, orbs, and oversized hero styling.

## 7. Testing Policy

Tests may be added, but they must stay concise and core.

Required test style:

- Focus on domain, repository, ViewModel, and state-transition behavior.
- Prefer one test per meaningful behavior.
- Do not add broad UI screenshot tests.
- Do not add tests whose main assertion is `not have`, `not exist`, or `not visible`.
- Avoid testing implementation details, private layout shape, or one-off copy unless the copy is the behavior.

Recommended coverage:

- Permission-state mapping and notification-blocked state.
- Habit create/edit ViewModel flow.
- Search result selection/detail state.
- Version identity.

## 8. Release Rules

The release manager must review worker changes before integration and must run appropriate tests.

Release APK rule:

1. Complete code review and verification.
2. Create the release commit with the version bump and release notes updates.
3. Create tag `v1.2.0` on that release commit.
4. Only after the release commit and tag exist, start APK packaging.

No APK build starts before step 3 is complete.
