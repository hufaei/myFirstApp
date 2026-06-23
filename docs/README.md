# LifeLab Documentation

本文档目录按“长期稳定的项目基线”和“按切片推进的实施文档”两类组织，避免一份大文档同时承担章程、设计和执行三种职责。

## 长期基线文档

### 00-roadmap

- [product-vision.md](00-roadmap/product-vision.md): 产品定位、学习目标、成功标准
- [scope-and-phases.md](00-roadmap/scope-and-phases.md): 总体范围边界、阶段与阶段目标
- [delivery-strategy.md](00-roadmap/delivery-strategy.md): 垂直切片顺序、每个切片的退出条件

### 01-architecture

- [architecture-overview.md](01-architecture/architecture-overview.md): 整体架构、交付方式、依赖方向
- [module-boundaries.md](01-architecture/module-boundaries.md): `app/core/feature` 的职责边界与当前边界约束方式

### 02-domain

- [domain-model.md](02-domain/domain-model.md): 稳定统一语言、首个切片的领域基线、后续切片的建模原则

### 05-quality

- [testing-strategy.md](05-quality/testing-strategy.md): 测试分层、覆盖策略、质量门槛

### 06-engineering

- [dev-commands.md](06-engineering/dev-commands.md): Gradle、adb、调试、构建、测试常用命令
- [coding-standards.md](06-engineering/coding-standards.md): Kotlin、Compose、架构、命名、测试规范

## 已确认的设计文档

- [2026-06-23-lifelab-project-design.md](superpowers/specs/2026-06-23-lifelab-project-design.md): 当前项目章程与交付策略总纲

## 后续会按切片新增的设计文档

这些文档只有在对应切片进入实施前才会创建，避免提前写伪精确设计：

- `docs/superpowers/specs/<date>-platform-baseline-design.md`
- `docs/superpowers/specs/<date>-productivity-slice-design.md`
- `docs/superpowers/specs/<date>-content-discovery-slice-design.md`
- `docs/superpowers/specs/<date>-account-notifications-slice-design.md`
- `docs/superpowers/plans/<date>-<slice>.md`

## 使用顺序

推荐按这个顺序阅读：

1. `README.md`
2. `docs/00-roadmap/*`
3. `docs/01-architecture/*`
4. `docs/02-domain/domain-model.md`
5. `docs/05-quality/testing-strategy.md`
6. `docs/06-engineering/*`
7. `docs/superpowers/specs/*`
