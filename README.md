# LifeLab

LifeLab 是一个面向学习的原生 Android 综合案例项目。它的目标不是做一个单一业务很深的产品，而是用一个结构清晰、标准接近正式团队项目的示例，系统覆盖 Kotlin 与 Android 开发中的高频概念、常见模块边界和工程实践。

当前仓库已建立 Android `app` module 主体骨架，包含 Compose 宿主、根导航、基础包边界、平台测试基线和 7 个最小正式功能模块。当前集成分支已经把 `Home`、`Tasks`、`Habits`、`Discover`、`Search`、`Notifications`、`Profile` 接入根导航；各模块先采用内存数据源完成端到端学习 demo，后续再按切片引入 Room、DataStore、网络和系统能力。

## 项目目标

- 用一个项目覆盖列表、详情、表单、搜索、分页、登录态、通知、本地存储、网络请求等典型场景
- 用正式项目可接受的标准组织代码：Clean Architecture 为骨架，务实引入 DDD 建模
- 用 Kotlin 主流写法组织业务：Compose、ViewModel、StateFlow、Coroutines、Repository、Room、DataStore、Hilt
- 让项目既能作为学习样板，也能逐步演进为可维护的真实工程

## 产品方向

LifeLab 的主题是“个人效率与成长实验室”，统一承载以下业务场景：

- 首页：信息流、推荐卡片、刷新与分页
- 任务：待办、任务详情、编辑与筛选
- 习惯：每日打卡、连续记录、提醒
- 发现：文章、课程、商品/会员混合内容
- 搜索：历史记录、热词、结果页
- 消息：站内消息、系统通知
- 我的：账号信息、偏好设置、主题与配置

## 交付策略

项目不再按“一份总设计覆盖整个 app”的方式推进，而是采用：

- `项目章程`：定义长期稳定的产品目标、架构边界和工程约束
- `垂直切片`：每次只为当前实施切片写详细设计和 implementation plan

当前固定的交付顺序是：

1. `Platform Baseline`：Android 工程迁移、Compose 宿主、导航、依赖注入、错误模型、测试基线
2. `Productivity Slice`：`Tasks + Habits`，优先打通 Room、DataStore、UseCase、提醒与核心业务流
3. `Content Discovery Slice`：`Home + Discover + Search`
4. `Account And Notifications Slice`：`Profile + Notifications`

## 技术方向

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

## Android CI

GitHub Actions 的日常 PR 只运行 `:app:testDebugUnitTest` 和 `:app:lintDebug`，不自动构建 APK。需要 release APK 时，在 Actions 页面手动运行 `Android CI` workflow，或推送 `v*` 版本 tag；workflow 会要求配置 Android release signing secrets，并上传 `lifelab-release-apk` artifact，保留 7 天。

稳定升级链路约束见 [Release Signing](docs/06-engineering/release-signing.md)：首次从 `com.example.lifelab` 切到 `com.study.lifelab` 会被 Android 视为新应用，不能覆盖旧包；之后只要 `applicationId`、release 签名保持一致且 `versionCode` 递增，同一渠道 APK 就可以覆盖升级。

## 文档入口

- [项目文档总览](docs/README.md)
- [项目章程与交付策略](docs/superpowers/specs/2026-06-23-lifelab-project-design.md)
- [产品愿景](docs/00-roadmap/product-vision.md)
- [范围与阶段](docs/00-roadmap/scope-and-phases.md)
- [交付策略](docs/00-roadmap/delivery-strategy.md)
- [架构总览](docs/01-architecture/architecture-overview.md)
- [模块边界](docs/01-architecture/module-boundaries.md)
- [领域模型](docs/02-domain/domain-model.md)
- [测试策略](docs/05-quality/testing-strategy.md)
- [开发命令](docs/06-engineering/dev-commands.md)
- [Release Signing](docs/06-engineering/release-signing.md)
- [编码规范](docs/06-engineering/coding-standards.md)
- [模块会话协作提示词](docs/07-coordination/module-thread-prompts.md)

## 当前阶段

当前处于 `Integrated Demo Slice` 阶段：平台基线和 7 个模块负责人会话的首批功能切片已经合并。下一步重点是在具备 Android SDK 的环境中跑通 CI/本地完整验证，并按后续切片逐步替换内存数据源、补持久化、网络和设备级验证。
