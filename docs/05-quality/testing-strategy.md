# Testing Strategy

## 1. 测试目标

LifeLab 的测试保护稳定产品行为，而不是保护每一种历史实现方式。当前阶段优先验证：

- 任务、习惯、搜索、发现、消息、资料页等核心用户路径不会回归
- Room、DataStore 和媒体存储策略等本地优先数据契约稳定
- ViewModel 对加载、空态、错误、重试、筛选、提交等 UI 状态流转有覆盖
- Manifest、应用身份、种子数据本地化、发布签名和版本配置等静态约束可被自动检查
- PR 质量门只运行测试和 lint，发布 APK 构建只在 `v*` tag 路径运行

## 2. 测试质量原则

- 每个测试必须对应明确业务规则、用户可感知状态、数据持久化契约或工程发布约束
- 优先保留能覆盖真实数据路径的 Room/DataStore 测试和能覆盖用户操作的 ViewModel 测试
- 删除被更高价值测试覆盖的 in-memory repository 变体测试，避免维护重复实现细节
- UI/状态测试关注稳定结果，例如关键文案、按钮、状态、列表内容和错误恢复
- 不为了测试数量保留只证明 getter/setter、样板映射或旧 fake 行为的测试
- 新增测试名称要表达行为和条件，而不是只描述方法名

## 3. 核心测试矩阵

### Static Guards

保留并持续维护：

- `AppIdentityGradleConfigurationTest`
- `AndroidManifestConfigurationTest`
- `SeedDataLocalizationTest`
- 发布工作流静态检查，确保 `assembleRelease`、签名 secret 校验和 APK artifact 上传只在 `refs/tags/v*` 下运行

### Data And Storage

保留并持续维护：

- DataStore 偏好映射：主题、语言、通知、默认任务筛选
- Room repository：任务、习惯、搜索、发现、消息、照片记录等持久化映射和关键查询
- 媒体策略：最多三张照片、owner 隔离、app-specific 文件路径和相机文件创建

不再单独保留：

- 与 Room repository 或 ViewModel 覆盖重复的 `InMemory...RepositoryTest`
- 只验证 demo seed repository 固定列表的低价值测试，除非该列表本身成为产品契约

### Domain And ViewModel

保留并持续维护：

- Task/Habit/Search/WebLab ViewModel 的核心 happy path 和错误/重试路径
- 任务创建、筛选、完成/恢复、照片附加
- 习惯打卡、重复打卡、提醒更新时间、照片附加
- 搜索提交、历史记录、筛选、清空历史、慢请求竞争处理
- Discover/Notifications 的加载、筛选、状态变更和错误恢复
- Profile 偏好持久化和资料概览映射

## 4. UX Flow Policy

UI polish 类变更优先依靠状态和 ViewModel 测试保护行为，再用人工或设备验证检查视觉层：

- 主要页面应有可验证的加载、空态、错误、内容态
- 长中文/英文标签应通过稳定控件、换行或堆叠布局避免挤压
- 次级页面的返回、刷新、重试、筛选、归档、已读等操作不得因外壳统一而改变
- WebLab 必须继续加载 `https://hufaei.github.io/`，保留网页返回、刷新、错误覆盖层和外部链接跳转

## 5. 完成门槛

在声称一个阶段完成前，应至少完成：

- `.\gradlew.bat :app:testDebugUnitTest --no-daemon`
- `.\gradlew.bat :app:lintDebug --no-daemon`
- `git diff --check`
- 中英文 `strings.xml` key 对齐检查
- Android CI workflow 静态检查，确认 PR 不构建 release APK

如果本机缺少 Android SDK 或环境阻塞 Gradle，记录原始错误，并运行可执行的静态检查作为替代证据。
