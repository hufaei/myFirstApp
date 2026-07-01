# LifeLab v1.2.1 Experience Correction Design

## Release Description

LifeLab v1.2.1 is an experience correction release for v1.2.0. It keeps the v1.2 feature set, restores the original blue LifeLab brand direction, removes demo-like copy and unnecessary intermediate actions, and makes habit reminders testable and credible on Android devices such as Xiaomi/HyperOS phones.

Release highlights:

- Restore the light brand palette to `#89C2FF` and `#E6F7FF`, with a matching blue-black dark palette.
- Tighten settings, home, search, and habit surfaces so information that fits on one line stays on one line.
- Remove redundant explanatory copy such as repeated "language" labels and repeated "today" labels.
- Make search results open their related destination directly instead of showing a second "open related page" prompt.
- Replace reminder `+30 minutes` controls with a normal time picker.
- Keep habit priority changes from immediately resorting the list.
- Express reminder priority through card UI treatment, not only through text tags.
- Add per-habit high-priority alarm mode. High priority stays an attention-grabbing notification by default; alarm mode is opt-in per habit.
- Add notification self-test tools: immediate test notification, one-minute scheduled test reminder, and short Xiaomi/HyperOS setup guidance.

## Product Decisions

### Brand

The v1.2.0 green/sage palette is not the product direction. v1.2.1 returns to the existing LifeLab blue identity:

- Light primary container: `#89C2FF`
- Light background/mist: `#E6F7FF`
- Strong primary blue may remain `#0F5FA8` unless contrast checks require a nearby darker blue.
- Dark mode should feel blue-black, not green, purple, brown, or beige.

### Information Density

Settings and dashboards should feel like product UI rather than explanation cards.

- Profile theme mode, language mode, and default task filter use one-line rows where possible.
- A section title should not be repeated as a group title directly below it.
- Descriptions are removed unless they help the user make a decision.
- Buttons should be compact when they are secondary navigation actions.

### Search

Search result click behavior should be direct:

- Task result opens Tasks.
- Habit result opens Habits.
- Notification result opens Messages.
- Article and offer results open Discover.
- The v1.2.0 local search detail card and "open related page" second step are removed from the primary flow.

### Habit Reminder UX

Reminder setup should match user expectations:

- Enable reminder with a switch.
- Tap reminder time to open a time picker.
- Save the chosen time directly.
- Do not expose `+30 minutes` or "set default 09:00" controls.
- Priority changes should not cause immediate visible list reordering.
- Priority is visible through card treatment:
  - High: stronger left accent, clearer reminder icon/tone, optional alarm-mode control.
  - Normal: standard blue accent.
  - Low: quieter accent.

### High Priority And Alarm Mode

High priority is per habit.

- High priority without alarm mode uses a high-importance notification channel and should be visually prominent in the notification shade where Android/OEM settings allow it.
- Alarm mode is available only for high-priority habit reminders.
- Alarm mode is off by default.
- Alarm mode should use an alarm-grade scheduling path such as `AlarmManager.setAlarmClock` when enabled.
- The UI must not imply alarm mode can override Xiaomi/HyperOS restrictions if the user disabled required permissions/settings.

### Notification Self-Test

The Notifications screen should help diagnose real devices:

- "Send test notification now" posts an immediate local notification.
- "Test reminder in 1 minute" schedules a local notification through the same reminder infrastructure.
- The screen shows current Android notification delivery status.
- Xiaomi/HyperOS guidance is short in-app copy:
  - Allow notifications.
  - Allow autostart/background activity when available.
  - Set battery saver to no restrictions for LifeLab when reminders do not fire.

Longer OEM setup details belong in docs, not on the app screen.

## Engineering Constraints

- Keep the app in the existing single `:app` module.
- Do not add new backend, login, payment, or network behavior.
- Do not add broad Compose UI tests.
- Tests should be concise and behavior-focused; avoid tests centered on "not visible" or "not have" UI absence.
- Use the existing Kotlin, Compose, Hilt, Room, DataStore, and AlarmManager patterns.
- If the habit reminder schema changes, add a Room migration and focused repository tests.
- Version target is `LIFELAB_VERSION_CODE=5` and `LIFELAB_VERSION_NAME=1.2.1`.

## Acceptance Criteria

- v1.2.1 release notes match the release description above.
- The app palette is blue again in light and dark modes.
- Profile settings no longer duplicate section labels and stay compact.
- Home no longer repeats "today" in adjacent title/subtitle/body positions.
- Search result click opens the related destination directly.
- Habit reminder time is set through a time picker.
- High priority can optionally enable alarm mode per habit.
- Priority changes are visible on cards but do not immediately reorder the visible habit list.
- Notifications screen includes immediate and one-minute test actions.
- Xiaomi/HyperOS setup guidance is present but compact.
- `git diff --check` passes.
- `./gradlew :app:testDebugUnitTest` passes.
- `./gradlew :app:lintDebug` passes.
- Release commit and `v1.2.1` tag exist before APK packaging starts.
