# LifeLab UI/UX Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the LifeLab Android demo into the approved blue-white workbench design, using the updated brand colors `#89C2FF` and `#E6F7FF`, while preserving the existing domain and ViewModel boundaries.

**Architecture:** Keep the current `app/core/feature` layering and existing feature ViewModels/use cases intact. Introduce a design-system layer under `core/ui`, replace the 7-tab shell with a 4-tab shell, add a new `workbench` presentation surface that composes Tasks and Habits, then restyle each feature screen against the shared component set.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Android vector drawables, JUnit 4, AndroidX Compose UI test

---

## File Map

### App Shell And Navigation

- Modify: `app/src/main/java/com/example/lifelab/app/LifeLabApp.kt`
  Purpose: Replace the default `Scaffold` + 7-tab navigation with the new branded shell.
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabDestination.kt`
  Purpose: Change the top-level route model to `Today`, `Workbench`, `Discover`, `Me`.
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt`
  Purpose: Wire the new top-level routes and secondary routes.
- Create: `app/src/main/java/com/example/lifelab/app/workbench/WorkbenchRoute.kt`
  Purpose: Provide the app-level route that hosts task and habit content without making features depend on each other.
- Create: `app/src/main/java/com/example/lifelab/app/workbench/WorkbenchScreen.kt`
  Purpose: Render the segmented workbench UI and summary strip.

### Design System

- Create: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabColorTokens.kt`
  Purpose: Centralize the blue-white palette and supporting neutrals.
- Create: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTypography.kt`
  Purpose: Define explicit display/body/utility type roles with deterministic fallback families.
- Create: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabShapes.kt`
  Purpose: Define card, input, chip, and scaffold corner radii.
- Modify: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`
  Purpose: Apply the new color scheme, typography, and shapes.
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/LifeLabTopBar.kt`
  Purpose: Shared top bar with page title, subtitle, and global actions.
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/LifeLabBottomBar.kt`
  Purpose: Shared branded bottom bar with icon + label items.
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/PulseCard.kt`
  Purpose: Shared “Daily Pulse” summary strip.
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/SectionHeader.kt`
  Purpose: Shared section title + action row.
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/SummaryMetric.kt`
  Purpose: Shared metric tile for counts/streaks/progress.
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/StatePanel.kt`
  Purpose: Shared loading, empty, and error panels.
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/ActionCard.kt`
  Purpose: Shared elevated action card for “focus” and “recommended next step” surfaces.

### Resources

- Create: `app/src/main/res/drawable/ic_today.xml`
- Create: `app/src/main/res/drawable/ic_workbench.xml`
- Create: `app/src/main/res/drawable/ic_discover.xml`
- Create: `app/src/main/res/drawable/ic_me.xml`
- Create: `app/src/main/res/drawable/ic_search.xml`
- Create: `app/src/main/res/drawable/ic_notifications.xml`
  Purpose: Provide stable vector icons without introducing a new icon dependency.

### Feature Screens

- Modify: `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt`
  Purpose: Convert Home into the `Today` hub.
- Modify: `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksRoute.kt`
  Purpose: Expose reusable task content for Workbench instead of owning a top-level shell.
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
  Purpose: Expose reusable habit content for Workbench and align styling.
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverScreen.kt`
  Purpose: Restyle Discover into an editorial content surface.
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`
  Purpose: Promote Search into a secondary global route with stronger focus and grouping.
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
  Purpose: Split notifications into inbox and settings sections.
- Modify: `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt`
  Purpose: Reframe Profile as `Me`.

### Tests

- Modify: `app/src/test/java/com/example/lifelab/app/navigation/LifeLabDestinationTest.kt`
  Purpose: Assert the new route order and labels.
- Modify: `app/src/androidTest/java/com/example/lifelab/app/LifeLabAppSmokeTest.kt`
  Purpose: Assert the new shell is visible.
- Create: `app/src/test/java/com/example/lifelab/core/ui/theme/LifeLabThemeTokensTest.kt`
  Purpose: Lock the approved brand colors.
- Create: `app/src/test/java/com/example/lifelab/app/workbench/WorkbenchSummaryTest.kt`
  Purpose: Verify the summary strip counts derived from task/habit states.

## Parallelization Notes

These tasks must be executed serially because they touch the same shell and token files:

1. Task 1: Navigation contract
2. Task 2: Theme tokens and shared components
3. Task 3: App shell integration

After Task 3 is merged, these domains can be implemented in parallel:

- Task 4: Today hub
- Task 5: Workbench
- Task 6: Discover + Search
- Task 7: Notifications + Me

## Task 1: Lock The New Navigation Contract

**Files:**
- Create: `app/src/main/java/com/example/lifelab/app/workbench/WorkbenchRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabDestination.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt`
- Modify: `app/src/test/java/com/example/lifelab/app/navigation/LifeLabDestinationTest.kt`

- [ ] **Step 1: Write the failing route-order test**

```kotlin
class LifeLabDestinationTest {

    @Test
    fun topLevelDestinationsUseExpectedOrder() {
        assertEquals(
            listOf("today", "workbench", "discover", "me"),
            topLevelDestinations.map { it.route },
        )
        assertEquals(
            listOf("Today", "Workbench", "Discover", "Me"),
            topLevelDestinations.map { it.title },
        )
    }
}
```

- [ ] **Step 2: Run the targeted unit test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.app.navigation.LifeLabDestinationTest"`
Expected: FAIL because the project still exposes `home/tasks/habits/discover/search/notifications/profile`.

- [ ] **Step 3: Implement the new top-level route contract and add a placeholder Workbench route**

```kotlin
object LifeLabRoutes {
    const val TODAY = "today"
    const val WORKBENCH = "workbench"
    const val DISCOVER = "discover"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val ME = "me"
}

val topLevelDestinations = listOf(
    LifeLabDestination(route = LifeLabRoutes.TODAY, title = "Today"),
    LifeLabDestination(route = LifeLabRoutes.WORKBENCH, title = "Workbench"),
    LifeLabDestination(route = LifeLabRoutes.DISCOVER, title = "Discover"),
    LifeLabDestination(route = LifeLabRoutes.ME, title = "Me"),
)
```

```kotlin
@Composable
fun WorkbenchRoute(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text("Workbench")
    }
}
```

```kotlin
NavHost(
    navController = navController,
    startDestination = LifeLabRoutes.TODAY,
) {
    composable(LifeLabRoutes.TODAY) { HomeRoute(contentPadding = contentPadding) }
    composable(LifeLabRoutes.WORKBENCH) { WorkbenchRoute(contentPadding = contentPadding) }
    composable(LifeLabRoutes.DISCOVER) { DiscoverRoute(contentPadding = contentPadding) }
    composable(LifeLabRoutes.SEARCH) { SearchRoute(contentPadding = contentPadding) }
    composable(LifeLabRoutes.NOTIFICATIONS) { NotificationsRoute(contentPadding = contentPadding) }
    composable(LifeLabRoutes.ME) { ProfileRoute(contentPadding = contentPadding) }
}
```

- [ ] **Step 4: Re-run the targeted unit test**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.app.navigation.LifeLabDestinationTest"`
Expected: PASS.

- [ ] **Step 5: Commit the route-contract baseline**

```bash
git add app/src/main/java/com/example/lifelab/app/navigation/LifeLabDestination.kt app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt app/src/main/java/com/example/lifelab/app/workbench/WorkbenchRoute.kt app/src/test/java/com/example/lifelab/app/navigation/LifeLabDestinationTest.kt
git commit -m "feat: establish lifelab navigation contract"
```

## Task 2: Build The Design Tokens And Shared UI Primitives

**Files:**
- Create: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabColorTokens.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTypography.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabShapes.kt`
- Modify: `app/src/main/java/com/example/lifelab/core/ui/theme/LifeLabTheme.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/LifeLabTopBar.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/LifeLabBottomBar.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/PulseCard.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/SectionHeader.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/SummaryMetric.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/StatePanel.kt`
- Create: `app/src/main/java/com/example/lifelab/core/ui/component/ActionCard.kt`
- Create: `app/src/main/res/drawable/ic_today.xml`
- Create: `app/src/main/res/drawable/ic_workbench.xml`
- Create: `app/src/main/res/drawable/ic_discover.xml`
- Create: `app/src/main/res/drawable/ic_me.xml`
- Create: `app/src/main/res/drawable/ic_search.xml`
- Create: `app/src/main/res/drawable/ic_notifications.xml`
- Create: `app/src/test/java/com/example/lifelab/core/ui/theme/LifeLabThemeTokensTest.kt`

- [ ] **Step 1: Write the failing brand-color test**

```kotlin
class LifeLabThemeTokensTest {

    @Test
    fun usesApprovedBlueWhitePalette() {
        assertEquals(Color(0xFF89C2FF), LifeLabColorTokens.SkyPrimary)
        assertEquals(Color(0xFFE6F7FF), LifeLabColorTokens.SnowBackground)
    }
}
```

- [ ] **Step 2: Run the targeted unit test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.core.ui.theme.LifeLabThemeTokensTest"`
Expected: FAIL because `LifeLabColorTokens` does not exist yet.

- [ ] **Step 3: Implement the token files and shared primitives**

```kotlin
object LifeLabColorTokens {
    val SkyPrimary = Color(0xFF89C2FF)
    val SkyDeep = Color(0xFF5AAEEB)
    val SnowBackground = Color(0xFFE6F7FF)
    val CloudPanel = Color(0xFFF4FBFF)
    val Ink = Color(0xFF18324A)
    val FogLine = Color(0xFFCFE4F2)
}

private val LightColors = lightColorScheme(
    primary = LifeLabColorTokens.SkyPrimary,
    secondary = LifeLabColorTokens.SkyDeep,
    background = LifeLabColorTokens.SnowBackground,
    surface = Color.White,
    surfaceVariant = LifeLabColorTokens.CloudPanel,
    outlineVariant = LifeLabColorTokens.FogLine,
    onSurface = LifeLabColorTokens.Ink,
)
```

```kotlin
val LifeLabTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontFamily = FontFamily.SansSerif,
    ),
    labelMedium = Typography().labelMedium.copy(
        fontFamily = FontFamily.Monospace,
    ),
)
```

```kotlin
@Composable
fun StatePanel(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}
```

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#18324A"
        android:pathData="M4,12 L12,4 L20,12 L20,20 L4,20 Z"/>
</vector>
```

- [ ] **Step 4: Re-run the targeted unit test**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.core.ui.theme.LifeLabThemeTokensTest"`
Expected: PASS.

- [ ] **Step 5: Commit the token baseline**

```bash
git add app/src/main/java/com/example/lifelab/core/ui/theme app/src/main/java/com/example/lifelab/core/ui/component app/src/main/res/drawable app/src/test/java/com/example/lifelab/core/ui/theme/LifeLabThemeTokensTest.kt
git commit -m "feat: add lifelab design tokens and primitives"
```

## Task 3: Replace The Default App Shell

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/app/LifeLabApp.kt`
- Modify: `app/src/androidTest/java/com/example/lifelab/app/LifeLabAppSmokeTest.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/workbench/WorkbenchRoute.kt`

- [ ] **Step 1: Write the failing shell smoke test**

```kotlin
@Test
fun appShellShowsNewNavigation() {
    composeRule.onNodeWithText("Today").assertIsDisplayed()
    composeRule.onNodeWithText("Workbench").assertIsDisplayed()
    composeRule.onNodeWithText("Discover").assertIsDisplayed()
    composeRule.onNodeWithText("Me").assertIsDisplayed()
}
```

- [ ] **Step 2: Run the instrumentation smoke test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.lifelab.app.LifeLabAppSmokeTest`
Expected: FAIL because the shell still renders the default Material 3 navigation bar with no branded top bar.

- [ ] **Step 3: Implement the branded shell**

```kotlin
@Composable
fun LifeLabApp() {
    LifeLabTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: LifeLabRoutes.TODAY

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                LifeLabTopBar(
                    title = topLevelDestinations.firstOrNull { it.route == currentRoute }?.title ?: "LifeLab",
                    subtitle = currentSubtitleFor(currentRoute),
                    showSearch = currentRoute != LifeLabRoutes.SEARCH,
                    showNotifications = currentRoute != LifeLabRoutes.NOTIFICATIONS,
                    onSearchClick = { navController.navigate(LifeLabRoutes.SEARCH) },
                    onNotificationsClick = { navController.navigate(LifeLabRoutes.NOTIFICATIONS) },
                )
            },
            bottomBar = {
                LifeLabBottomBar(
                    currentRoute = currentRoute,
                    destinations = topLevelDestinations,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                        }
                    },
                )
            },
        ) { innerPadding ->
            LifeLabNavHost(navController = navController, contentPadding = innerPadding)
        }
    }
}
```

- [ ] **Step 4: Re-run the instrumentation smoke test**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.lifelab.app.LifeLabAppSmokeTest`
Expected: PASS if an emulator/device is connected. If no device is available, run `.\gradlew.bat testDebugUnitTest` and record the instrumentation step as environment-blocked.

- [ ] **Step 5: Commit the shell integration**

```bash
git add app/src/main/java/com/example/lifelab/app/LifeLabApp.kt app/src/main/java/com/example/lifelab/app/navigation/LifeLabNavHost.kt app/src/main/java/com/example/lifelab/app/workbench/WorkbenchRoute.kt app/src/androidTest/java/com/example/lifelab/app/LifeLabAppSmokeTest.kt
git commit -m "feat: introduce branded lifelab app shell"
```

## Task 4: Rebuild Home As The Today Hub

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt`

- [ ] **Step 1: Write a failing smoke assertion for the Today hub**

```kotlin
@Test
fun appShellShowsTodayFocusSection() {
    composeRule.onNodeWithText("Today focus").assertIsDisplayed()
}
```

- [ ] **Step 2: Run the smoke test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.lifelab.app.LifeLabAppSmokeTest`
Expected: FAIL because Home still renders generic “Recommended” and “Feed” sections.

- [ ] **Step 3: Rewrite HomeScreen into the Today structure**

```kotlin
Column(
    modifier = modifier
        .fillMaxSize()
        .padding(contentPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    PulseCard(
        headline = "Today focus",
        items = listOf(
            "Open tasks ${content.taskSummary.openTaskCount}",
            "Checked in ${content.habitInsight.checkedInTodayCount}",
            "Unread ${content.notificationCount}",
        ),
    )
    SectionHeader(title = "Today focus", actionLabel = "Refresh", onAction = onRefresh)
    ActionCard(title = primaryTaskTitle, body = primaryTaskBody, actionLabel = "Open workbench")
    SectionHeader(title = "For you")
    content.recommendedEntries.forEach { entry -> RecommendedEntryCard(entry) }
}
```

- [ ] **Step 4: Re-run the smoke and existing Home tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.feature.home.presentation.HomeViewModelTest"`
Expected: PASS.

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.lifelab.app.LifeLabAppSmokeTest`
Expected: PASS if a device is connected.

- [ ] **Step 5: Commit the Today hub**

```bash
git add app/src/main/java/com/example/lifelab/feature/home/presentation/HomeRoute.kt app/src/main/java/com/example/lifelab/feature/home/presentation/HomeScreen.kt app/src/androidTest/java/com/example/lifelab/app/LifeLabAppSmokeTest.kt
git commit -m "feat: redesign home as today hub"
```

## Task 5: Introduce The Workbench Surface

**Files:**
- Create: `app/src/main/java/com/example/lifelab/app/workbench/WorkbenchScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/app/workbench/WorkbenchRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt`
- Create: `app/src/test/java/com/example/lifelab/app/workbench/WorkbenchSummaryTest.kt`

- [ ] **Step 1: Write the failing workbench summary test**

```kotlin
class WorkbenchSummaryTest {

    @Test
    fun summaryUsesTaskAndHabitState() {
        val summary = buildWorkbenchSummary(
            tasks = listOf(activeTask, completedTask),
            habits = listOf(activeHabit),
            checkedInToday = 1,
        )

        assertEquals("1 active", summary.taskLabel)
        assertEquals("1 checked in", summary.habitLabel)
    }
}
```

- [ ] **Step 2: Run the targeted unit test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.app.workbench.WorkbenchSummaryTest"`
Expected: FAIL because the summary builder does not exist yet.

- [ ] **Step 3: Implement Workbench and extract reusable task/habit content**

```kotlin
enum class WorkbenchTab { Tasks, Habits }

@Composable
fun WorkbenchScreen(
    selectedTab: WorkbenchTab,
    onSelectTab: (WorkbenchTab) -> Unit,
    taskSummary: WorkbenchSummary,
    taskContent: @Composable () -> Unit,
    habitContent: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(title = "Workbench", subtitle = "Plan work and keep streaks alive")
        WorkbenchSummaryRow(summary = taskSummary)
        SingleChoiceSegmentedButtonRow {
            WorkbenchTab.entries.forEach { tab ->
                SegmentedButton(
                    selected = selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                ) { Text(tab.name) }
            }
        }
        if (selectedTab == WorkbenchTab.Tasks) taskContent() else habitContent()
    }
}
```

```kotlin
@Composable
fun TasksPane(...) { /* existing list/detail/editor content moved out of the top-level route */ }

@Composable
fun HabitsPane(...) { /* existing stats + habit cards moved out of the old top-level route */ }
```

- [ ] **Step 4: Re-run targeted workbench and feature unit tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.app.workbench.WorkbenchSummaryTest" --tests "com.example.lifelab.feature.tasks.presentation.TaskListViewModelTest" --tests "com.example.lifelab.feature.habits.presentation.HabitsViewModelTest"`
Expected: PASS.

- [ ] **Step 5: Commit the Workbench integration**

```bash
git add app/src/main/java/com/example/lifelab/app/workbench app/src/main/java/com/example/lifelab/feature/tasks/presentation/TasksRoute.kt app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsRoute.kt app/src/main/java/com/example/lifelab/feature/habits/presentation/HabitsScreen.kt app/src/test/java/com/example/lifelab/app/workbench/WorkbenchSummaryTest.kt
git commit -m "feat: add workbench surface"
```

## Task 6: Restyle Discover And Search

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverScreen.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt`

- [ ] **Step 1: Write a failing shell smoke assertion for search/discover language**

```kotlin
@Test
fun appShellShowsDiscoverAndSearchEntryPoints() {
    composeRule.onNodeWithText("Discover").assertIsDisplayed()
    composeRule.onNodeWithText("Hot searches").assertExists()
}
```

- [ ] **Step 2: Run the smoke test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.lifelab.app.LifeLabAppSmokeTest`
Expected: FAIL because Search has not yet been promoted into the global search surface.

- [ ] **Step 3: Rebuild the page structure using shared components**

```kotlin
SectionHeader(
    title = "Discover",
    subtitle = "Editorial picks for your next experiment",
)
```

```kotlin
KeywordSection(
    title = "Hot searches",
    keywords = hotKeywords,
    onKeywordClick = onHotKeywordClick,
)
```

```kotlin
StatePanel(
    title = "No results",
    body = "Try another keyword or filter.",
    actionLabel = "Clear filters",
    onAction = onClearFilters,
)
```

- [ ] **Step 4: Re-run existing Discover/Search unit tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.feature.discover.presentation.DiscoverViewModelTest" --tests "com.example.lifelab.feature.search.presentation.SearchViewModelTest" --tests "com.example.lifelab.feature.search.domain.SearchHistoryPolicyTest"`
Expected: PASS.

- [ ] **Step 5: Commit the Discover/Search redesign**

```bash
git add app/src/main/java/com/example/lifelab/feature/discover/presentation/DiscoverScreen.kt app/src/main/java/com/example/lifelab/feature/search/presentation/SearchRoute.kt app/src/main/java/com/example/lifelab/feature/search/presentation/SearchScreen.kt app/src/androidTest/java/com/example/lifelab/app/LifeLabAppSmokeTest.kt
git commit -m "feat: restyle discover and search"
```

## Task 7: Reframe Notifications And Profile As Secondary Control Surfaces

**Files:**
- Modify: `app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt`
- Modify: `app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt`

- [ ] **Step 1: Write a failing smoke assertion for the Me page**

```kotlin
@Test
fun appShellShowsMeNavigation() {
    composeRule.onNodeWithText("Me").assertIsDisplayed()
    composeRule.onNodeWithText("Preferences").assertExists()
}
```

- [ ] **Step 2: Run the smoke test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.lifelab.app.LifeLabAppSmokeTest`
Expected: FAIL because the page still presents itself as “Profile”.

- [ ] **Step 3: Restructure Notifications and Profile**

```kotlin
SectionHeader(
    title = "Notifications",
    subtitle = "Inbox and delivery settings",
)

SingleChoiceSegmentedButtonRow {
    SegmentedButton(selected = selectedTab == Inbox, onClick = { onSelectTab(Inbox) }) { Text("Inbox") }
    SegmentedButton(selected = selectedTab == Settings, onClick = { onSelectTab(Settings) }) { Text("Settings") }
}
```

```kotlin
SectionHeader(
    title = "Me",
    subtitle = "Your account, defaults, and preferences",
)
```

- [ ] **Step 4: Re-run existing Notifications/Profile unit tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.lifelab.feature.notifications.presentation.NotificationsViewModelTest" --tests "com.example.lifelab.feature.profile.presentation.ProfileViewModelTest" --tests "com.example.lifelab.feature.profile.domain.ProfileOverviewTest"`
Expected: PASS.

- [ ] **Step 5: Commit the Notifications/Me redesign**

```bash
git add app/src/main/java/com/example/lifelab/feature/notifications/presentation/NotificationsRoute.kt app/src/main/java/com/example/lifelab/feature/profile/presentation/ProfileRoute.kt app/src/androidTest/java/com/example/lifelab/app/LifeLabAppSmokeTest.kt
git commit -m "feat: redesign notifications and me pages"
```

## Task 8: Verify The Integrated Redesign

**Files:**
- Modify: `docs/superpowers/specs/2026-06-24-lifelab-uiux-redesign-design.md`
  Purpose: Update the spec with the finalized blue-white palette values if it still lists the earlier colors.

- [ ] **Step 1: Update the spec color values if needed**

```markdown
- `SkyPrimary` `#89C2FF`
- `SnowBackground` `#E6F7FF`
```

- [ ] **Step 2: Run the full unit-test suite**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: PASS.

- [ ] **Step 3: Run the instrumentation smoke suite**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.lifelab.app.LifeLabAppSmokeTest`
Expected: PASS if an emulator/device is connected. If not, record the device availability gap.

- [ ] **Step 4: Inspect git status and verify only intended files changed**

Run: `git status --short`
Expected: Only the new UI/system files and the updated spec are modified.

- [ ] **Step 5: Commit the integrated redesign**

```bash
git add app docs/superpowers/specs/2026-06-24-lifelab-uiux-redesign-design.md
git commit -m "feat: deliver lifelab uiux redesign"
```

## Self-Review

### Spec Coverage

- New 4-tab shell: covered by Tasks 1 and 3
- Blue-white visual system and shared tokens: covered by Task 2
- Today hub: covered by Task 4
- Workbench consolidation: covered by Task 5
- Discover/Search redesign: covered by Task 6
- Notifications/Me redesign: covered by Task 7
- Spec color correction and final verification: covered by Task 8

### Placeholder Scan

- No `TODO`, `TBD`, or deferred implementation markers remain in the plan
- Every code-changing step includes a concrete code block
- Every verification step includes an exact command

### Type Consistency

- Top-level routes consistently use `today`, `workbench`, `discover`, `me`
- Shared shell components consistently use `LifeLabTopBar` and `LifeLabBottomBar`
- Workbench consistently uses `WorkbenchRoute`, `WorkbenchScreen`, and `WorkbenchSummary`
