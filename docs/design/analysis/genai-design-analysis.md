# GenAI 分析 — 正式 Iteration 1

| 项目 | 内容 |
|---|---|
| 文档版本 | v1.1 |
| 日期 | 2026-10-20 |
| 课程 | COMP603/ENSE600 Program Design & Construction |
| 作业 | Project 1（40%），Component 1.2 Software Design |
| 对应报告 | 3.4 GenAI analysis of Iteration 1 |
| 分析对象 | [`class-diagram-iteration-1.md`](../iterations/iteration-1/class-diagram-iteration-1.md) v1.0（已冻结） |
| 评审输入 | [`codex_review/design-review.md`](../../../codex_review/design-review.md) |
| 需求基线 | `docs/requirements/requirements-analysis.md` v0.4 |
| 输出 | Iteration 2 修订项，见 [`class-diagram-iteration-2-final.md`](../iterations/iteration-2/class-diagram-iteration-2-final.md) |
| 状态 | 分析完成；采纳项已纳入 Iteration 2（v1.2）并随设计基线冻结（2026-10-20） |

> 本文档记录对 GenAI 评审建议的逐条判断，不包含学生自撰的设计反思（3.7）。最终设计取舍由学生确认；本文档不替代 3.7。

---

## 1. 分析范围与方法

### 1.1 输入

本次分析的对象是正式 Iteration 1 的类图文档与五张 PlantUML 图。评审输入是 Codex 生成的 `design-review.md`，其中包含 4 个高风险问题、4 个中等风险问题和 4 个低风险改进项。

### 1.2 判断标准

逐条按以下顺序判断，而不是全部照单接受：

1. **需求一致性**：是否违反 `requirements-analysis.md` v0.4 的 FR、唯一性约束或验收标准。
2. **UML 语义**：关系类型、多重性、组合/关联的使用是否符合 UML 定义。
3. **可实现性**：在 Java 21/25、无数据库、CSV 持久化的约束下是否能直接实现。
4. **职责与耦合**：修改是否降低耦合、减少上帝类，且不引入不必要的抽象。
5. **两轮可追溯**：每条采纳项必须能说明“第 1 轮是什么、第 2 轮改成什么、为什么”。

### 1.3 结论标记

| 标记 | 含义 |
|---|---|
| 采纳 | 第 1 轮确有缺陷，Iteration 2 修改 |
| 部分采纳 | 方向正确，但表述或范围需要修正 |
| 拒绝 | 经核对需求/UML 语义，第 1 轮写法成立，保留并说明理由 |
| 过程改进 | 不属于类结构错误，但补入设计过程记录或交付物 |

---

## 2. 结论摘要

| # | 来源 | 问题 | 结论 | Iteration 2 处理 |
|---|---|---|---|---|
| 1 | 高风险 1 | `Score` 同时被 `Student` 与 `ClassCourse` 组合拥有 | 采纳 | `Student` 为唯一组合拥有者；`ClassCourse—Score` 改为普通关联 |
| 2 | 高风险 2 | 多重性 `1` 导致父对象必须有子对象 | 部分采纳 | 全部多端显式写 `0..*`；但不承认“当前语义错误”的判断，见 3.2 |
| 3 | 高风险 3 | 成绩唯一键 `(studentId, courseId)` 与 `(studentId, classCourseId)` 不一致 | 采纳 | 统一为 `(studentId, courseId)`，补 `ScoreService.record`、加载校验和约束注释 |
| 4 | 高风险 4 | 教师查看课程目录没有用例落点 | 采纳 | 新增 `UC-06a 查看课程目录`，连接管理员与教师 |
| 5 | 中风险 5 | 两轮设计闭环未完成 | 采纳（过程） | 产出本文档、Iteration 2 与演进说明 |
| 6 | 中风险 6 | 独立初版证据不足 | 部分采纳 | 补 Iteration 1 确认记录；人工重画清单由学生本人补充，AI 不代写 |
| 7 | 中风险 7 | DTO 未完整表达“数据不足”和 `updatedAt` | 采纳 | 新增 `DataStatus`、可空指标、`ScoreDetail.updatedAt` |
| 8 | 中风险 8 | 关系图语义易误读 | 采纳 | 标角色名与端点说明，补文字约束 |
| 9 | 低风险 1 | 聚合型用例可能被误读为单个按钮 | 采纳（文档） | 在用例模型说明管理类用例包含查看/新增/修改/删除与异常流程 |
| 10 | 低风险 2 | `MenuFactory`、`DataInitializer`、`StatisticsService` 可能是上帝类 | 部分采纳 | 拆 `MenuFactory`；保留 `DataInitializer`、`StatisticsService` 并说明理由 |
| 11 | 低风险 3 | `Repository<T>` 未约束 `Identifiable` | 采纳 | 改为 `Repository<T extends Identifiable>`，`CsvCodec`/`CsvRepository` 同步 |
| 12 | 低风险 4 | record 的集合/空值语义未定义 | 采纳 | 集合统一 `List.copyOf`；无数据指标用可空类型 + `DataStatus` |

---

## 3. 逐条分析

### 3.1 `Score` 的双组合拥有者（高风险 1）

**评审意见：** `Student ◆── Score` 与 `ClassCourse ◆── Score` 同时使用组合，违反组合的生命周期语义；建议改为普通关联，或只保留一个组合拥有者。

**核对结果：** 采纳。UML 组合表示整体对部分的生命周期负责。当前需求中：

- 删除普通学生时，由 `StudentService` 级联删除其成绩（FR-STU-06）；
- 有成绩的 `ClassCourse` 不得移除（FR-TEACH-04）；
- `Score.classCourseId` 创建后不可变，转班不迁移历史成绩（FR-SCORE-09）。

因此 `Student` 是成绩生命周期的实际拥有者；`ClassCourse` 只是成绩必须引用的教学任务，且删除教学任务时反被成绩阻止。两个实心菱形会误导读者以为二者都能级联删除成绩。

**Iteration 2 处理：**

```text
Student "1" ◆── "0..*" Score : owns
ClassCourse "1" ──── "0..*" Score : assessed by
```

- `Student—Score` 保留组合：删除普通学生时级联删除成绩；归档保护状态下禁止删除学生，因此不会触发级联。
- `ClassCourse—Score` 改为普通关联：`TeachingService.removeCourse` 先检查成绩引用，有成绩则拒绝。
- 在类图注释中写明“唯一生命周期拥有者是 `Student`；`ClassCourse` 不是组合拥有者”。

**未采纳的替代方案：** 两个方向都改为普通关联也能表达同样的业务规则，但会丢失“成绩不能脱离学生存在”的 UML 语义。保留 `Student` 侧的单一组合更准确。

### 3.2 多重性把“可为空集合”画成必有一条（高风险 2）

**评审意见：** 类图使用 `*`，左侧为 `1`，认为每个实体必须至少有一项关联对象；建议改为 `0..*`。

**核对结果：** 部分采纳。需要区分两件事：

1. **UML 语义判断不成立。** 在 `Department "1" ─── "*" Employee` 中，`1` 位于 `Department` 端，表示“每名员工必须属于恰好一个部门”；`*` 位于 `Employee` 端，表示“一个部门可以有零到多名员工”。`*` 在 UML 中本来就是 `0..*`，空部门是允许的。同理，空班级、无成绩学生、待分配教学任务在现有写法下都已允许。
2. **可读性问题成立。** `*` 与 `1` 并排时容易被读成“至少一条”（本次评审就出现了这种误读）。把多端显式写成 `0..*` 可以消除歧义，值得改。

**Iteration 2 处理：**

- 把所有多端从 `*` 改为 `0..*`。
- 父端仍保留 `1` 或 `0..1`，因为员工的部门、成绩的学生/教学任务确实是必填引用。
- 在文档中补一句多重性阅读规则：“`1` 在父端表示子对象必须引用一个父对象；`0..*` 在子端表示父对象可以没有子对象。”

**结论：** 采纳可读性修改；拒绝“第 1 轮多重性语义错误”的判断，并在本节保留理由，避免在报告中把正确模型误写成缺陷。

### 3.3 成绩唯一性键不一致（高风险 3）

**评审意见：** 需求要求 `(studentId, courseId)` 唯一，但架构设计写成 `(studentId, classCourseId)`，会允许同一学生在同一课程的两个 `ClassCourse` 下各有一条成绩。

**核对结果：** 采纳。需求 4.1 明确 `Score` 的唯一组合是 `(studentId, courseId)`，其中 `courseId` 经 `ClassCourse` 解析；FR-STU-05 又要求转班后同课程不在新班级重复建分。第 1 轮的 `(studentId, classCourseId)` 表述确实会把课程粒度偷换成教学任务粒度。

**Iteration 2 处理：**

1. `ScoreService.record(studentId, classCourseId, score)` 先由 `classCourseId` 解析 `courseId`。
2. 按 `(studentId, courseId)` 查找已有成绩：
   - 若已有成绩属于同一 `ClassCourse`：更新分数与 `updatedAt`，不新增行。
   - 若已有成绩属于其他 `ClassCourse`（学生转班后同课程）：不新建成绩；当前用户能访问原 `ClassCourse` 时更新原记录并保留原 `classCourseId`，否则提示由原班级任课教师维护。
3. `DataLoader` 加载 `scores.csv` 时执行同一唯一性校验；重复的后续行按坏行处理并记入 `LoadDiagnostics`。
4. 在类图约束和 `architecture-design.md` 第 3.4 节使用同一表述。
5. UC-10 的异常流程改为“同一学生同一课程”，不再写“同一学生同一 ClassCourse”。

**边界说明：** 本设计不支持同一学生重修同一课程。若以后需要重修，必须增加修读/学期/教学任务粒度并修改需求，不能只把唯一键改回 `classCourseId`。

### 3.4 教师查看课程目录没有用例落点（高风险 4）

**评审意见：** FR-COURSE-05 允许教师查看课程目录，但 UC-06 只连接管理员。

**核对结果：** 采纳。这是需求追溯缺口，不是可选优化。

**Iteration 2 / 用例模型处理：**

- 新增 `UC-06a 查看课程目录`，主要 actor 为管理员、教师；只读。
- `UC-06` 保留为管理员的管理用例，覆盖 FR-COURSE-01～04。
- 用例图新增 `UC-06a`，同时连接管理员与教师。
- 菜单映射新增“教师 → 我的教学 → 课程目录”（需求 6.3 子菜单的设计细化）。
- 需求追溯改为 `FR-COURSE | UC-06、UC-06a`。
- 在用例叙述中写明：教师只能查看，不能增删改；service 仍执行权限校验，不依赖菜单隐藏。

### 3.5 两轮设计闭环未完成（中风险 5）

**评审意见：** `genai-design-analysis.md`、`class-diagram-iteration-2-final.md`、`design-evolution.md` 仍为待完成，不能满足“两轮设计”的交付要求。

**核对结果：** 采纳，属于交付完整性问题。

**处理：**

- 本文档作为 3.4 的分析记录。
- `class-diagram-iteration-2-final.md` 作为 3.5 的最终类图，包含本文档的采纳项。
- `design-evolution.md` 作为 3.6 的演进说明，逐项对照两轮差异。
- Iteration 1 保持冻结，不被覆盖；Iteration 2 另建文档与图源。

### 3.6 独立初版证据不足（中风险 6）

**评审意见：** 正式 Iteration 1 由 AI 讨论稿演化而来，但没有记录哪些内容由学生确认、哪些由学生重画。

**核对结果：** 部分采纳。第 1 轮确实是“AI 讨论稿 → 学生确认 → 正式冻结”的流程；确认项有记录，但“学生手工重画/修改清单”属于学生本人的设计过程，AI 不能替学生编造。

**处理：**

- 在 `design-evolution.md` 增加“Iteration 1 的确认记录”，只记录学生明确确认过的六项设计决策及其来源。
- “人工重画/手工调整清单”保留为由学生本人补充的条目；如果学生没有手工重画，应如实写“未重画，基于讨论稿确认”，不能虚构独立绘制过程。
- 报告 3.3 提交 Iteration 1 时，附上讨论稿与确认记录，说明两版关系。

### 3.7 DTO 未表达“数据不足”和成绩更新时间（中风险 7）

**评审意见：** `ClassStatistics`/`CourseStatistics` 依赖 `scoreCount == 0` 隐式判断数据不足；`StudentStatistics` 没有 `scoreCount`；`ScoreDetail` 没有 `updatedAt`。

**核对结果：** 采纳。隐式约定会让 UI 把合法的 0 分和“无数据”混在一起。

**Iteration 2 处理：**

- 新增枚举 `DataStatus { AVAILABLE, INSUFFICIENT }`。
- `ClassStatistics`、`CourseStatistics`、`StudentStatistics` 增加 `DataStatus dataStatus`。
- `StudentStatistics` 增加 `scoreCount`。
- 无数据时平均分、及格率、最高分、最低分改为可空 `Double`；`WarningResult.average`、`WarningResult.failedCount`、`ClassWarning.passRate`、`ClassWarning.average` 同步改为可空类型，`null` 只表示“该项不适用”，不再用 0 冒充。
- `ScoreDetail` 增加 `LocalDateTime updatedAt`，满足 FR-SCORE-07 的展示要求。
- record 的集合字段在紧凑构造器中 `List.copyOf`，禁止 `null` 列表；空集合表示“没有明细”，不是“数据不足”。
- UI 契约：`dataStatus == INSUFFICIENT` 时显示“数据不足”，不打印数值；`scoreCount == 0` 与 `dataStatus` 必须一致，由 service 保证。

### 3.8 关系图语义易误读（中风险 8）

**评审意见：** `ClassCourse 0..* ─── 0..1 Employee` 的文字解释正确，但图上应标角色名和端点方向。

**核对结果：** 采纳为可读性改进。

**Iteration 2 处理：**

- 在关联线上保留并统一角色名：`headTeacher`、`teacher`、`current class`、`offers`、`assessed by`。
- 在类图注释中写明“左端/右端分别描述哪一方的多重性”。
- 对 `ClassCourse.teacherId` 补“空值 = 待分配”的端点说明。

### 3.9 低风险 1：聚合型用例的展开

**评审意见：** “管理部门/管理课程/管理成绩”容易被评审认为只是一个按钮。

**核对结果：** 采纳为文档改进。

**处理：** 在 `use-case-model.md` 第 2.2 节增加说明：管理类用例包含查看、新增、修改、删除和异常流程；报告叙述时按 CRUD 展开。类图不变。

### 3.10 低风险 2：候选上帝类的取舍

**评审意见：** `MenuFactory`、`DataInitializer`、`StatisticsService` 需要明确取舍，而不是只留“可能成为上帝类”的备注。

**核对结果：** 部分采纳。

**处理：**

| 类 | 决定 | 理由 |
|---|---|---|
| `MenuFactory` | 拆分 | 管理员与教师菜单是两棵独立的树，且会随功能增加持续膨胀。Iteration 2 引入 `MenuFactory` 接口 + `AdminMenuFactory` / `TeacherMenuFactory`，由组合根选择实现。 |
| `DataInitializer` | 保留 | 它负责一次性的启动编排：目录/文件/表头、全新初始化、管理员兜底。这些阶段共享同一组仓储和初始化判定，拆成多个类会增加传递依赖，但不减少业务复杂度。 |
| `StatisticsService` | 保留 | 班级、课程、学生统计共用同一套可见范围过滤和成绩聚合；拆分类似方法会重复口径，反而增加不一致风险。若以后统计规则显著分化，再按用例拆分。 |

**拒绝“为了拆而拆”：** 类数量不是设计质量指标；保留的理由和拆分的理由一样，都要写清楚。

### 3.11 低风险 3：`Repository<T extends Identifiable>`

**评审意见：** 泛型仓储应约束 `T extends Identifiable`，让 ID 分配与 `CsvCodec` 契约在类型层面一致。

**核对结果：** 采纳。当前 `Repository<T>` 允许没有 `getId()` 的类型，`save` 的 ID 分配只能靠运行时假设。

**Iteration 2 处理：**

- `Repository<T extends Identifiable>`
- `CsvCodec<T extends Identifiable>`
- `CsvRepository<T extends Identifiable>`
- 持久化类图与架构设计第 4.3、5.2 节同步更新。

### 3.12 低风险 4：record 的集合与空值语义

**评审意见：** `StudentStatistics`、`WarningResult` 等 record 的集合字段应明确不可变/空值语义。

**核对结果：** 采纳。

**Iteration 2 处理：**

- 所有 record 的 `List` 字段在紧凑构造器中执行 `List.copyOf(...)`；传入 `null` 视为编程错误，抛 `NullPointerException` 或由 service 保证不传 `null`。
- 空列表表示“没有明细/没有命中规则”，与 `DataStatus.INSUFFICIENT` 区分。
- 数值字段的可空规则见 3.7；UI 不把 `null` 当作 0。

---

## 4. 未采纳或保留的第 1 轮设计

以下项目经核对后保留，不作为 Iteration 2 的修改项：

| 项目 | 保留理由 |
|---|---|
| `Session` 同时保存当前用户与可见范围 | 第 1 轮已确认合并；本项目只有管理员/教师两种角色，拆成 `Session` + `AccessScope` 会增加传递依赖。 |
| UI 层分页 | 第 1 轮已确认；service 返回完整列表，`TablePrinter` 切片，每页 10 条。 |
| `ClassStatistics.studentCount` 为全班人数 | 第 1 轮已确认；教师可见范围只影响成绩指标，不影响班级人数。 |
| `Person` 继承 | `Employee`/`Student` 共享姓名、性别、电话和资料更新行为；当前继承层次浅，改组合的收益不足以抵消改动。 |
| `AbstractEntity` + `Identifiable` | 为 7 个实体提供统一 ID、相等性和仓储约束；不是装饰性抽象。 |
| DTO 数量较多 | 统计、排名、预警、班级预警的字段组合不同；合并成一个大 DTO 会引入大量可空字段和语义歧义。 |
| `CsvRepository<T>` + 7 个 `CsvCodec` | 泛型仓储隔离 CSV 细节，codec 按实体负责解析；这是本项目展示 Strategy 与 DIP 的主要位置。 |
| `DataLoader` 做跨文件引用校验 | 启动阶段必须在 service 可用前发现坏行；把校验放到 service 会让每个 service 都承担加载容错。 |

---

## 5. 对 Iteration 2 的输入清单

Iteration 2 必须包含以下变化，且每项在 `design-evolution.md` 中有对照行：

1. 领域图：`Student` 为 `Score` 的唯一组合拥有者；`ClassCourse—Score` 改普通关联；多端显式 `0..*`。
2. 领域约束：成绩唯一键 `(studentId, courseId)`；`classCourseId` 不可变；转班同课程不新建成绩。
3. 服务图：`ScoreService.record` 的唯一性流程；`ScoreService` 的删除/修改权限按成绩原 `ClassCourse` 校验；`CourseService.list` 支持教师只读。
4. 用例：`UC-06a 查看课程目录`；管理类用例的 CRUD 展开说明。
5. DTO：`DataStatus`、可空指标、`StudentStatistics.scoreCount`、`ScoreDetail.updatedAt`、不可变集合。
6. 持久化：`Repository<T extends Identifiable>`；加载时校验 `(studentId, courseId)` 唯一。
7. UI：`MenuFactory` 拆为接口 + `AdminMenuFactory` / `TeacherMenuFactory`；教师课程目录入口。
8. 文档：架构设计 v0.5、README 索引、Iteration 2 文档、演进说明。

---

## 6. 与报告章节的对应

| 报告章节 | 本文档的作用 |
|---|---|
| 3.3 Class diagram — Iteration 1 | 分析对象；Iteration 1 本身不改 |
| 3.4 GenAI analysis of Iteration 1 | 本文档 |
| 3.5 Class diagram — Iteration 2 (final) | 本文档第 5 节是 Iteration 2 的输入 |
| 3.6 Summary of design evolution | 本文档的采纳/拒绝结论是演进表的依据 |
| 3.7 Critical Reflection | 学生本人撰写；本文档不代写 |
