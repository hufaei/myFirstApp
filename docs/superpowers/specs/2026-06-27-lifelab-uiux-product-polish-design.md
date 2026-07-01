# LifeLab UI/UX Product Polish Design

## Summary

This stage turns LifeLab from a technically working local-first app into a clearer product experience. The app should feel like a Chinese-first personal planning and growth log: users open it to see today's work, complete tasks, check in habits, attach a few photos, and adjust basic preferences without learning the app's internal modules.

The release scope is product polish, not a new backend or new feature expansion. Keep the local-first Room/DataStore foundation, the existing package id `com.study.lifelab`, and the blue-white brand direction based on `#89C2FF` and `#E6F7FF`. Release packaging is allowed after the branch is reviewed and merged, but no APK should be built on PR.

## Product Positioning

LifeLab is a local-first personal efficiency and growth record app. Its first useful loop is:

1. Open Home.
2. See what matters today.
3. Create or complete a task.
4. Check in a habit.
5. Attach up to three photos when context matters.
6. Review preferences and experiments from Profile.

The app is Chinese-first. English remains supported through string resources, but the Chinese locale must not show avoidable English leftovers from internal state strings.

## Current Problems

- Home is still mostly an entry collection rather than a daily dashboard.
- Tasks and Habits expose too many controls at the same visual level, which makes buttons feel crowded and makes the main action less obvious.
- Some buttons can compress poorly on small screens because rows use fixed horizontal layouts with long text.
- Screen structure, cards, empty states, loading states, and message banners are implemented separately per feature.
- Profile reads like a demo settings page instead of an app settings surface.
- Search, Discover, Notifications, and Web Lab work as secondary pages but use inconsistent spacing and state presentation.
- Tests still contain early in-memory repository tests that duplicate newer Room/DataStore and ViewModel coverage.

## Design Direction

### Information Architecture

Bottom navigation stays at four destinations:

- Home: daily dashboard and quick actions.
- Tasks: task list, task detail, task editor.
- Habits: habit check-in list.
- Profile: preferences, messages, Web Lab, app identity.

Secondary pages remain reachable but do not return to the bottom bar:

- Search from Home.
- Notifications from Home and Profile.
- Discover from Home.
- Web Lab from Profile.

### Visual System

The UI should stay quiet, useful, and app-like. Avoid a marketing-style landing page, decorative gradients, oversized hero sections, and nested cards.

Color tokens:

- `LifeBlue`: `#89C2FF`
- `LifeMist`: `#E6F7FF`
- `LifeBlueStrong`: deepened accessible action blue derived from `#89C2FF`
- `LifeInk`: near-black blue text for light mode
- `LifeNavy`: deep blue-black background for dark mode
- `LifeNavySurface`: elevated dark surface that still reads blue, not gray

Cards should use restrained 8 dp or lower corners unless Material defaults are already suitable. Button heights and minimum widths should be stable so long Chinese or English labels do not deform controls.

### Shared UI Components

Add a small shared UI layer only for repeated product patterns:

- `LifeLabScreenHeader`: title, optional subtitle, optional icon actions.
- `LifeLabStateCard`: loading, empty, error, and informational states.
- `LifeLabMessageBanner`: success/info/error feedback with optional dismiss.
- `LifeLabSectionTitle`: section title with optional action.
- `LifeLabPhotoStrip`: shared photo preview and attachment row for task/habit owners.
- `LifeLabPrimaryActionRow`: stable primary/secondary action layout that stacks on narrow widths.

These components should not hide feature logic. They only standardize layout and behavior.

### Home

Home becomes a daily dashboard:

- Top summary: greeting-style title, date/today label, search/messages/refresh icons.
- Primary summary cards: today's tasks and today's habits.
- Quick actions: create task, open tasks, open habits, explore Discover.
- Recent feed: discovery teaser or recent state, but secondary to the daily loop.

Home should not teach the internal architecture. Labels should use user goals such as "今日任务", "习惯打卡", "继续探索".

### Tasks

Tasks should optimize for fast capture and completion:

- Header: title plus one clear "新建任务" action.
- Filter uses a stable segmented control or compact filter row that does not compete with creation.
- List cards show title, status, due label, and one line of description. Priority should be visual but subtle.
- Detail mode prioritizes complete/restore as the main action, edit as secondary.
- Editor uses grouped fields and a bottom/full-width save action.
- Photo records use the shared photo strip. Album and camera actions are icon-first with tooltips/accessible labels.

### Habits

Habits should optimize for one-tap check-in:

- Header and stats should be scannable, not dominant.
- Each habit card highlights name, current streak, and check-in state.
- The main button is check-in. Reminder controls and photo records are secondary and should not visually crowd the check-in action.
- Reminder time changes should remain simple, but labels must not force layout deformation.

### Profile And Secondary Pages

Profile should read as "我的和设置":

- Account header remains, but settings are grouped into appearance, language, notifications, default task behavior, and lab.
- Language/theme controls use stable selectable controls with consistent width/line behavior.
- Web Lab remains experimental and clearly placed under Lab.

Search, Discover, Notifications, and Web Lab should receive consistency fixes:

- Shared headers and state cards.
- Rounded cards instead of rectangular one-off cards.
- Error/empty states with actionable copy.
- Back button alignment consistent with other secondary pages.

### Copy And Localization

- Chinese resources are the default and should be complete.
- English resources should stay complete for every new key.
- UI code should avoid comparing English messages and mapping them to Chinese. Prefer state enums or resource ids where touched.
- Existing persisted user-created content is not translated.

## Testing Strategy

Tests should protect behavior, not historical implementation details.

Keep or strengthen:

- Manifest/icon/package/release signing static guards.
- Room repository behavior for task, habit, search, notification, discover, and photo records.
- DataStore preference mapping for theme/language/default task filter.
- Task ViewModel happy path: create, filter, complete/restore, attach photos.
- Habit ViewModel happy path: check in, duplicate check-in, reminder update, attach photos.
- Search ViewModel core path: submit, record history, filter, clear history.
- One app shell/navigation smoke test.
- UI contract tests where practical for shared component copy and stable navigation labels.

Remove or merge:

- In-memory repository tests whose behavior is already covered by Room repository or ViewModel tests.
- Overly granular ViewModel tests that assert intermediate implementation states without user-visible value.
- Static string checks that duplicate resource compilation unless they guard a known regression.

Local machine verification may be blocked by missing Android SDK. GitHub Actions remains the authoritative Android build/test/lint environment.

## Release Criteria

Before tag/release:

- PR branch is merged to `main`.
- PR CI passes unit test and lint jobs.
- No release APK is assembled on PR.
- Version code and version name are intentionally bumped for this user-visible UI/UX release.
- A `v*` tag is created from merged `main`.
- GitHub Release is created from the tag.
- Release APK workflow runs only from the tag release path and requires signing secrets.

## Out Of Scope

- Backend server or sync.
- Account login.
- Cloud photo upload.
- Custom camera UI.
- Adding bottom navigation items.
- Reworking app package id or release signing design.
- Full visual screenshot automation, unless a device/emulator is available.
