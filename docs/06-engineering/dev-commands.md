# Development Commands

本文档收录后续开发中最常用的 Gradle、adb 和调试命令。当前仓库已建立 Android `app` module；需要 Android SDK 的命令只有在本机配置完整 SDK 后才可执行。

当前机器默认只作为开发与自动化验证环境，不要求始终具备完整 IDE、模拟器或真机调试能力。后续 Agent 默认跑构建、测试和静态验证；需要设备或模拟器的运行调试由用户在完整环境中执行。

## 1. Gradle

默认验证命令：

```powershell
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Android 工程中常用的定向单元测试命令：

```powershell
./gradlew :app:testDebugUnitTest
```

辅助检查命令：

```powershell
./gradlew tasks
./gradlew :app:checkKotlinGradlePluginConfigurationErrors
```

需要 Android 设备或模拟器时再运行：

```powershell
./gradlew installDebug
./gradlew connectedAndroidTest
```

用途：

- `tasks`: 查看可用任务
- `:app:testDebugUnitTest`: 跑 app debug 变体 JVM 单元测试
- `:app:lintDebug`: 跑 debug 变体 lint
- `:app:assembleRelease`: 构建正式签名 APK，仅在 release signing 属性齐全时使用
- `installDebug`: 安装到设备，默认不作为本机验证要求
- `connectedAndroidTest`: 跑设备或模拟器 UI 测试，默认不作为本机验证要求

## 2. GitHub Actions

仓库提供 `.github/workflows/android-ci.yml`。远端 CI 使用 GitHub-hosted `ubuntu-24.04` runner，该镜像预装 Android SDK，并设置 `ANDROID_HOME` / `ANDROID_SDK_ROOT`。CI 当前执行：

```bash
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:lintDebug --no-daemon
```

PR workflow 只执行上述 unit test 和 lint，不构建 APK。推送 `v*` tag 时，CI 会检查 release signing secrets，解码 release keystore，并执行：

```bash
./gradlew :app:assembleRelease --no-daemon \
  -PLIFELAB_RELEASE_STORE_FILE="$RUNNER_TEMP/lifelab-signing/lifelab-release.jks" \
  -PLIFELAB_RELEASE_KEY_ALIAS="$ANDROID_RELEASE_KEY_ALIAS" \
  -PLIFELAB_RELEASE_KEY_PASSWORD="$ANDROID_RELEASE_KEY_PASSWORD" \
  -PLIFELAB_RELEASE_STORE_PASSWORD="$ANDROID_RELEASE_STORE_PASSWORD"
```

生成的 artifact 名称为 `lifelab-release-apk`，路径为 `app/build/outputs/apk/release/app-release.apk`。release 签名配置、secret 生成和发版步骤见 [release-signing.md](release-signing.md)。

本机如果没有 Android SDK，相关 Gradle 任务会在 SDK discovery 阶段失败；这种情况下以远端 CI 或用户完整 Android 环境的结果作为最终编译/测试证据。

## 3. adb

以下命令只在具备 Android SDK、设备或模拟器时使用：

```powershell
adb devices
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell am force-stop com.study.lifelab
adb shell pm clear com.study.lifelab
adb logcat
adb shell dumpsys activity activities
```

用途：

- 查看设备
- 重装 APK
- 强制停止应用
- 清空应用数据
- 查看运行日志
- 检查页面栈与 activity 状态

## 4. 定向启动与调试

以下命令用于人工运行调试，不作为当前机器默认验收条件：

```powershell
adb shell am start -n com.study.lifelab/com.example.lifelab.app.MainActivity
adb shell input text hello
adb shell input tap 500 1200
```

用途：

- 启动指定入口
- 做简单输入
- 模拟点击

## 5. Git

```powershell
git status
git diff
git log --oneline --decorate -10
git switch -c feature/<topic>
```

用途：

- 查看当前变更
- 检查差异
- 查看最近提交
- 新建功能分支

## 6. 推荐补充工具

- `ktlint` 或 `detekt`: Kotlin 代码风格和静态检查
- Android Studio Profiler: 性能分析
- Layout Inspector: Compose/布局检查
- App Inspection: 数据库、网络、DataStore 观察

## 7. 后续建议加入的脚本

等 Android 工程迁移完成后，建议补这些脚本：

- `./gradlew verify`: 汇总测试、lint、静态检查
- `./gradlew testDebugUnitTest`: 针对 debug 变体测试
- `./gradlew :app:dependencies`: 查看依赖树
