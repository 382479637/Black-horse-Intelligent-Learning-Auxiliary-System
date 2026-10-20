# Component 1.2 设计工件索引

本目录保存 Project 1 的软件设计文档与 PlantUML 图源。需求基线见 [`docs/requirements/requirements-analysis.md`](../requirements/requirements-analysis.md) v0.4。

## 目录结构

```text
docs/design/
├── README.md                          # 本索引
├── architecture/
│   ├── architecture-design.md         # 3.2 架构设计
│   ├── architecture.puml
│   └── architecture.png
├── use-case/
│   ├── use-case-model.md              # 3.1 用例模型
│   ├── use-case.puml
│   └── use-case.png
├── iterations/
│   ├── iteration-1/
│   │   ├── class-diagram-iteration-1.md
│   │   ├── class-diagram-iteration-1-draft.md
│   │   └── diagrams/
│   │       ├── class-*-iteration-1.puml
│   │       └── png/class-*-iteration-1.png
│   └── iteration-2/
│       ├── class-diagram-iteration-2-final.md
│       └── diagrams/
│           ├── class-*-iteration-2.puml
│           └── png/class-*-iteration-2.png
├── analysis/
│   ├── genai-design-analysis.md       # 3.4 GenAI 分析
│   ├── design-evolution.md            # 3.6 设计演进
│   └── implementation-deviation-record.md  # Component 1.3 实现偏差记录
└── archive/
    └── class-service-persistence-iteration-1.puml
```

## 文档

| 文档 | 内容 | 对应报告 | 状态 |
|---|---|---|---|
| [`architecture/architecture-design.md`](architecture/architecture-design.md) | 四层架构、包结构、依赖、持久化、OOP/SOLID、设计模式 | 3.2、3.3/3.5 支撑 | v0.5，已冻结基线 |
| [`use-case/use-case-model.md`](use-case/use-case-model.md) | Actor、系统边界、用例目录、关键用例叙述、追溯 | 3.1 | v0.2，已冻结（2026-10-20） |
| [`iterations/iteration-1/class-diagram-iteration-1-draft.md`](iterations/iteration-1/class-diagram-iteration-1-draft.md) | 第 1 轮类图讨论稿（AI 辅助，保留为讨论记录） | — | v0.2 历史记录 |
| [`iterations/iteration-1/class-diagram-iteration-1.md`](iterations/iteration-1/class-diagram-iteration-1.md) | 正式第 1 轮类图 | 3.3 | v1.0，已冻结 |
| [`iterations/iteration-2/class-diagram-iteration-2-final.md`](iterations/iteration-2/class-diagram-iteration-2-final.md) | 第 2 轮最终类图 | 3.5 | v1.2，已冻结（2026-10-20） |
| [`analysis/genai-design-analysis.md`](analysis/genai-design-analysis.md) | GenAI 建议与采纳/拒绝记录 | 3.4 | v1.1 分析完成 |
| [`analysis/design-evolution.md`](analysis/design-evolution.md) | 两轮演进说明 | 3.6 | v1.2，含冻结记录 |
| [`analysis/implementation-deviation-record.md`](analysis/implementation-deviation-record.md) | Component 1.3 实现与冻结设计的偏差、处理与原因 | 实现记录 | v1.0（2026-10-20） |

> `3.7 Critical Reflection` 由学生本人撰写，本目录不提供代写文本。

## 图源与导出图

| 文件 | 导出 PNG | 内容 |
|---|---|---|
| [`architecture/architecture.puml`](architecture/architecture.puml) | [`architecture/architecture.png`](architecture/architecture.png) | 分层架构图（v0.5） |
| [`use-case/use-case.puml`](use-case/use-case.puml) | [`use-case/use-case.png`](use-case/use-case.png) | 用例图（v0.2） |
| [`iterations/iteration-1/diagrams/class-domain-iteration-1.puml`](iterations/iteration-1/diagrams/class-domain-iteration-1.puml) | [`png/class-domain-iteration-1.png`](iterations/iteration-1/diagrams/png/class-domain-iteration-1.png) | 第 1 轮领域模型 |
| [`iterations/iteration-1/diagrams/class-service-iteration-1.puml`](iterations/iteration-1/diagrams/class-service-iteration-1.puml) | [`png/class-service-iteration-1.png`](iterations/iteration-1/diagrams/png/class-service-iteration-1.png) | 第 1 轮服务、会话/权限 |
| [`iterations/iteration-1/diagrams/class-dto-iteration-1.puml`](iterations/iteration-1/diagrams/class-dto-iteration-1.puml) | [`png/class-dto-iteration-1.png`](iterations/iteration-1/diagrams/png/class-dto-iteration-1.png) | 第 1 轮统计与预警 DTO |
| [`iterations/iteration-1/diagrams/class-persistence-iteration-1.puml`](iterations/iteration-1/diagrams/class-persistence-iteration-1.puml) | [`png/class-persistence-iteration-1.png`](iterations/iteration-1/diagrams/png/class-persistence-iteration-1.png) | 第 1 轮持久化与工具 |
| [`iterations/iteration-1/diagrams/class-ui-iteration-1.puml`](iterations/iteration-1/diagrams/class-ui-iteration-1.puml) | [`png/class-ui-iteration-1.png`](iterations/iteration-1/diagrams/png/class-ui-iteration-1.png) | 第 1 轮 CUI |
| [`iterations/iteration-2/diagrams/class-domain-iteration-2.puml`](iterations/iteration-2/diagrams/class-domain-iteration-2.puml) | [`png/class-domain-iteration-2.png`](iterations/iteration-2/diagrams/png/class-domain-iteration-2.png) | 第 2 轮领域模型 |
| [`iterations/iteration-2/diagrams/class-service-iteration-2.puml`](iterations/iteration-2/diagrams/class-service-iteration-2.puml) | [`png/class-service-iteration-2.png`](iterations/iteration-2/diagrams/png/class-service-iteration-2.png) | 第 2 轮服务、会话/权限 |
| [`iterations/iteration-2/diagrams/class-dto-iteration-2.puml`](iterations/iteration-2/diagrams/class-dto-iteration-2.puml) | [`png/class-dto-iteration-2.png`](iterations/iteration-2/diagrams/png/class-dto-iteration-2.png) | 第 2 轮统计与预警 DTO |
| [`iterations/iteration-2/diagrams/class-persistence-iteration-2.puml`](iterations/iteration-2/diagrams/class-persistence-iteration-2.puml) | [`png/class-persistence-iteration-2.png`](iterations/iteration-2/diagrams/png/class-persistence-iteration-2.png) | 第 2 轮持久化与工具 |
| [`iterations/iteration-2/diagrams/class-ui-iteration-2.puml`](iterations/iteration-2/diagrams/class-ui-iteration-2.puml) | [`png/class-ui-iteration-2.png`](iterations/iteration-2/diagrams/png/class-ui-iteration-2.png) | 第 2 轮 CUI |

> 服务、DTO、持久化原为一张合并图，因连线交叉严重已拆分；旧文件保存在 [`archive/`](archive/)，仅作历史参考，不再引用。
>
> PNG 与 `.puml` 不一致时以 `.puml` 为准并重新导出。

## 渲染方式

### PlantUML（推荐）

- VS Code 安装 PlantUML 扩展，打开 `.puml` 文件预览/导出 PNG/SVG。
- 或使用 `plantuml.jar`：

```bash
# 迭代图：-o 相对于每个源文件所在目录，输出到各自的 diagrams/png/
java -jar plantuml.jar -tpng -o png docs/design/iterations/iteration-1/diagrams/*.puml
java -jar plantuml.jar -tpng -o png docs/design/iterations/iteration-2/diagrams/*.puml

# 架构图与用例图：PNG 与源文件放在同一目录
java -jar plantuml.jar -tpng docs/design/architecture/architecture.puml
java -jar plantuml.jar -tpng docs/design/use-case/use-case.puml
```

报告模板要求插入 PNG/JPG，建议导出 PNG（至少 2x 缩放；PlantUML 可用 `-scale 2` 或在图中设置 `skinparam dpi`）。

### PowerDesigner

PlantUML 源不能直接打开为 PowerDesigner 模型。可选路径：

1. 按本目录的类目录/用例目录在 PowerDesigner 中重建模型（推荐，便于人工确认与调整）。
2. 尝试 PlantUML 的 XMI 导出后导入 PowerDesigner，再人工校对关系、多重性和中文标签；不同版本兼容性不保证。
3. 保留 PlantUML 作为版本化图源，PowerDesigner 只作为最终排版/出图工具；两者不一致时以 PlantUML 和最终类图文档为准，并在提交前统一。

## 迭代流程

1. 架构与用例模型已评审并冻结：`architecture/architecture-design.md`、`use-case/use-case-model.md`。
2. 正式 Iteration 1 已固化到 `iterations/iteration-1/class-diagram-iteration-1.md`，冻结不再修改。
3. GenAI 批判已完成并记录到 `analysis/genai-design-analysis.md`（3.4）。
4. Iteration 2 已产出到 `iterations/iteration-2/class-diagram-iteration-2-final.md`（3.5）；两轮复审补正 `Employee.email` 的读取/更新契约，2026-10-20 已由学生确认并冻结。
5. 演进说明已写入 `analysis/design-evolution.md`（3.6）。
6. 学生本人撰写 3.7 Critical Reflection。
7. 进入 Component 1.3；实现与最终类图保持一致。

## 命名约定

- `iteration-1-draft`：AI 辅助讨论稿，不作为正式初版。
- `iteration-1`：学生确认/重画后的正式初版，冻结后不再改；后续修订进入 iteration 2。
- `iteration-2-final`：最终设计，与代码一一对应。
- 图源中的标题和文档头必须标明版本/轮次，避免报告里无法区分两轮。

## 关于 `codex_review/`

`codex_review/` 中的评审报告是外部评审记录，保留原始文本，不随本次目录整理改写。报告中出现的 `docs/design/...` 路径按整理前的旧布局书写，对照关系以本 README 的目录结构为准。
