# LifeLab v1.2.1 Experience Correction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship LifeLab v1.2.1 as an experience correction release that restores the blue brand, reduces UI clutter, makes search direct, and makes habit reminders testable on Xiaomi/Android devices.

**Architecture:** Keep the app in the existing single `:app` module. Use small, task-scoped changes in the existing feature packages. Add one Room migration only for the per-habit alarm-mode field.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, ViewModel + StateFlow, Room, Hilt, AlarmManager, JUnit.

## Global Constraints

- Main controller reviews and integrates; implementation code is written by worker agents.
- Light brand palette must restore `#89C2FF` and `#E6F7FF`.
- Dark mode must use a matching blue-black palette, not the v1.2.0 sage/green direction.
- Do not add backend, login, payment, or network behavior.
- Do not add broad Compose UI tests.
- Tests must be concise and behavior-focused; avoid tests centered on "not visible" or "not have" UI absence.
- Version target is `LIFELAB_VERSION_CODE=5` and `LIFELAB_VERSION_NAME=1.2.1`.
- Do not start APK packaging until a release commit and tag `v1.2.1` both exist.
- Workers must not rewrite unrelated files or revert changes outside their task scope.

---

## File Structure

- `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`: blue light/dark color scheme.
- `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt`: compact settings rows and reduced copy.
- `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt`: reduced "today" repetition and compact dashboard/action surfaces.
- `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchRoute.kt`: result clicks route directly.
- `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`: remove primary local-detail click flow.
- `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchViewModel.kt`: remove or de-emphasize selected detail state if no longer needed by direct flow.
- `app/src/main/java/com/example/lifelab/feature/habits/domain/model/HabitReminder.kt`: add per-habit alarm-mode state.
- `app/src/main/java/com/example/lifelab/feature/habits/data/HabitEntity.kt`: persist alarm-mode state.
- `app/src/main/java/com/example/lifelab/core/database/LifeLabDatabase.kt`: add migration `3 -> 4`.
- `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsViewModel.kt`: update reminder editing, stable list ordering, and alarm-mode events.
- `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`: time picker, priority visual treatment, alarm-mode UI.
- `app/src/main/java/com/example/lifelab/core/notifications/HabitReminderNotificationScheduler.kt`: high-priority channel behavior, alarm-clock scheduling when enabled, test scheduling support.
- `app/src/main/java/com/example/lifelab/core/notifications/HabitReminderReceiver.kt`: handle test reminder extras if needed.
- `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`: notification self-test UI.
- `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModel.kt`: notification self-test events and status messages.
- `app/src/main/res/values/strings.xml` and `app/src/main/res/values-en/strings.xml`: concise copy and new reminder/self-test strings.
- Focused tests under `app/src/test/java/com/example/lifelab/...`.

---

### Task 1: Restore Blue Brand Theme

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`

**Interfaces:**
- Consumes: existing `LifeLabTheme(themeMode, content)` API.
- Produces: restored blue `MaterialTheme.colorScheme` used by all screens.

- [ ] **Step 1: Restore light palette**

Set the named light tokens to the v1.1.1 direction:

```kotlin
val LifeBlue = Color(0xFF89C2FF)
val LifeMist = Color(0xFFE6F7FF)
val LifeBlueStrong = Color(0xFF0F5FA8)
val LifeInk = Color(0xFF0D1B2A)
```

Use them in `lightColorScheme`:

```kotlin
primary = LifeBlueStrong
primaryContainer = LifeBlue
background = Color(0xFFF3FAFF)
surfaceVariant = Color(0xFFDDF2FF)
```

- [ ] **Step 2: Replace dark sage palette with blue-black palette**

Use blue-black values close to:

```kotlin
val LifeNavy = Color(0xFF061524)
val LifeNavySurface = Color(0xFF0D2033)
```

Dark primary should remain blue and legible:

```kotlin
primary = LifeBlue
primaryContainer = LifeBlueStrong
background = LifeNavy
surface = LifeNavySurface
```

- [ ] **Step 3: Verify compile**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: build succeeds.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt
git commit -m "style: restore lifelab blue theme"
```

---

### Task 2: Compact Profile And Home Copy

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

**Interfaces:**
- Consumes: existing `ProfileScreen`, `HomeScreen`, `ProfileUiEvent`, and string resource APIs.
- Produces: denser settings/home screens without repeated labels or demo-like descriptions.

- [ ] **Step 1: Replace Profile duplicated group headings**

In `ProfileRoute.kt`, remove nested `SettingGroup(title = profile_theme_mode)` when the containing section already communicates appearance. Use a compact row helper that places the label and choices on one row when width allows:

```kotlin
@Composable
private fun SettingChoiceRow(
    title: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            content()
        }
    }
}
```

Use it for theme mode, language mode, and default task filter. Keep chip text single-line where possible.

- [ ] **Step 2: Remove redundant setting descriptions**

Remove or shorten descriptions that repeat the title:

```xml
<string name="profile_notifications_description">消息中心内容</string>
<string name="profile_notification_settings_description">习惯卡片里设置手机提醒</string>
```

If the row title is enough, use `SettingInfoRow` only where it adds real value.

- [ ] **Step 3: Reduce Home today repetition**

Change Home header so adjacent text does not say "今日面板" and "今天，日期" together. Preferred behavior:

```kotlin
LifeLabScreenHeader(
    title = stringResource(R.string.home_title),
    actions = { ... },
)
```

Make `home_title` concise:

```xml
<string name="home_title">今天</string>
```

Remove `home_today_subtitle` from the header flow unless it is moved to a non-redundant date chip.

- [ ] **Step 4: Keep home actions compact**

For quick action buttons, cap labels to one line:

```kotlin
Text(
    text = label,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)
```

- [ ] **Step 5: Run focused verification**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: build succeeds.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "style: tighten profile and home surfaces"
```

---

### Task 3: Make Search Results Open Directly

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchUiState.kt`
- Modify: `app/src/test/java/com/example/lifelab/feature/search/presentation/SearchViewModelTest.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

**Interfaces:**
- Consumes: `SearchResultType` and `LifeLabRoutes.relatedRouteFor(type)`.
- Produces: result click callback that opens the related destination without a second detail action.

- [ ] **Step 1: Change SearchScreen result click signature**

Change:

```kotlin
onResultClick: (String) -> Unit
```

to:

```kotlin
onResultClick: (SearchResultItem) -> Unit
```

In `SearchResultRow`, call:

```kotlin
.clickable { onClick(item) }
```

- [ ] **Step 2: Route clicks directly**

In `SearchRoute`, wire:

```kotlin
onResultClick = { item ->
    onOpenResultDestination(item.type)
}
```

Do not call `viewModel.selectResult` for primary result clicks.

- [ ] **Step 3: Remove the detail card from the primary flow**

Remove `SearchResultDetailCard` from the displayed search flow. If the ViewModel selected-detail state becomes unused, remove `selectedResultDetail`, `SearchResultDetail`, `selectResult`, and `clearSelectedResult`.

Remove unused string:

```xml
search_detail_open_related
```

only if no file references it after the change.

- [ ] **Step 4: Update tests**

Replace the selected-detail test with a focused state test that still belongs to ViewModel behavior, such as query/filter/history. Do not add UI absence tests.

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.search.*'
```

Expected: search tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/lifelab/feature/search/presentation app/src/test/java/com/example/lifelab/feature/search/presentation/SearchViewModelTest.kt app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "feat: open search results directly"
```

---

### Task 4: Redesign Habit Reminder Time, Priority, And Alarm Mode

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/domain/model/HabitReminder.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/data/HabitEntity.kt`
- Modify: `app/src/main/java/com/example/lifelab/core/database/LifeLabDatabase.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsUiState.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `app/src/test/java/com/example/lifelab/feature/habits/data/RoomHabitRepositoryTest.kt`
- Modify: `app/src/test/java/com/example/lifelab/feature/habits/presentation/HabitsViewModelTest.kt`

**Interfaces:**
- Consumes: existing habit repository and reminder scheduler.
- Produces: persisted per-habit alarm-mode state and a normal time-picker reminder UI.

- [ ] **Step 1: Add per-habit alarm mode**

Change `HabitReminder`:

```kotlin
data class HabitReminder(
    val enabled: Boolean,
    val time: LocalTime?,
    val priority: HabitReminderPriority = HabitReminderPriority.Normal,
    val alarmClockEnabled: Boolean = false,
)
```

Alarm mode is meaningful only for `HabitReminderPriority.High`. When priority changes away from High, ViewModel should save `alarmClockEnabled = false`.

- [ ] **Step 2: Persist alarm mode**

Add to `HabitEntity`:

```kotlin
@ColumnInfo(name = "reminder_alarm_clock_enabled") val reminderAlarmClockEnabled: Boolean,
```

Map it to and from `HabitReminder.alarmClockEnabled`.

- [ ] **Step 3: Add Room migration**

Update database version from `3` to `4`.

Add:

```kotlin
val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE habits ADD COLUMN reminder_alarm_clock_enabled INTEGER NOT NULL DEFAULT 0",
        )
    }
}
```

Ensure the database builder registers the new migration wherever migrations are supplied.

- [ ] **Step 4: Replace reminder increment with explicit time update**

Replace `increaseEditorReminderTime()` with:

```kotlin
fun updateEditorReminderTime(time: LocalTime)
```

For habit cards, use the existing `updateReminderTime(habitId, time)` path.

- [ ] **Step 5: Prevent immediate priority resorting**

Change `sortedForDisplay()` so it does not sort by `habit.reminder.priority.sortWeight`. Keep stable ordering by enabled state, reminder time, and name:

```kotlin
compareBy<Habit> { habit -> if (habit.reminder.enabled) 0 else 1 }
    .thenBy { habit -> habit.reminder.time?.toSecondOfDay() ?: Int.MAX_VALUE }
    .thenBy { habit -> habit.name }
```

- [ ] **Step 6: Add time picker UI**

In `HabitsScreen.kt`, replace the `+30 minutes`/`09:00` button with a time-picker dialog. Use Material 3 time picker APIs already available in the project dependency:

```kotlin
TimePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.common_save)) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
) {
    TimePicker(state = timePickerState)
}
```

If `TimePickerDialog` is not available in the Material 3 version, use `AlertDialog` with `TimePicker` content.

- [ ] **Step 7: Add priority visual treatment**

Do not rely only on priority text chips. Add a visual treatment in `HabitCard`, such as a left accent strip or tinted reminder row. Use blue-family colors from `MaterialTheme.colorScheme`.

Show the high-priority alarm-mode switch only when priority is High:

```kotlin
if (priority == HabitReminderPriority.High) {
    SettingSwitchRow-like control for stringResource(R.string.habits_alarm_clock_enabled)
}
```

- [ ] **Step 8: Add focused tests**

Update tests to cover:

- Saving a high-priority reminder with alarm mode persists `alarmClockEnabled = true`.
- Changing priority away from High clears alarm mode.
- `sortedForDisplay` no longer reorders by priority.
- Room repository round-trips alarm mode.

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.habits.*'
```

Expected: habit tests pass.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/example/lifelab/feature/habits app/src/main/java/com/example/lifelab/core/database/LifeLabDatabase.kt app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml app/src/test/java/com/example/lifelab/feature/habits
git commit -m "feat: improve habit reminder controls"
```

---

### Task 5: Add Notification Self-Test And Release Prep

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/core/notifications/HabitReminderNotificationScheduler.kt`
- Modify: `app/src/main/java/com/example/lifelab/core/notifications/HabitReminderReceiver.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsUiState.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModel.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `app/src/test/java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModelTest.kt`
- Modify: `gradle.properties`
- Modify: `app/src/test/java/com/example/lifelab/app/AppIdentityGradleConfigurationTest.kt`
- Modify: `README.md`
- Modify: `docs/07-coordination/integration-log.md`

**Interfaces:**
- Consumes: scheduler, receiver, Android notification permission status, and existing Notifications UI.
- Produces: notification self-test actions and v1.2.1 release version metadata.

- [ ] **Step 1: Add scheduler functions for tests and alarm mode**

Add scheduler APIs:

```kotlin
fun showTestNotification()
fun scheduleTestReminderOneMinuteFromNow()
```

Use existing channel setup and notification posting logic. Test reminder should go through `HabitReminderReceiver` so it exercises scheduled delivery.

When scheduling a habit reminder with `alarmClockEnabled = true`, use `AlarmManager.setAlarmClock` with a suitable show intent. Otherwise keep `setAndAllowWhileIdle`.

- [ ] **Step 2: Add Notifications events and UI state**

Add events:

```kotlin
data object SendImmediateTestNotification : NotificationsUiEvent
data object ScheduleOneMinuteTestReminder : NotificationsUiEvent
```

Add a transient message field if needed:

```kotlin
val systemTestMessage: String? = null
```

- [ ] **Step 3: Add self-test UI**

In `NotificationsRoute.kt`, add a compact self-test card:

- Button: `发送测试通知`
- Button: `1 分钟后测试提醒`
- Short Xiaomi/HyperOS guidance text.

Do not add long OEM setup prose to the screen.

- [ ] **Step 4: Add focused tests**

Use a fake scheduler or small interface if needed so ViewModel tests can assert:

- Immediate test event calls the immediate test scheduler path.
- One-minute test event calls scheduled test path.
- User-facing status message updates after each event.

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.example.lifelab.feature.notifications.*'
```

Expected: notification tests pass.

- [ ] **Step 5: Bump version to v1.2.1**

Set:

```properties
LIFELAB_VERSION_CODE=5
LIFELAB_VERSION_NAME=1.2.1
```

Update `AppIdentityGradleConfigurationTest` to assert those values.

- [ ] **Step 6: Update release docs**

Add a concise v1.2.1 note to README or integration log:

```markdown
v1.2.1 restores the blue brand palette, tightens dense screens, opens search results directly, and adds reminder self-test tools for Android/Xiaomi notification debugging.
```

- [ ] **Step 7: Run final verification**

Run:

```bash
git diff --check
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Expected: all pass.

- [ ] **Step 8: Commit release changes**

```bash
git add app/src/main/java/com/example/lifelab/core/notifications app/src/main/java/com/example/lifelab/feature/notifications app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml app/src/test/java/com/example/lifelab/feature/notifications app/src/test/java/com/example/lifelab/app/AppIdentityGradleConfigurationTest.kt gradle.properties README.md docs/07-coordination/integration-log.md
git commit -m "chore: release lifelab v1.2.1"
```

---

## Controller Review And Release Gate

The controller must review every worker commit before continuing.

After all worker tasks:

```bash
git diff --check
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Confirm `HEAD` is the release commit and then tag it:

```bash
git tag v1.2.1
```

Only after both release commit and `v1.2.1` tag exist may APK packaging start.
