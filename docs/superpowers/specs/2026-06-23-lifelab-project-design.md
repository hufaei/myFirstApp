# LifeLab Project Charter And Delivery Strategy

## 1. 背景

当前仓库是一个新建 Kotlin 工程，尚未迁移为 Android 应用结构。项目目标不是快速拼出一个 demo，而是先建立一个可以作为长期学习样板和正式项目基线的 Android 综合案例。

用户目标明确：

- 以正式项目标准设计和实现
- 学习 Kotlin 与 Android 原生开发
- 覆盖大多数常见业务场景和技术点
- 项目结构能体现高级开发者应有的边界意识

## 2. 文档定位

本文档是项目章程和交付策略总纲，不是“一次性覆盖整个 app 的实施蓝图”。

它负责固定：

- 产品目标
- 架构方向
- 切片顺序
- 质量边界

真正进入开发前，每个切片都要单独产出自己的设计文档和 implementation plan。

## 3. 目标

LifeLab 要做成一个“个人效率与成长实验室”型综合 app，用统一产品壳承载多种典型业务场景：

- 首页推荐流
- 任务管理
- 习惯打卡
- 内容发现
- 搜索
- 消息通知
- 我的与设置

项目目标不是某个功能做到极深，而是通过一套可维护架构覆盖 Kotlin 与 Android 的高频开发点。

## 4. 非目标

第一阶段不追求：

- 真正商业化闭环
- 完整服务端系统
- 大规模多 module 拆分
- 教条式重型 DDD
- 兼容历史 View 系统为主的双栈实现
- 历史发版兼容、旧数据结构迁移和旧 UI 行为兼容

## 5. 设计原则

- `正式项目标准优先`: 结构、状态、测试、边界都按真实工程要求设计
- `学习友好`: 抽象必须服务于理解，而不是为了抽象而抽象
- `渐进复杂度`: 先定清晰包边界，再决定是否 Gradle 模块化
- `统一状态模型`: 页面统一处理加载态、内容态、空态、错误态
- `明确依赖方向`: UI 不碰网络和数据库，feature 不越界依赖

## 6. 交付策略

项目采用 `Modular Monolith + Vertical Slice Delivery`。

切片顺序固定为：

1. `Platform Baseline`
2. `Productivity Slice`，即 `Tasks + Habits`
3. `Content Discovery Slice`
4. `Account And Notifications Slice`

这样做的原因是：

- 先验证 Android 工程迁移、导航、依赖注入和基础状态模型
- 再用 `Tasks + Habits` 这条高价值链路验证 Room、DataStore、UseCase 和提醒能力
- 将尚未验证的内容流和账号系统延后，避免文档先于事实过度细化
- 当前开发机器只承担构建、测试和静态验证，设备或模拟器运行由用户在完整环境中执行

## 7. 架构决策

### 7.1 采用方案

采用 `Clean Architecture 为骨架 + 务实 DDD 建模 + feature-first 包结构`。

主链路如下：

`UI -> ViewModel -> UseCase(按需) -> Repository -> DataSource`

其中：

- 简单读取场景可由 ViewModel 直接调用 Repository
- 涉及规则编排或复用的场景必须进入 UseCase
- Repository 是统一数据访问入口

### 7.2 为什么不直接按全 app 总蓝图实施

文档总纲可以覆盖全局，但实现如果也按全 app 一次性铺开，会导致：

- 计划过大，验证不及时
- 早期架构判断缺少真实反馈
- 细节文档容易失真

因此总纲只固定原则，细节按切片设计。

### 7.3 为什么不走纯 MVVM

纯 MVVM 对小项目足够，但本项目需要覆盖多个 feature、多个数据源和明确的测试边界。若只靠 ViewModel + Repository 容易出现：

- ViewModel 承担过多业务决策
- 不同页面复制业务规则
- DTO、Entity、UI 模型混用

### 7.4 为什么不走全量重型 DDD

完整 DDD 战术设计适合高复杂度企业领域，但对当前项目过重。全量聚合根、领域事件、工厂、规格模式会显著抬高样板代码和学习门槛，不符合当前目标。

因此采用“务实 DDD”：

- 保留领域模型
- 保留统一语言
- 保留关键 use case
- 保留 repository contract
- 不强制每个简单动作都走完整领域对象生命周期

## 8. 功能范围

### 8.1 项目目标范围

- `Home`: Banner、推荐流、刷新、分页
- `Tasks`: 列表、详情、新增、编辑、筛选
- `Habits`: 打卡、统计、提醒
- `Discover`: 文章、课程、商品/会员混排
- `Search`: 搜索建议、历史、结果
- `Notifications`: 站内消息、系统通知
- `Profile`: 登录态、个人信息、设置

### 8.2 首个实施范围

首个实施范围只包含：

- `Platform Baseline`
- `Tasks`
- `Habits`

后续 `Home / Discover / Search / Profile / Notifications` 在对应切片设计通过后再进入实施。

### 8.3 典型能力覆盖

通过以上功能覆盖以下 Android 高频开发点：

- 导航
- 状态管理
- 表单与校验
- 列表与详情
- 网络请求
- 图片加载
- 本地缓存
- 设置持久化
- 通知和提醒
- 单元测试与 UI 测试

## 9. 代码结构

项目迁移到 Android 后，推荐目标结构如下：

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

说明：

- `app`: 应用入口、根导航、App 级初始化
- `core`: 跨 feature 公共能力
- `feature`: 每个一级业务模块的 data/domain/presentation 实现

在当前阶段，这些边界先依赖包结构、命名规范、测试和 review 维持，而不是一开始就拆成多个 Gradle module。

## 10. 领域设计

核心统一语言：

- `Task`
- `Habit`
- `FeedItem`
- `Article`
- `Offer`
- `Message`
- `Reminder`
- `UserPreference`

关键建模规则：

- 用 `data class` 建模稳定数据对象
- 用 `sealed interface/class` 建模有限状态或卡片类型
- 用 UseCase 承载中高复杂业务规则
- 不把网络 DTO 直接泄漏到 UI 层

当前真正被固定细化的领域重点是 `Task`、`Habit` 和 `UserPreference`。其余内容流和消息相关模型会在对应切片设计中展开。

## 11. 数据与状态流

统一数据流：

1. UI 发出 `UiEvent`
2. ViewModel 接收并转为业务调用
3. Repository/DataSource 获取数据
4. ViewModel 合并结果为 `UiState`
5. UI 被动渲染状态

统一状态结构：

- `UiState`: 稳定状态
- `UiEffect`: 一次性事件
- `Result/ErrorModel`: 跨层错误表达

每个关键页面必须具备：

- Loading
- Content
- Empty
- Error

## 12. 技术栈

- Kotlin
- Android Studio
- Jetpack Compose
- Navigation Compose
- ViewModel + StateFlow
- Coroutines + Flow
- Hilt
- Retrofit + OkHttp + Kotlinx Serialization
- Room
- DataStore
- Coil
- JUnit + MockK + Compose UI Test

## 13. Kotlin 学习覆盖

本项目需要覆盖的 Kotlin 重点：

- `val` / `var`
- 空安全
- `data class`
- `sealed class`
- 扩展函数
- Lambda
- 高阶函数
- 命名参数与默认参数
- 协程
- Flow / StateFlow
- 集合操作
- `when`

规范要求：

- 默认优先不可变数据
- 谨慎使用 `var`
- 禁止滥用 `!!`
- 用明确类型表达状态而不是布尔变量拼装

## 14. 测试策略

测试按三层执行：

- 单元测试：UseCase、Repository、ViewModel、Mapper
- UI 测试：Compose 页面状态与交互
- 集成验证：导航、缓存、登录态等跨层流程

质量门槛：

- 当前切片关键业务链路可测试
- 当前切片至少有基础 happy path
- 新增页面不允许无状态模型直接上屏
- TDD 可以使用，但新增测试必须对应明确行为或工程约束，不堆叠临时、重复、只保护实现细节的测试
- UI 和状态测试优先验证应当存在的稳定结果，默认不写以 `not have`、`not exist`、`not visible` 为核心的否定式测试
- 本机默认只要求测试、构建和静态验证通过；需要设备或模拟器的运行调试在交付说明中写清验证入口和预期行为

## 15. 实施顺序

### Phase 0: 文档基线

- 固定项目章程、架构边界、测试策略和交付顺序

### Slice A: Platform Baseline

- Android 工程迁移
- 主题、导航、依赖注入、Result/ErrorModel 和测试基线
- 网络、数据库、DataStore 只建立当前切片必需的最小配置，不提前铺完整能力

### Slice B: Productivity Slice

- 任务与习惯主链路
- Room、DataStore、Repository、UseCase、提醒首批落地

### Slice C: Content Discovery Slice

- 首页、发现、搜索
- 分页、图片、混排卡片

### Slice D: Account And Notifications Slice

- 消息、我的、登录态、设置
- 测试完善与静态检查

## 16. 风险与控制

### 风险 1: 架构过重

控制：

- 简单场景不强制 use case 化
- 单 module 先行

### 风险 2: 学习点太散

控制：

- 所有功能围绕统一产品壳
- 文档先行，明确每层职责

### 风险 3: 当前仓库不是 Android 工程

控制：

- 将工程迁移列为实现阶段第一优先级
- 所有后续计划都以 Android 工程为目标结构

### 风险 4: 总体目标与当前切片脱节

控制：

- 用交付策略文档固定切片顺序
- 每个切片进入开发前单独出 spec 和 plan

## 17. 结论

LifeLab 的最佳方向不是“几个 demo 拼盘”，也不是“一份大设计从头推到尾”，而是“一个统一产品壳下的正式项目级学习样板”，按切片逐步验证。它以 Clean Architecture 为骨架，以务实 DDD 进行关键建模，用 Kotlin 与 Android 现代栈覆盖真实开发中最常见的能力面。
