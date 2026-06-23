# Module Boundaries

## 1. 目标结构

当前仓库后续将迁移为 Android 工程，目标代码结构如下：

```text
app/
  src/main/java/com/example/lifelab/
    app/
    core/
    feature/
```

在单 `app` module 阶段，逻辑边界通过包结构严格执行。

## 2. 当前现实约束

现阶段的边界不是编译期模块边界，而是工程约束边界：

- 包结构必须稳定
- feature 目录不得直接偷用其他 feature 的内部实现
- 共享代码只有在出现第二个明确消费者后才上提到 `core`
- 通过测试和 review 约束跨边界访问

这意味着当前边界足够正式，但仍是“先验证，再固化”的策略。

## 3. 顶层包职责

### `app`

职责：

- `Application`
- `MainActivity`
- 根导航图
- 全局初始化
- App 级装配

不负责：

- 具体业务规则
- 页面内部数据编排

### `core`

职责：

- `common`: 常量、Result、错误类型、扩展函数
- `ui`: 主题、公共组件、状态页、设计 token
- `network`: Http client、API 定义、拦截器
- `database`: Room 数据库、DAO、本地实体
- `datastore`: 偏好、token、轻量配置
- `model`: 跨 feature 通用模型
- `testing`: 测试 fake、规则、辅助工具

不负责：

- 某个具体 feature 的业务规则

### `feature`

每个一级功能一个独立包：

- `home`
- `tasks`
- `habits`
- `discover`
- `search`
- `notifications`
- `profile`

每个 feature 内部建议结构：

```text
feature/tasks/
  data/
  domain/
  presentation/
```

职责：

- 自己的页面、状态模型、ViewModel
- 自己的 use case
- 自己的 repository 接口与实现细节
- 自己的数据映射

## 4. feature 间交互规则

- 允许通过导航参数和公共 domain model 交流
- 不允许直接依赖其他 feature 的内部 data 或 presentation 实现
- 跨模块复用逻辑优先下沉到 `core`
- 若只是偶发共享，不急于抽公共层，避免过早抽象
- 共享 UI 或工具代码至少在两个真实场景复用后再提升

## 5. 何时引入真正的多 module

满足以下任一条件时再考虑：

- feature 明显独立且变更频繁
- 构建时间成为问题
- 测试隔离需求明显
- 包边界已经稳定，不会频繁重构

在此之前，单 module + 严格边界更适合当前项目阶段。
