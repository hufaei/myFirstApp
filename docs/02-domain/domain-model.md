# Domain Model

## 1. 文档范围

本文档只固定两类内容：

- 长期稳定的统一语言
- 首个核心切片 `Tasks + Habits` 的领域基线

后续 `Home / Discover / Search / Profile / Notifications` 的详细领域字段和交互规则，不在这里做伪精确展开，而在对应切片设计中单独定义。

## 2. 统一语言

为避免同一概念在不同层次被不同命名污染，先固定核心术语：

- `User`: 当前登录用户
- `Task`: 一项可完成、可延期、可归档的任务
- `Habit`: 一项可重复打卡的习惯
- `FeedItem`: 首页或发现页展示的内容项
- `Article`: 可阅读内容
- `Offer`: 课程、会员或商品类内容
- `Message`: 站内消息
- `Reminder`: 本地提醒配置
- `SearchQuery`: 搜索输入
- `UserPreference`: 用户偏好和设置

## 3. 领域边界

本项目不是完整企业 DDD 示例，不做复杂上下文映射。但仍保留以下 bounded context 思路：

- `Task Management`
- `Habit Tracking`
- `Content Discovery`
- `Identity And Preferences`
- `Messaging And Notifications`

每个 context 先在 feature 内部闭环，再通过导航和少量共享模型连接。

## 4. 当前已固定的核心模型

### Task

关键属性：

- `id`
- `title`
- `description`
- `status`
- `priority`
- `dueAt`
- `tags`
- `createdAt`
- `updatedAt`

关键规则：

- 已归档任务不可直接编辑内容
- 已完成任务允许恢复为进行中
- 截止时间为空表示非时限任务

### Habit

关键属性：

- `id`
- `name`
- `frequency`
- `streakCount`
- `lastCheckInDate`
- `reminder`

关键规则：

- 同一自然日只允许一次有效打卡
- 连续天数由打卡记录推导，而不是任意写入

### FeedItem

它是首页和发现页的聚合展示模型，但当前阶段只固定它的角色，不固定详细字段。后续内容切片会单独定义其结构。

它可能承载：

- `ArticleCard`
- `OfferCard`
- `TaskSuggestionCard`
- `HabitInsightCard`

因此展示层建议使用 `sealed interface` 表达不同卡片类型。

### UserPreference

关键属性：

- `themeMode`
- `notificationEnabled`
- `defaultTaskFilter`
- `contentInterestTags`

适合存储在 DataStore，而不是 Room。

### Article / Offer / Message / SearchQuery

这些模型已经确认会存在，但详细字段和状态约束留给对应切片设计，当前阶段只保留命名和角色边界。

## 5. use case 粒度原则

不是所有操作都强制单独抽 use case。规则如下：

### 必须 use case 化

- 有业务规则编排
- 涉及多个 repository 协作
- 需要复用
- 需要重点测试

例如：

- `CreateTaskUseCase`
- `CompleteTaskUseCase`
- `CheckInHabitUseCase`

### 可直接由 ViewModel 调用 repository

- 简单查询
- 纯读取设置
- 明显一次性的小操作

后续内容切片可能新增：

- `BuildHomeFeedUseCase`
- `SearchContentUseCase`

## 6. 状态模型建议

页面状态统一遵守：

- `UiState`: 可重复订阅的稳定状态
- `UiEvent`: 来自用户输入的动作
- `UiEffect`: Toast、导航、Snackbar 等一次性事件

推荐用 `sealed interface` 或 `data class` 组合表达状态，而不是用多个散乱布尔值拼凑。

## 7. 模型映射原则

- `DTO`: 面向接口协议
- `Entity`: 面向 Room 持久化
- `Domain Model`: 面向业务规则
- `UiModel`: 面向页面展示

不要求所有场景机械地四层映射，但禁止直接把网络 DTO 暴露到 Compose 页面。
