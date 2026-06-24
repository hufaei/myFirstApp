# Integration Log

本文档是主会话维护的模块集成台账。它记录模块负责人会话、worktree、预期分支和当前主会话观察到的状态。

## Platform Branch

- Branch: `codex/platform-baseline-architecture`
- Integration state: all seven module branches have been merged into this platform branch; use first-parent history for the exact merge commits
- Shared dependency owner: main controller thread
- Android SDK status on controller machine: not configured; Android Gradle tasks are expected to stop at SDK discovery here
- Android Gradle Plugin: upgraded to `8.6.1` with Gradle wrapper `8.7` to satisfy AndroidX/Compose dependency metadata requiring AGP 8.6+ while keeping `compileSdk = 35`
- Remote verification: `.github/workflows/android-ci.yml` runs unit tests, lint, and debug assemble on GitHub-hosted `ubuntu-24.04` with Android SDK

## Module Threads

| Module | Thread ID | Worktree | Expected Branch | Observed Base | Main-Thread Note |
|---|---|---|---|---|---|
| Tasks | `019ef769-f10f-7711-9428-ec2b4e08717c` | `C:/Users/17346/.codex/worktrees/1c94/myFirstApp` | `codex/tasks-module` | `9309da8` | Pushed and merged into platform integration branch |
| Habits | `019ef76a-354a-7f53-be01-311b956d3384` | `C:/Users/17346/.codex/worktrees/f101/myFirstApp` | `codex/habits-module` | `95e368a` | Pushed and merged into platform integration branch |
| Home | `019ef76a-790c-7aa3-82a0-af08d03e4fcd` | `C:/Users/17346/.codex/worktrees/be57/myFirstApp` | `codex/home-module` | `9309da8` | Pushed and merged into platform integration branch |
| Discover | `019ef76a-c6f6-70c1-b83e-1466d5989af3` | `C:/Users/17346/.codex/worktrees/7dfc/myFirstApp` | `codex/discover-module` | `9309da8` | Pushed and merged into platform integration branch |
| Search | `019ef76b-1520-7140-8893-131ef2a8614d` | `C:/Users/17346/.codex/worktrees/a36e/myFirstApp` | `codex/search-module` | `9309da8` | Pushed and merged into platform integration branch |
| Notifications | `019ef76b-5d0e-7df3-bd41-36c79110db3c` | `C:/Users/17346/.codex/worktrees/a9a6/myFirstApp` | `codex/notifications-module` | `9309da8` | Pushed and merged into platform integration branch |
| Profile | `019ef76b-b179-77b0-9756-27d74c54ae57` | `C:/Users/17346/.codex/worktrees/2d39/myFirstApp` | `codex/profile-module` | `9309da8` | Pushed and merged into platform integration branch |

## Shared Coordination Decisions

- Module owners must not commit `app/build.gradle.kts` changes.
- ViewModel tests should use `com.example.lifelab.core.testing.MainDispatcherRule`.
- Shared dependencies are maintained on `codex/platform-baseline-architecture`.
- Feature modules should keep implementation under their own `feature/<module>` package unless the main controller approves a shared contract change.
