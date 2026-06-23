# Architecture Overview

## 1. 结论

LifeLab 采用 `Modular Monolith + Vertical Slice Delivery + feature-first package structure` 的整体策略。

这意味着：

- 实现先维持在单 `app` module 内，避免过早进入多 module 构建复杂度
- 交付按切片推进，而不是按整站总蓝图一次性铺开
- 每个 feature 内部再用 `Clean Architecture + 务实 DDD` 组织业务和数据边界
- 当包边界稳定且复杂度确实需要时，再升级为 Gradle 级模块化

## 2. 为什么不是其他方案

### 一份总设计覆盖全 app 的实现方式

文档可以保留总纲，但实现不能直接按一份全量蓝图推进。否则容易出现：

- 实施计划过大且不可验证
- 早期假设长期得不到反馈
- 文档会写出大量未经验证的细节

### 从第一天就多 module + 重型 Clean Architecture

优点是编译期边界更强，但对当前项目过重，会带来：

- Gradle 配置和依赖复杂度大幅上升
- 很多模块边界在真实功能出现前都只是推测
- 学习主线会被工程样板稀释

### 纯页面驱动 MVVM

优点是快，但在功能增多后容易出现：

- ViewModel 过胖
- 页面直接拼接业务规则
- 数据模型混用
- 测试边界不清

### 选择的折中方案

因此本项目采用：

- 简单页面：`UI -> ViewModel -> Repository`
- 中高复杂业务：`UI -> ViewModel -> UseCase -> Repository`
- 数据获取统一经过 repository
- DTO、本地实体、领域模型、UI 模型按需要拆分，不强制每处都重复映射
- 当前以垂直切片为实施单位，而不是以技术层为实施单位

## 3. 逻辑分层

### Presentation

- Compose 页面
- 公共 UI 组件
- Navigation
- ViewModel
- UiState / UiEvent / UiEffect

### Domain

- 领域模型
- 值对象
- 关键业务规则
- UseCase
- Repository 接口

### Data

- Repository 实现
- Remote DataSource
- Local DataSource
- DTO / Entity
- Mapper

### Core

- 网络、数据库、DataStore
- 日志与错误模型
- 公共 Result 类型
- 公共 UI 与工具函数

## 4. 依赖方向

依赖只允许向内收敛：

- Presentation 依赖 Domain 和 Core
- Data 依赖 Domain 和 Core
- Domain 只依赖少量无业务语义的 Core 能力
- Core 不依赖 feature

禁止：

- UI 直接访问 Retrofit/Room
- feature 之间直接互相读写内部实现
- data 层反向依赖页面层

## 5. 当前边界约束方式

在单 `app` module 阶段，边界还不是编译期强约束，而是依靠以下手段维持：

- 包结构
- 命名规范
- 代码评审
- 测试边界
- 后续静态检查

这是一种刻意选择。先验证边界是否合理，再决定是否升级到 Gradle 多 module。

## 6. 状态与数据流

统一采用状态驱动 UI：

1. UI 触发 `UiEvent`
2. ViewModel 处理事件并调用 UseCase 或 Repository
3. Domain/Data 返回明确结果
4. ViewModel 产出新的 `UiState`
5. UI 根据状态渲染
6. 一次性消息通过 `UiEffect` 下发

## 7. 工程落地策略

- 先完成 `Platform Baseline`
- 再用 `Tasks + Habits` 验证首条完整业务链路
- 对已验证的共享能力再上提到 `core`
- 只有在构建速度、团队协作或边界污染明显时，再升级为多 module
