# Testing Strategy

## 1. 测试目标

LifeLab 的测试不是为了堆覆盖率数字，而是为了验证：

- 关键业务规则不被回归破坏
- 页面状态流转符合预期
- 数据层在成功、失败、空数据场景下都行为稳定
- 关键用户路径在工程演进后仍可运行

## 2. 测试金字塔

### 单元测试

重点覆盖：

- UseCase
- Repository
- Mapper
- ViewModel
- 日期、筛选、状态转换等纯逻辑

这是主力测试层，数量最多、运行最快。

### UI 测试

重点覆盖：

- Compose 页面在不同 `UiState` 下的展示
- 表单输入与按钮交互
- 列表点击与导航触发
- 错误态与重试行为

### 集成级验证

重点覆盖：

- 导航主链路
- Repository 与本地/远程数据源联动
- 登录态、设置项、缓存回填等跨层行为

## 3. 切片式质量门槛

测试门槛按“当前正在交付的切片”计算，不对未进入实现的后续功能做空承诺。

### Slice A: Platform Baseline

- App 启动路径可验证
- 主导航壳至少有一条 smoke test
- 基础依赖注入与 Result/ErrorModel 至少有单元测试

### Slice B: Productivity Slice

- `Tasks + Habits` 的关键 UseCase 有单元测试
- 至少一条 Room 或 DataStore 持久化链路被验证
- 至少一条表单或打卡交互有 UI 测试

### Slice C / D

- 在对应切片进入实施时补充各自的详细测试矩阵

## 4. 各层最低测试要求

### Domain

- 每个关键 use case 至少有成功与失败两类测试
- 涉及日期、状态流转、连续打卡等规则必须覆盖边界值

### Data

- 每个 repository 至少覆盖：
  - 远程成功
  - 远程失败
  - 本地回退或缓存命中
- 每个 mapper 至少有一组稳定转换测试

### Presentation

- 每个复杂 ViewModel 至少覆盖：
  - 初始加载
  - 用户操作后状态变化
  - 错误恢复或重试

### UI

- 当前切片内的每个关键 feature 至少一条 happy path
- 每个关键表单至少一条输入与校验路径

## 5. 不测什么

- 纯样式细节
- 第三方库自身行为
- 低价值 getter/setter
- 无业务含义的样板代码

## 6. 测试数据策略

- 优先使用 builder/factory 构造测试对象
- 避免在测试里内联大段重复数据
- 日期、时间、时区逻辑统一走可注入时钟
- UI 测试尽量用稳定 fake repository，而不是依赖真实网络

## 7. 统一质量门槛

在声称一个阶段“完成”之前，应满足：

- 相关单元测试通过
- 新增核心页面有至少基础 UI 测试
- 构建通过
- 无阻塞级崩溃
- 文档与实际结构没有明显偏离

## 8. 首批重点测试对象

第一批实现时，优先保证这些测试：

- App 启动与导航壳 smoke test
- `CreateTaskUseCase`
- `CompleteTaskUseCase`
- `CheckInHabitUseCase`
- `TaskListViewModel`
- `TaskEditorViewModel`
- `HabitViewModel`
