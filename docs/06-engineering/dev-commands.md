# Development Commands

本文档收录后续开发中最常用的 Gradle、adb 和调试命令。由于当前仓库尚未迁移为 Android 工程，部分命令会在 Android 工程搭建后正式可用。

## 1. Gradle

```powershell
./gradlew tasks
./gradlew test
./gradlew assembleDebug
./gradlew installDebug
./gradlew lint
./gradlew connectedAndroidTest
```

用途：

- `tasks`: 查看可用任务
- `test`: 跑 JVM 单元测试
- `assembleDebug`: 构建 debug 包
- `installDebug`: 安装到设备
- `lint`: 跑静态检查
- `connectedAndroidTest`: 跑设备或模拟器 UI 测试

## 2. adb

```powershell
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop com.example.lifelab
adb shell pm clear com.example.lifelab
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

## 3. 定向启动与调试

```powershell
adb shell am start -n com.example.lifelab/.MainActivity
adb shell input text hello
adb shell input tap 500 1200
```

用途：

- 启动指定入口
- 做简单输入
- 模拟点击

## 4. Git

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

## 5. 推荐补充工具

- `ktlint` 或 `detekt`: Kotlin 代码风格和静态检查
- Android Studio Profiler: 性能分析
- Layout Inspector: Compose/布局检查
- App Inspection: 数据库、网络、DataStore 观察

## 6. 后续建议加入的脚本

等 Android 工程迁移完成后，建议补这些脚本：

- `./gradlew verify`: 汇总测试、lint、静态检查
- `./gradlew testDebugUnitTest`: 针对 debug 变体测试
- `./gradlew :app:dependencies`: 查看依赖树
