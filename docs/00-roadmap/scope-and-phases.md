# Scope And Phases

## 1. 范围定义

LifeLab 的范围不是“做一个最小能跑的 app”，而是“做一个可持续扩展的学习型正式项目”。范围控制的重点不是减少模块名称，而是把实现拆成小而完整的可验证切片。

## 2. 项目目标范围

项目最终目标范围仍然包含以下一级业务：

- 首页、任务、习惯、发现、搜索、消息、我的 7 个一级功能模块
- Android 单应用工程与主导航框架
- 本地数据库、设置持久化、远程接口模拟或对接
- 登录态、错误态、空态、加载态统一处理
- 单元测试、Compose UI 测试、基础集成验证
- 开发命令、编码规范、文档体系落地

## 3. 当前执行范围

第一阶段执行范围不覆盖全部一级业务，而只覆盖：

- `Platform Baseline`
- `Productivity Slice`，即 `Tasks + Habits`

这两个切片先用于验证：

- Android 工程迁移是否顺利
- 单 `app` module + 包边界是否足够支撑当前复杂度
- Clean Architecture + 务实 DDD 的落地是否不过重
- Room、DataStore、UseCase、ViewModel、StateFlow、通知/提醒等核心链路是否顺畅

## 4. out of scope

- 多端共享代码
- 真正支付能力
- 完整 IM 聊天系统
- 复杂离线同步冲突解决
- 运营后台与服务端实现
- 大规模动态化与插件化

## 5. 阶段规划

### Phase 0: 文档基线

- 固定产品方向、架构边界、测试策略和交付顺序
- 明确项目章程与切片交付方式

### Slice A: Platform Baseline

- App 入口、底部导航、全局主题
- Hilt、网络层、Room、DataStore、日志、结果封装
- 假数据和环境配置

### Slice B: Productivity Slice

- 任务列表、详情、新增/编辑、状态切换
- 习惯打卡、统计卡片、提醒
- 本地优先与远程同步示例

### Slice C: Content Discovery Slice

- 首页推荐流
- 发现页内容/商品混排
- 搜索、筛选、分页与图片加载

### Slice D: Account And Notifications Slice

- 登录态与个人信息
- 设置与偏好
- 消息中心、通知
- 测试补全、静态检查、文档补全

## 6. 复杂度控制原则

- 先保留单 `app` module，按包边界先做出模块感
- 只对中高复杂业务强制 use case 化
- 优先展示清晰边界和可测试性，不为“纯架构美观”制造额外复杂度
- 每个切片都必须可运行、可验证、可继续扩展
