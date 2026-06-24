# Agent Guide

本文档是后续 Agent 继续开发 LifeLab 时的项目入口。它不替代 `README.md` 和 `docs/`，而是把当前项目状态、协作规则和工程约束集中成一份可快速接手的说明。

## 1. Project Snapshot

- 项目名：`LifeLab`
- 当前仓库：`myFirstApp`
- 当前阶段：文档基线已建立，代码仍是 Kotlin JVM 初始化模板，尚未迁移为 Android 工程
- 项目定位：面向学习的原生 Android 综合案例，但按接近正式团队项目的工程标准推进
- 产品主题：个人效率与成长实验室
- 当前交付策略：`Modular Monolith + Vertical Slice Delivery + feature-first package structure`

后续开发不要把它当成一次性 demo 拼盘。它应该按正式项目方式逐步演进：先建立稳定平台基线，再通过垂直切片验证真实功能链路。

## 2. Source Of Truth

开始任何开发前，按顺序阅读：

1. `README.md`
2. `docs/README.md`
3. `docs/superpowers/specs/2026-06-23-lifelab-project-design.md`
4. `docs/00-roadmap/delivery-strategy.md`
5. `docs/01-architecture/architecture-overview.md`
6. `docs/01-architecture/module-boundaries.md`
7. `docs/02-domain/domain-model.md`
8. `docs/05-quality/testing-strategy.md`
9. `docs/06-engineering/coding-standards.md`
10. `docs/06-engineering/dev-commands.md`

如果本文档与 `docs/` 中的详细规范冲突，以更具体、更近期的设计文档为准。修改工程策略时，同步更新相关文档，不要只改代码。

## 3. Delivery Order

固定交付顺序：

1. `Platform Baseline`
2. `Productivity Slice`，即 `Tasks + Habits`
3. `Content Discovery Slice`
4. `Account And Notifications Slice`

当前下一步应进入 `Platform Baseline` 的切片设计与实施，不要越过平台基线直接开发任务、习惯、首页或账号功能。

每个切片进入实现前，应先有对应设计文档和 implementation plan。不要用一份总设计直接覆盖全 app 的实现细节。

## 4. Architecture Rules

目标架构采用 Clean Architecture 为骨架，结合务实 DDD 和 feature-first 包结构。

Android 工程迁移后的目标结构：

```text
app/
  src/main/java/com/example/lifelab/
    app/
    core/
      common/
      ui/
      network/
      database/
      datastore/
      model/
      testing/
    feature/
      home/
      tasks/
      habits/
      discover/
      search/
      notifications/
      profile/
```

基本依赖方向：

- Presentation 可以依赖 Domain 和 Core
- Data 可以依赖 Domain 和 Core
- Domain 只依赖少量无业务语义的 Core 能力
- Core 不依赖 feature
- UI 不直接访问 Retrofit、Room、DataStore
- feature 之间不能直接依赖对方内部 data 或 presentation 实现

共享能力只有在出现第二个真实消费者后才上提到 `core`。禁止为了未来假想需求预建空抽象。

## 5. State And Data Flow

统一采用状态驱动 UI：

1. UI 发出 `UiEvent`
2. ViewModel 接收事件并调用 UseCase 或 Repository
3. Repository/DataSource 获取数据
4. ViewModel 合并结果为 `UiState`
5. UI 根据状态渲染
6. 一次性消息通过 `UiEffect` 下发

页面至少考虑：

- Loading
- Content
- Empty
- Error

复杂业务规则进入 UseCase。简单读取场景可以由 ViewModel 直接调用 Repository，但数据访问仍必须统一经过 Repository。

## 6. Engineering Standards

遵守 `docs/06-engineering/coding-standards.md`，重点包括：

- Kotlin 默认使用 `val`
- 避免滥用 `!!`
- 有限状态优先用 `sealed class` 或 `sealed interface`
- 数据对象优先用 `data class`
- 函数保持短小、单一职责
- Compose 页面拆分为 `Route` 和 `Screen`
- ViewModel 只暴露稳定 `UiState`
- DTO、本地 Entity、Domain Model、UI Model 按需隔离，不把 DTO 直接暴露给 UI
- 不建立万能 `utils` 文件

代码应服务于可维护性、可测试性和边界清晰。不要为了“像企业项目”而堆无实际消费者的层级。

## 7. Testing Expectations

遵守 `docs/05-quality/testing-strategy.md`。

TDD 可以作为实现流程，但新增测试必须控制质量：

- 每个新增测试都要对应明确业务行为、状态转换或工程约束
- TDD 迭代中产生的临时、重复、只保护实现细节的测试必须及时合并或删除
- 不为了“覆盖率好看”堆叠低价值测试
- UI 和状态测试优先验证应当存在的稳定结果，例如关键文案、按钮、状态或数据可见
- 除非产品规则、安全规则或明确缺陷需要，不写以 `not have`、`not exist`、`not visible` 为核心的否定式测试
- 测试名称要表达行为和条件，而不是只描述方法名或组件名

声称阶段完成前，至少满足：

- 当前切片相关单元测试通过
- 新增核心页面有基础 UI 测试或可解释的测试替代方案
- 构建通过
- 无阻塞级崩溃
- 文档与实际结构没有明显偏离

第一批重点验证对象：

- App 启动与导航壳 smoke test
- 基础依赖注入
- Result/ErrorModel
- 后续 `Tasks + Habits` 的核心 UseCase 和 ViewModel

## 8. Development Workflow

每次开始工作前：

1. 查看 `git status --short --branch`
2. 阅读与当前任务相关的文档
3. 明确当前任务属于哪个切片
4. 保持修改范围小，不做无关重构
5. 若发现用户已有未提交修改，必须保留并围绕它们工作

推荐验证命令：

```powershell
./gradlew test
./gradlew tasks
```

Android 工程迁移后再逐步启用：

```powershell
./gradlew assembleDebug
./gradlew lint
```

具备设备或模拟器时再运行：

```powershell
./gradlew connectedAndroidTest
```

## 9. Local Verification And Runtime Policy

当前机器只作为开发与自动化验证环境，不要求始终具备完整 Android 运行调试能力。

- 后续 Agent 只需要跑通与当前改动匹配的测试和静态验证
- 不要求为了本机运行 app 额外下载或配置 IDE 插件、模拟器、设备环境或其它大体量工具
- 本机无法完整运行 app 不应单独阻塞开发，只要测试、构建或静态检查能证明当前改动质量
- 若某项变更确实需要人工运行验证，应在交付说明中明确验证入口、预期行为和未在本机验证的原因
- 实际运行调试默认由用户在另一台机器拉取代码后完成

## 10. Documentation Rules

- 长期稳定约束写入 `docs/00-roadmap`、`docs/01-architecture`、`docs/05-quality`、`docs/06-engineering`
- 单个切片的详细设计写入 `docs/superpowers/specs/`
- 实施计划写入 `docs/superpowers/plans/`
- 文档不要伪精确描述尚未进入实施的后续切片
- 代码结构发生有意义变化时，同步更新文档索引和相关约束

## 11. Release And Compatibility Assumptions

本项目当前作为学习 demo 和最终样板应用推进，不维护历史发版兼容性。

- 新增代码默认只服务最终版目标形态
- 不为尚不存在的旧版本、旧数据结构、旧 API 或历史 UI 行为保留兼容分支
- 如果迁移过程中需要删除模板代码、重命名包、调整数据模型，优先保持最终结构清晰
- 只有在当前切片明确需要验证迁移能力时，才引入迁移或兼容逻辑

## 12. Current Next Step

下一步建议：

1. 为 `Platform Baseline` 编写切片设计文档
2. 将 Kotlin JVM 模板迁移为 Android app 工程
3. 建立 Compose 宿主、主题、根导航和基础包结构
4. 引入基础依赖注入、统一错误模型和测试基线
5. 建立最小 App 启动入口和自动化验证链路

在完成 `Platform Baseline` 之前，不要进入 `Tasks + Habits` 的正式功能实现。
