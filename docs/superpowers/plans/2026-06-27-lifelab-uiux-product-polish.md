# LifeLab UI/UX Product Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Polish LifeLab into a clearer Chinese-first local personal planning app, simplify high-value tests, and prepare a reviewed release tag.

**Architecture:** Keep the existing single Android app module and local-first Room/DataStore architecture. Add a narrow shared Compose UI layer for repeated screen, state, action, and photo patterns, then refactor feature screens to consume those components without moving business logic into UI helpers.

**Tech Stack:** Kotlin, Jetpack Compose Material3, Navigation Compose, Hilt, Room, DataStore Preferences, Android Photo Picker, Coil Compose, JUnit/coroutines tests, GitHub Actions.

---

## File Structure

- Create `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabScaffoldComponents.kt` for headers, state cards, section titles, and message banners.
- Create `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabActionComponents.kt` for stable action rows and responsive button layout.
- Create `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabPhotoStrip.kt` for shared photo attachment and preview UI.
- Modify `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt` to formalize LifeLab blue/mist/navy tokens while keeping existing theme mode behavior.
- Modify `app/src/main/java/com/example/lifelab/app/LifeLabApp.kt` and `app/src/main/java/com/example/lifelab/app/navigation/LifeLabDestination.kt` only if navigation shell polish or selected indicator behavior needs small changes.
- Modify `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt` for the daily dashboard.
- Modify `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksRoute.kt` and `TasksUiState.kt` only where UI state needs non-English message or derived display improvements.
- Modify `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt` for one-tap check-in layout and shared photo strip.
- Modify `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt` for grouped settings and stable controls.
- Modify `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`, `feature/discover/presentation/DiscoverScreen.kt`, `feature/notifications/presentation/NotificationsRoute.kt`, and `feature/weblab/presentation/WebLabRoute.kt` for header/state/card consistency only.
- Modify `app/src/main/res/values/strings.xml` and `app/src/main/res/values-en/strings.xml` for new/changed copy.
- Modify or delete redundant tests under `app/src/test/java/com/example/lifelab/**`.
- Modify `docs/05-quality/testing-strategy.md` to reflect the trimmed core test policy.
- Modify `gradle.properties` near the end to bump `LIFELAB_VERSION_CODE` and `LIFELAB_VERSION_NAME` after UI work is verified.

## Execution Order And Ownership

Run tasks sequentially: Task 1, then Task 2, then Task 3. Do not dispatch these implementation tasks in parallel because multiple tasks intentionally touch shared resources and strings.

Ownership rules:

- Task 1 owns shared UI components, theme tokens, and app shell polish.
- Task 2 owns Home, Tasks, Habits, and the shared photo UI. It may add string resources needed by those screens after rebasing on Task 1.
- Task 3 owns Profile, secondary pages, test trimming, testing docs, and release version bump. It may edit string resources after rebasing on Task 2.
- No worker may revert or rewrite another worker's completed files unless explicitly asked by the controller.
- If a worker finds a needed change outside its ownership, it should report the need instead of silently widening scope.

## Task 1: Shared UI System And App Shell

**Files:**
- Create: `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabScaffoldComponents.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabActionComponents.kt`
- Modify: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/LifeLabApp.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Test: `app/src/test/java/com/example/lifelab/app/navigation/LifeLabDestinationTest.kt`

- [ ] **Step 1: Inspect existing screen patterns**

Read current Home, Tasks, Habits, Profile, Search, Discover, Notifications, WebLab screens. Note repeated header, empty/error/loading, and action row patterns.

- [ ] **Step 2: Add shared UI components**

Implement:

```kotlin
@Composable
fun LifeLabScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
)

@Composable
fun LifeLabStateCard(
    title: String,
    body: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
)

@Composable
fun LifeLabMessageBanner(
    message: String,
    onDismiss: (() -> Unit)? = null,
)

@Composable
fun LifeLabSectionTitle(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
)
```

Implement stable action layout in `LifeLabActionComponents.kt`:

```kotlin
@Composable
fun LifeLabPrimaryActionRow(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    primaryIcon: ImageVector? = null,
    secondaryIcon: ImageVector? = null,
)
```

Use `FlowRow` or `BoxWithConstraints` so actions wrap or stack instead of squeezing text.

- [ ] **Step 3: Formalize theme tokens**

Keep `#89C2FF` and `#E6F7FF` as explicit token constants. Adjust light/dark schemes to keep strong contrast:

```kotlin
private val LifeBlue = Color(0xFF89C2FF)
private val LifeMist = Color(0xFFE6F7FF)
private val LifeBlueStrong = Color(0xFF1F6FB8)
private val LifeInk = Color(0xFF0D1B2A)
private val LifeNavy = Color(0xFF061524)
private val LifeNavySurface = Color(0xFF0D2033)
```

Do not introduce purple, orange, beige, or large gradients.

- [ ] **Step 4: Polish bottom navigation**

Keep four destinations. Ensure each item uses stable labels and icons, and navigation bar colors come from the LifeLab theme.

- [ ] **Step 5: Verify**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*LifeLabDestinationTest*" --no-daemon
```

If local Android SDK is missing, record the exact SDK error and continue with static checks.

- [ ] **Step 6: Commit**

Commit message:

```bash
git commit -m "feat: add shared LifeLab UI shell components"
```

## Task 2: Home Dashboard, Tasks, And Habits UX

**Files:**
- Create: `app/src/main/java/com/example/lifelab/core/ui/components/LifeLabPhotoStrip.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksUiState.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Test: `app/src/test/java/com/example/lifelab/feature/home/presentation/HomeViewModelTest.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/tasks/presentation/TaskListViewModelTest.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/habits/presentation/HabitsViewModelTest.kt`

- [ ] **Step 1: Build shared photo strip**

Move duplicated task/habit photo UI into `LifeLabPhotoStrip`. Preserve Android Photo Picker, camera `FileProvider`, app-specific storage copy, and max-three policy.

`LifeLabPhotoStrip` is a UI reuse component only. It may call the existing picker/camera launchers and existing app-specific file-copy helpers, but it must not move repository, Room, storage policy, or photo persistence ownership into the shared UI layer.

Expose callbacks that keep feature ViewModels unchanged:

```kotlin
@Composable
fun LifeLabPhotoStrip(
    ownerId: String,
    owner: PhotoOwner,
    photos: List<PhotoRecord>,
    onAttachPhotos: (List<String>, PhotoSource) -> Unit,
    modifier: Modifier = Modifier,
)
```

- [ ] **Step 2: Rebuild Home as a daily dashboard**

Use shared components. Required sections:

- Header with Home title, today subtitle, search/messages/refresh icon actions.
- "今日任务" card using `HomeFeedItem.TaskSummary`.
- "习惯打卡" card using `HomeFeedItem.HabitInsight`.
- "快捷操作" row with clear actions for tasks, habits, and discover.
- Feed/recommendations remain below the primary daily cards.

Avoid nested cards and do not add a landing-page hero.

- [ ] **Step 3: Rework Tasks layout**

Required behavior:

- Header has title/subtitle plus one clear new-task action.
- Filter row does not share a cramped row with create button.
- Task cards have stable metadata layout and avoid chip overflow.
- Detail mode uses one primary action: complete or restore. Edit is secondary.
- Editor fields stay full-width, with save button fixed full-width at the end.
- Use shared photo strip in detail and editor.

Do not change repository or Room behavior unless needed to remove English message mapping.

- [ ] **Step 4: Rework Habits layout**

Required behavior:

- Stats are compact and scannable.
- Habit card prioritizes check-in and streak.
- Reminder controls are visually secondary and wrap on narrow width.
- Use shared photo strip.

- [ ] **Step 5: Remove English message mapping where touched**

Where task/habit UI currently compares English strings, prefer resource-backed UI message keys or at minimum make ViewModel emitted copy Chinese-first. Do not migrate the entire app message architecture if it would explode scope.

- [ ] **Step 6: Verify**

Run focused tests:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*HomeViewModelTest*" --tests "*TaskListViewModelTest*" --tests "*HabitsViewModelTest*" --no-daemon
```

If local Android SDK is missing, run static checks:

```powershell
rg -n '"Task created"|"Task updated"|"Task completed"|"Task restored"|"Checked in |already checked in today|Reminder updated for' app/src/main/java
```

Expected: no newly introduced English UI message mapping in touched presentation files.

- [ ] **Step 7: Commit**

Commit message:

```bash
git commit -m "feat: polish home tasks and habits UX"
```

## Task 3: Profile, Secondary Pages, Test Trim, And Release Prep

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/weblab/presentation/WebLabRoute.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `docs/05-quality/testing-strategy.md`
- Modify/Delete: redundant files under `app/src/test/java/com/example/lifelab/**`
- Modify: `gradle.properties`
- Test: `app/src/test/java/com/example/lifelab/core/datastore/DataStoreAppPreferencesRepositoryTest.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/profile/presentation/ProfileAppPreferencesTest.kt`
- Test: `app/src/test/java/com/example/lifelab/feature/search/presentation/SearchViewModelTest.kt`
- Test: `app/src/test/java/com/example/lifelab/app/AppIdentityGradleConfigurationTest.kt`

- [ ] **Step 1: Polish Profile settings**

Group settings as:

- 外观
- 语言
- 通知
- 任务偏好
- 实验室

Use stable controls and wrapping rows so Chinese and English labels fit.

- [ ] **Step 2: Normalize secondary page shell**

Apply shared headers/state cards to Search, Discover, Notifications, and Web Lab. Keep behavior unchanged:

- Search still records history and supports filters.
- Discover remains a secondary page.
- Notifications still supports read/archive/settings toggles.
- Web Lab still loads `https://hufaei.github.io/`, supports refresh, back history, error overlay, and external links.

- [ ] **Step 3: Trim tests**

Delete or merge low-value tests after confirming coverage remains:

- Prefer deleting `InMemory...RepositoryTest` files when equivalent Room repository and ViewModel tests cover the behavior.
- Keep current AppIdentity, Manifest, SeedDataLocalization, DataStore, Room, core media policy/storage, Task/Habit/Search ViewModel, and WebLab ViewModel coverage.
- Update `docs/05-quality/testing-strategy.md` to say tests protect core behavior and UX flows, not all historical implementation variants.

- [ ] **Step 4: Bump version for release**

In `gradle.properties`, increment:

```properties
LIFELAB_VERSION_CODE=2
LIFELAB_VERSION_NAME=1.1.0
```

Only do this after UI changes and test trim are in the branch.

- [ ] **Step 5: Verify**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:lintDebug --no-daemon
```

If local Android SDK is missing, run:

```powershell
git diff --check
rg -n "LIFELAB_VERSION_CODE=2|LIFELAB_VERSION_NAME=1.1.0" gradle.properties
rg -n "if: github.event_name == 'workflow_dispatch' \\|\\| startsWith\\(github.ref, 'refs/tags/v'\\)" .github/workflows/android-ci.yml
rg -n "pull_request:|workflow_dispatch:|tags:|assembleRelease|upload-artifact" .github/workflows/android-ci.yml
```

Expected workflow behavior: PR path runs unit tests and lint only; release signing, `assembleRelease`, and APK upload are guarded by `workflow_dispatch` or `refs/tags/v*`.

- [ ] **Step 6: Commit**

Commit message:

```bash
git commit -m "chore: trim tests and prepare ui polish release"
```

## Integration And Release

- [ ] Controller reviews final diff against `docs/superpowers/specs/2026-06-27-lifelab-uiux-product-polish-design.md`.
- [ ] Controller runs best available local verification.
- [ ] Controller completes UI/UX checklist:
  - Chinese locale has no obvious English leftovers in built-in screen labels, state text, task/habit success messages, or seed data.
  - `values-en` has matching resources for all new string keys.
  - Light theme uses `#89C2FF` and `#E6F7FF` as visible brand anchors without low-contrast text.
  - Dark theme stays in deep blue/blue-black color range and avoids harsh bright panels.
  - Bottom navigation remains four destinations and does not crowd labels.
  - Primary buttons and action rows wrap or stack instead of squeezing text on narrow screens.
  - Home reads as a daily dashboard, not a landing page or entry dump.
  - Tasks and Habits keep the primary user action visually dominant.
  - Search, Discover, Notifications, and Web Lab retain their existing behavior after shell polish.
- [ ] Push branch `codex/uiux-product-polish`.
- [ ] Open PR.
- [ ] Wait for GitHub Actions PR checks. Do not manually run release workflow on the PR.
- [ ] Merge PR to `main` after checks pass.
- [ ] Update local `main`.
- [ ] Create annotated tag `v1.1.0` from merged `main`.
- [ ] Push tag `v1.1.0`.
- [ ] Create GitHub Release `v1.1.0` with concise Chinese notes.
- [ ] Confirm GitHub tag/release workflow starts. If signing secrets are missing, document that release metadata exists but APK artifact cannot be produced until secrets are configured.
