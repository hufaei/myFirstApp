# LifeLab UI/UX Redesign Design

## 1. Purpose

当前 `LifeLab` 已具备完整的业务演示骨架，但 UI/UX 仍停留在“默认 Compose Material 3 组件堆叠”的阶段，主要问题有：

- 底部导航承载 `7` 个一级入口，结构拥挤，难以形成稳定心智
- 各页面几乎都由标题 + 卡片列表组成，视觉层级过平，用户很难快速判断“现在最重要的事”
- 工具型页面、内容型页面、状态页、设置页没有明确区分，交互气质混杂
- 主题、字体、图标、色彩缺少统一品牌语言，整体不像一个完成度高的正式产品

本切片的目标是对整个 app 进行一次正式项目级别的 UI/UX 重构，在不推翻既有领域边界和模块结构的前提下，建立可持续扩展的设计系统与信息架构。

## 2. Scope

本切片包含：

- 应用级信息架构重组
- 根导航与 `App Shell` 重做
- 主题、色彩、字体、形体、间距、状态样式统一
- `Today/Home`、`Workbench`、`Discover`、`Search`、`Notifications`、`Profile` 全量视觉与交互改造
- 现有 `Tasks`、`Habits` 模块在新工作台结构下的页面重组
- 空态、错态、加载态、提示态的统一表达
- Android app icon 设计 brief

本切片不包含：

- 新增后端接口、数据库 schema、同步机制
- 改写领域模型、use case、repository 边界
- 新增当前 roadmap 外的业务模块
- 复杂手势系统、Widget、Wear OS、平板专属布局

## 3. Product Direction

### 3.1 Brand Thesis

`LifeLab` 的品牌方向定义为：

`温和成长 x 实验系统`

它既不是纯效率工具，也不是纯内容流产品，而是一个帮助用户“有节奏地管理生活与成长实验”的个人操作台。

### 3.2 Audience

目标用户：

- 有长期自驱习惯的个人用户
- 同时需要任务管理、习惯追踪、内容探索与个人偏好控制
- 希望产品既专业可靠，又不要像企业后台一样生硬

### 3.3 Single Job

用户每天打开 app 后，应在 `10` 秒内知道：

1. 今天最该关注什么
2. 当前进展到哪
3. 下一步从哪个入口继续

### 3.4 Tone

界面文案与交互语气遵循：

- 直白，不营销
- 温和，但不幼态
- 专业，不冷漠
- 帮用户做决定，不堆信息

## 4. Information Architecture

### 4.1 Top-Level Navigation

底部一级导航从 `7` 个压缩为 `4` 个：

- `Today`
- `Workbench`
- `Discover`
- `Me`

压缩后结构如下：

| 旧入口 | 新归属 |
| --- | --- |
| `Home` | `Today` |
| `Tasks` | `Workbench` |
| `Habits` | `Workbench` |
| `Discover` | `Discover` |
| `Search` | 顶部全局动作 |
| `Notifications` | 顶部全局动作 |
| `Profile` | `Me` |

### 4.2 Navigation Rules

- 底部导航只保留“长期稳定、用户高频切换”的入口
- `Search` 不再占据一级 tab，而是作为全局入口出现在 `Today`、`Workbench`、`Discover` 顶栏
- `Notifications` 由全局 bell 入口进入 inbox 页面
- `Tasks` 与 `Habits` 不再并列占据底部，而是在 `Workbench` 内通过分段切换承载
- 深层页面继续保留独立 route，例如任务详情、任务编辑、通知详情、搜索结果等

### 4.3 Structural Principle

信息架构遵循：

- 首页负责“定方向”
- 工作台负责“做事情”
- 发现页负责“找内容”
- 个人页负责“管偏好”

任何页面都必须让用户在首屏明确知道它属于哪一种页面类型，避免当前“所有页面看起来都像普通卡片列表”的问题。

## 5. Visual System

### 5.1 Color Tokens

主视觉采用双色撞色体系，以 `晴空蓝 + 雪雾白` 为核心，强调清爽、克制、实验感。

- `Sky 500` `#66B8F4`
- `Sky 700` `#2C7FB8`
- `Snow Mist` `#F7FBFF`
- `Cloud Panel` `#EAF4FB`
- `Ink Blue` `#173247`
- `Fog Line` `#C9DCEB`

使用原则：

- `Sky 500` 用于品牌动作、焦点、关键进度强调
- `Sky 700` 用于深层强调、选中态和标题辅助
- `Snow Mist` 作为全局背景，避免纯白的生硬感
- `Cloud Panel` 用于次级容器、分区卡和轻状态背景
- `Ink Blue` 作为主文本和结构主色，减少纯黑对比过硬
- `Fog Line` 作为边界线、分隔线和弱状态边框

禁止事项：

- 不引入第三主色作为品牌色
- 不用大面积炫技渐变覆盖页面背景
- 不把状态色和品牌色混用

### 5.2 Typography

字体采用三层角色：

- `Display`: `Sora`
- `Body`: `Noto Sans SC`
- `Utility`: `JetBrains Mono`

使用原则：

- `Sora` 只用于首页关键标题、摘要数字、少量 section heading
- `Noto Sans SC` 承担绝大多数中文内容与控件文本
- `JetBrains Mono` 用于日期、计数、时间、标签辅助信息

实现约束：

- 字体以本地 `font` 资源打包，避免依赖在线 provider
- 如果首轮未补齐全部字体文件，必须提供明确 fallback，而不是回退到默认系统主题后不再补齐

### 5.3 Shape And Surfaces

- 页面主卡片圆角：`20dp`
- 次级卡片圆角：`16dp`
- 输入控件圆角：`14dp`
- 芯片类控件圆角：`12dp`
- 底部导航与顶栏容器采用更平直的轮廓，不做夸张胶囊

表面语言：

- 减少“所有东西都像悬浮卡片”的问题
- 更多使用轻边框、弱背景、局部分组来表达结构
- 层级靠间距、字体、边界和少量品牌强调建立，而不是靠大量阴影

### 5.4 Signature Element

全 app 的记忆点定义为 `Daily Pulse`。

它是一条横向“日脉带”，在首页头部和若干关键摘要区复用，用来串联：

- 今日任务
- 习惯进度
- 提醒状态
- 内容建议

`Daily Pulse` 不是装饰条，而是一个可读的状态组织器。它必须真实承载信息，不能退化为单纯渐变背景。

## 6. Screen-Level Design

### 6.1 App Shell

`App Shell` 统一改造为：

- 顶部：品牌标题 + 场景副标题 + 全局动作
- 中部：当前一级页面内容
- 底部：`4` 项一级导航

要求：

- 底部导航必须使用真实图标，而不是当前的首字母占位
- 顶栏动作在不同页面保持位置稳定
- 页面内容与系统栏、底栏之间的安全区处理一致
- 当前页面要有足够强的标题与副标题，不允许只显示一个平淡标题

### 6.2 Today

`Today` 不再是普通 feed，而是“当日中枢”：

- 首屏先展示欢迎语和 `Daily Pulse`
- 第二层给出 `Today Focus`，包含今天最关键的任务与习惯
- 第三层给出跨模块摘要，如进度、提醒、连续记录、未读消息
- 第四层才进入轻内容流，承接发现页内容预览

首屏必须先回答“今天要做什么”，而不是先堆推荐卡片。

### 6.3 Workbench

`Workbench` 是工具密度最高的一级页，用于统一承载 `Tasks` 与 `Habits`。

结构：

- 顶部摘要条：当前任务数、今日完成、活跃习惯、最长 streak
- 分段切换：`Tasks | Habits`
- 筛选与排序控件
- 列表内容
- 明确的主要动作，如 `New task`

`Tasks` 设计原则：

- 列表项信息层级清楚：标题、说明、状态、优先级、截止时间
- 详情页与编辑页使用同一套语言，不再像独立样式
- 编辑器要更像正式表单，而不是默认字段堆叠

`Habits` 设计原则：

- 将签到、提醒、streak 作为高优先级信息
- `Check in` 是主动作，提醒设置是次动作
- 统计卡不能只是一排裸数字，要通过层级与文案建立意义

### 6.4 Discover

`Discover` 继续保留内容流属性，但要从“数据列表”转成“编辑精选”感：

- 分类筛选保留，但增强标题区与导语
- 内容卡使用更明确的内容类型、价值摘要、下一步动作
- 文章、课程、权益三类卡片必须有同一设计骨架，但能看出类别差异

### 6.5 Search

`Search` 升级为全局搜索页：

- 搜索输入区要成为首屏焦点
- 热词、历史、结果分组必须区分层级
- 结果卡要显示类型、标题、摘要和跳转方向
- 空结果、加载、错误都要具备明确引导动作

### 6.6 Notifications

`Notifications` 拆成两个逻辑层：

- inbox：未读、已读、归档消息
- settings：系统通知与站内消息偏好

默认进入 inbox，再从页面内进入设置，而不是像现在一样把消息列表和系统偏好生硬堆在一起。

### 6.7 Me

`Profile` 改造成 `Me` 页面：

- 顶部为账号身份与个人摘要
- 中部为偏好控制台
- 下部为兴趣、默认行为、系统偏好等次级内容

它应更像“我的控制面板”，而不是普通设置页。

## 7. Shared Component Rules

应新增或收敛的共享组件包括：

- `LifeLabTopBar`
- `LifeLabBottomBar`
- `PulseCard`
- `SectionHeader`
- `SummaryMetric`
- `StatePanel`
- `FilterRow`
- `WorkbenchSegment`
- `ActionCard`

原则：

- 同类信息必须复用同一组件骨架
- 不允许每个页面都各写一套消息卡、空态卡、标题行
- 共享组件放在 `core/ui`，但不把业务文案和 feature 私有结构上提到 `core`

## 8. Content And State Guidelines

### 8.1 Copywriting

文案要求：

- 使用用户可理解的动作词
- 不使用内部实现术语
- 按钮、toast、空态文案前后一致
- 错误文案说明发生了什么，以及用户接下来能做什么

示例方向：

- 用 `Save task`，不用 `Submit`
- 用 `Try another filter`，不用 `No data`
- 用 `Mark as read`，不用 `Handle`

### 8.2 State Design

所有页面统一具备以下状态策略：

- `Loading`: 以结构化骨架或轻进度提示呈现，不用孤立 spinner 作为唯一表达
- `Empty`: 说明当前为空的原因，并给下一步建议
- `Error`: 说明失败对象，并给可恢复动作
- `Success feedback`: 用统一的轻提示样式，不在各页面临时拼装 banner

## 9. Motion And Accessibility

### 9.1 Motion

动画策略：

- 页面首屏做轻量进入动效
- `Daily Pulse` 与关键摘要卡允许有轻微状态过渡
- 列表项 hover/press/selection 状态明确
- 遵循 `reduced motion` 偏好，可降级为无位移动画

禁止：

- 为每张卡片添加无意义浮动动效
- 在一个页面中叠加多种竞争性动画

### 9.2 Accessibility

必须满足：

- 文字与背景对比达标
- 可点击区域不小于 `48dp`
- 结构顺序与 TalkBack 语义合理
- 状态变化不只靠颜色表达
- 字体放大后核心流程不破版

## 10. Implementation Constraints

本次 UI/UX 改造不改变既有 Clean Architecture 风格的模块边界。

继续保持：

- `app/` 负责装配、导航、壳层
- `core/ui/` 负责主题和共享组件
- `feature/*/presentation` 负责各自页面组合
- 领域模型、use case、repository 保持原边界

允许的结构调整：

- 把 `Tasks`、`Habits` 在壳层整合到 `Workbench`
- 把 `Search`、`Notifications` 从一级 tab 降为全局动作入口
- 新增 `core/ui/component`、`core/ui/icon`、`core/ui/token` 等目录

## 11. Testing Strategy

本切片重点验证：

- 顶层导航结构是否符合新的 `4` tab 设计
- 页面关键节点是否具备可测试语义
- `Today`、`Workbench`、`Discover`、`Me` 至少具备基础 smoke test
- 共享组件的状态切换逻辑具备单元或 Compose UI 测试

同时要求：

- 不以主观截图满意度代替测试
- 视觉改造后，现有 ViewModel 与 domain 测试不得回退
- 如本机无法完成设备验证，需明确说明阻塞点

## 12. Acceptance

本切片完成时应满足：

- 底部一级导航已压缩为 `4` 项
- 顶部全局动作已承载搜索与通知
- 全 app 页面已统一进入同一视觉系统
- `Today`、`Workbench`、`Discover`、`Me` 的页面类型差异清楚
- `Tasks` 与 `Habits` 已在交互上整合为工作台体验
- 空态、错态、加载态不再是默认组件临时拼装
- 品牌色、字体、图标和形体规则已经落地
- app icon brief 已准备完成，资源到位后可直接接入 adaptive icon

## 13. App Icon Brief

图标方向：

- 主题：`温和成长 x 实验系统`
- 主形态：圆角实验容器 / 地平线容器
- 识别元素：中心一条稳定的水平“液面 / 地平线”
- 避免：字母 `L`、卡通烧瓶、复杂拟物、过度渐变

色彩要求：

- 主色：`#66B8F4`
- 背景：`#F7FBFF`
- 深层辅助：`#2C7FB8`

交付要求：

- `1024x1024` 主图
- 前景透明版
- 单色 themed icon 版
- 前景与背景分层，便于生成 Android adaptive icon

## 14. Risks

主要风险包括：

- 一次性重做全 app 容易在实现期造成样式分叉
- 自定义字体接入如果执行粗糙，会引入不一致 fallback
- 导航重组若只改视觉不改路径说明，用户仍会迷失

对应策略：

- 先完成壳层、主题、共享组件，再改各 feature 页面
- 字体接入采用资源打包，不使用在线依赖
- 页面标题、副标题和分区说明同时重写，确保结构可读
