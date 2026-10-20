# `docs/design/` 软件设计审查报告

## 审查范围

本审查对照以下内容完成：

- `.claude/skills/pdc-project1-architecture-standards/SKILL.md`
- `.claude/skills/pdc-project1-development-process/SKILL.md`
- `docs/requirements/requirements-analysis.md` v0.4
- `docs/design/README.md`
- `docs/design/architecture-design.md`
- `docs/design/use-case-model.md` 与 `diagrams/use-case.puml`
- `docs/design/class-diagram-iteration-1.md` 及其 5 张 PlantUML 类图

本报告审查设计内容和设计交付状态，不评价学生自撰反思的文字质量。

## 总体结论

架构方向基本正确：四层职责、依赖倒置、CSV 仓储接口、会话权限、统计/预警 DTO、初始化和加载诊断都已经有明确落点。用例目录也覆盖了大部分需求，正式 Iteration 1 的类图比草稿更完整。

当前设计还不能直接冻结为最终设计，存在两个高风险模型问题和一个明确的用例追溯遗漏：

1. `Score` 同时被 `Student` 和 `ClassCourse` 作为 UML 组合对象持有，违反组合关系的生命周期语义。
2. 类图多处使用 `1..*`，与需求允许空部门、空班级、无成绩学生、待分配教学任务和无成绩课程不一致。
3. 成绩唯一性在需求要求的 `(studentId, courseId)`、服务说明中的 `(studentId, classCourseId)` 和设计追溯之间不一致。
4. 教师可查看课程目录（FR-COURSE-05），但用例图和用例目录只把 UC-06 连接到管理员。

此外，当前目录仍处于 Iteration 1 阶段；`class-diagram-iteration-2-final.md`、`genai-design-analysis.md` 和 `design-evolution.md` 仍标记为待完成。这不是本轮架构模型错误，但在 Project 1 Component 1.2 交付上尚未闭环。

## 高风险问题

### 1. `Score` 存在两个组合拥有者

**位置：** `diagrams/class-domain-iteration-1.puml`、`class-diagram-iteration-1.md` 第 2.3 节。

当前同时画了：

```text
Student ◆── Score
ClassCourse ◆── Score
```

而需求又规定：

- 删除普通学生时级联删除成绩；
- 有成绩的教学任务不能移除；
- `Score.classCourseId` 决定历史班级归属；
- 归档学生和归档成绩不能删除。

UML 组合通常表示一个对象由一个组合拥有者控制生命周期。一个 `Score` 同时被两个实心菱形拥有，会让删除、转班和归档的生命周期无法由关系图解释清楚。

**建议：** 不要把两个方向都画成组合。可以改为普通关联，并在约束注释中写明：删除学生时由 `StudentService` 执行成绩级联；删除教学任务前由 `TeachingService` 检查成绩引用；归档状态下禁止删除。这更符合实际的跨实体业务规则。若坚持保留一个组合，应明确唯一的生命周期拥有者及另一方向只是普通引用。

### 2. 类图多重性普遍把“可为空/可为空集合”画成了必有一条

**位置：** `class-domain-iteration-1.puml` 与 `class-diagram-iteration-1.md` 第 2.3 节。

当前关系包括：

```text
Department   1 ─── * Employee
SchoolClass  1 ─── * ClassCourse
Course       1 ─── * ClassCourse
SchoolClass  1 ─── * Student
Student      1 ─── * Score
ClassCourse  1 ─── * Score
```

右侧的 `*` 在 UML 中等价于 `0..*`，但左侧的 `1` 表示每个实体必须至少有一项关联对象。需求明确允许：

- 部门暂时没有员工，否则删除部门的“仍有员工时拒绝”没有空部门场景；
- 新建班级暂时没有学生和课程；
- 课程尚未被任何班级采用；
- 学生没有任何成绩；
- 教学任务处于待分配且没有成绩。

因此建议至少改为：

```text
Department  1 ─── 0..* Employee
SchoolClass 1 ─── 0..* ClassCourse
Course      1 ─── 0..* ClassCourse
SchoolClass 1 ─── 0..* Student
Student     1 ─── 0..* Score
ClassCourse 1 ─── 0..* Score
```

如果某些实体的父引用也可能缺失，还应在相应端标出 `0..1`，并与加载规则保持一致。

### 3. 成绩唯一性键在设计中不一致

**位置：** 需求文档 4.1、`architecture-design.md` 第 3.4 节、`class-diagram-iteration-1.md` 第 3.3 节、UC-10 描述。

需求要求 `Score` 的唯一组合是 `(studentId, courseId)`，并且同一学生同一课程只能有一条成绩。由于 `courseId` 通过 `ClassCourse` 解析，学生转班后旧成绩仍然占用该课程的成绩键。

但服务流程写成：

```text
已有 (studentId, classCourseId) 则更新，否则新增
```

这允许同一学生在同一课程的两个不同 `ClassCourse` 中各有一条成绩，直接违反需求。类图也没有把这一项作为约束注释出来。

**建议：** 统一为：

1. 由 `classCourseId` 找到 `courseId`；
2. 检查该学生是否已有同一 `courseId` 的 Score；
3. 有记录则只允许更新原 `Score`，不创建新的历史记录；
4. 在 `ScoreService.record`、CSV 加载校验、类图约束和 AC 中使用同一表述。

如果实际想允许学生重修同一课程，则必须修改需求和数据模型，增加修读/学期/教学任务粒度，不能只把唯一键改成 `classCourseId`。

### 4. 教师查看课程目录没有用例落点

**依据：** 需求 FR-COURSE-05 和权限矩阵允许教师查看课程；`use-case-model.md` 的 UC-06“管理课程”只列管理员，`use-case.puml` 也只连接 `Admin --> UC06`。

这会导致需求追溯无法从用例图恢复“教师只读课程目录”。

**建议：** 二选一：

- 将 UC-06 改名为“查看/管理课程”，同时连接教师和管理员，并在叙述中区分教师只读、管理员 CRUD；或
- 新增 `UC-06a 查看课程目录`，连接教师和管理员，管理员管理用例继续只连接管理员。

同时更新第 4 节菜单映射和第 5 节需求追溯。

## 中等风险问题

### 5. 正式 Iteration 1 尚未形成完整的两轮设计闭环

`docs/design/README.md` 明确显示以下工件仍为待完成：

- `genai-design-analysis.md`；
- `class-diagram-iteration-2-final.md`；
- `design-evolution.md`。

架构规范要求至少两轮设计，且必须记录具体的 GenAI 建议、采纳/拒绝理由和结构变化。当前“候选批判点”只是待讨论清单，不能替代正式分析记录；README 中的 Iteration 1 也应保留为独立初版，后续修订不能直接覆盖它。

**建议：** 先对正式 Iteration 1 做逐条批判，再产出 Iteration 2 final 和演进表。最终类图中的每个类、方法和关系要能对应实现计划。

### 6. 初版类图的“独立初版”证据不足

`class-diagram-iteration-1-draft.md` 明确标注为 AI 辅助草案，`class-diagram-iteration-1.md` 又标为正式冻结版，但没有在设计记录中说明正式版哪些结构由学生独立确认、哪些是在 AI 草案基础上重画和修改的。

这不是类结构本身的错误，但会削弱“先设计、再批判、再迭代”的可证明性。

**建议：** 在设计过程记录中补充正式 Iteration 1 的确认依据和人工修改清单；不要把 AI 草案直接当作独立初版证据。

### 7. DTO 没有完整表达“数据不足”和成绩更新时间

`ClassStatistics`、`CourseStatistics` 使用 `double` 的平均分/最高分/最低分和 `int scoreCount`，可以通过 `scoreCount == 0` 推断数据不足，但设计没有明确这是 UI 的固定呈现契约。`StudentStatistics` 没有 `scoreCount`，只能依赖 `details` 为空或 `rankNote` 推断。

此外，需求 FR-SCORE-07 要求记录并展示 `updatedAt`，但统计 DTO 的 `ScoreDetail` 没有 `updatedAt`；`ScoreService.listByClassCourse` 返回 Score 的设计也没有在 UI 追溯中说明如何展示该字段。

**建议：** 明确 DTO/UI 契约：无成绩时使用显式状态或明确的 `scoreCount`/`dataStatus`，并在成绩列表 DTO 或 UI 追踪中加入 `updatedAt`。这样“数据不足”和合法的 0 分不会靠隐式约定区分。

### 8. 部分关系虽然文字解释正确，但图形语义仍容易误读

`ClassCourse 0..* ─── 0..1 Employee` 的文字已解释为“每项任务零或一位教师、一位教师可承担多项任务”，这是可接受的，但建议在图上标注角色名和端点方向，避免读者按连接线左到右误读。该问题低于前述多重性错误，因为当前文字已经提供了补充解释。

## 低风险改进

1. 用例中的“管理部门/管理课程/管理成绩”是聚合型用例，建议在关键用例叙述或报告中明确它们包含查看、新增、修改、删除和异常流程，避免被评审认为只是一个按钮。
2. `MenuFactory`、`DataInitializer`、`StatisticsService` 被列为候选上帝类，应在 Iteration 2 中给出明确取舍，而不是只保留“可能成为上帝类”的备注。
3. `Repository<T>` 最好写成 `Repository<T extends Identifiable>`，让 ID 分配和 `CsvCodec<T>` 的契约在类型层面一致；否则泛型仓储接受没有 ID 的类型，抽象边界较弱。
4. `StudentStatistics`、`WarningResult` 等 record 的字段应在 Iteration 2 中补充不可变集合/空值语义，避免 UI 直接修改内部列表或把 `null` 当作多种状态。

## 建议修订顺序

1. 先修正 Score 的双组合关系和全部实体的最小多重性。
2. 统一成绩唯一性键，补上 `ScoreService.record` 和类图约束。
3. 补教师查看课程目录的用例和追溯。
4. 完成 GenAI 批判记录、Iteration 2 final 和演进说明。
5. 明确 DTO 的数据不足、更新时间和空值契约。
6. 依据最终结构更新 PlantUML、PNG 和 README 索引。
