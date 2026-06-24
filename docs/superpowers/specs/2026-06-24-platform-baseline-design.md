# Platform Baseline Design

## 1. Purpose

`Platform Baseline` 的目标是把当前 Kotlin JVM 模板迁移为 LifeLab 的 Android 主体工程，并建立后续功能模块可以并行接入的稳定边界。

本切片只负责平台骨架，不实现具体业务功能。`Tasks`、`Habits`、`Home`、`Discover`、`Search`、`Notifications`、`Profile` 后续由独立新会话作为模块负责人推进。

## 2. Scope

本切片包含：

- Android `app` module
- `com.example.lifelab` 包名与 application id
- `Application` 与 `MainActivity`
- Compose 宿主和主题入口
- 根导航骨架与一级功能占位路由
- 基础 `Result/ErrorModel`
- 最小 Hilt 配置
- 最小测试基线
- 模块负责人会话的派发规则文档

本切片不包含：

- 真实任务、习惯、内容、账号、消息业务
- 完整 Room schema
- 完整 Retrofit API
- 完整 DataStore 偏好体系
- 历史版本兼容或旧数据迁移

## 3. Architecture

项目继续采用单 `app` module 内的逻辑分层：

```text
app/src/main/java/com/example/lifelab/
  app/
    LifeLabApplication.kt
    MainActivity.kt
    LifeLabApp.kt
    navigation/
  core/
    common/
    ui/
  feature/
    home/
    tasks/
    habits/
    discover/
    search/
    notifications/
    profile/
```

依赖方向：

- `app` 负责应用装配、根导航、主题入口
- `core` 只放跨 feature 的基础能力
- `feature/*` 只暴露当前阶段的占位 Route
- feature 间不互相依赖内部实现

## 4. Data And State Flow

本切片只建立状态流约定，不实现业务数据流：

1. Feature Route 作为导航入口
2. 后续 feature 内部使用 `Route -> Screen -> UiState/UiEvent/UiEffect`
3. 业务结果通过 `AppResult` 和 `AppError` 统一表达
4. 后续 Repository/DataSource 只能从 feature 或 core 的明确边界接入

## 5. Error Handling

`core/common` 提供最小错误模型：

- `AppResult.Success<T>`
- `AppResult.Failure`
- `AppError.Network`
- `AppError.Storage`
- `AppError.Validation`
- `AppError.Unknown`

本切片只测试模型行为，不提前设计完整错误展示策略。

## 6. Module Ownership Model

主体架构完成后，功能模块通过新建 Codex 会话交付。每个模块会话必须作为模块负责人，而不是单纯代码 worker。

模块负责人会话职责：

- 阅读 `Agent.md`、项目章程、Platform Baseline 设计和实施计划
- 为自己的模块设定 goal
- 制定模块级设计、验收标准和任务拆分
- 派发自己会话内的子 agent 执行代码撰写
- 在交付前完成静态 review，检查逻辑漏洞、边界污染和测试质量
- 只提交自己模块范围内的代码，不改其它模块内部实现

主会话职责：

- 维护全局架构和模块边界
- 处理跨模块依赖、导航契约和共享能力上提
- 审查模块会话交付结果
- 决定何时创建下一批模块会话

## 7. Testing Strategy

本切片测试重点：

- `AppResult/AppError` 的基本行为
- 顶层导航目的地定义稳定
- Compose 宿主具备可测试入口

本机没有完整 Android 运行环境时，不以设备或模拟器运行作为阻塞条件。必须保留可执行的 Gradle/JVM 测试或静态验证，并在交付说明中写清未执行的 Android 运行项。

新增测试遵守高质量规则：不堆覆盖率，不保留 TDD 临时测试，默认不写以 `not have`、`not exist`、`not visible` 为核心的否定式测试。

## 8. Acceptance

本切片完成时应满足：

- Android 工程文件结构已建立
- Kotlin JVM 模板入口已移除
- 根包名、application id 和文档一致
- 一级功能模块目录已清晰划分
- 最小导航占位可以表达所有一级模块
- 基础错误结果模型有单元测试
- 本机可执行的测试和静态验证已运行或明确说明环境阻塞原因
- 后续模块负责人会话的初始提示词模板已落地
