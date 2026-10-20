# 设计演进说明 — Iteration 1 → Iteration 2

| 项目 | 内容 |
|---|---|
| 文档版本 | v1.2（含两轮复审补正） |
| 日期 | 2026-10-20 |
| 课程 | COMP603/ENSE600 Program Design & Construction |
| 作业 | Project 1（40%），Component 1.2 Software Design |
| 对应报告 | 3.6 Summary of design evolution |
| 第 1 轮 | [`class-diagram-iteration-1.md`](../iterations/iteration-1/class-diagram-iteration-1.md) v1.0（冻结） |
| GenAI 分析 | [`genai-design-analysis.md`](genai-design-analysis.md) |
| 第 2 轮 | [`class-diagram-iteration-2-final.md`](../iterations/iteration-2/class-diagram-iteration-2-final.md) |
| 冻结日期 | 2026-10-20（Iteration 2 与用例模型 v0.2 一并冻结） |

> 本文档记录两轮设计之间的结构变化和取舍，不包含学生自撰的 Critical Reflection（3.7）。

---

## 1. 演进概览

Iteration 1 建立了完整的四层结构、领域实体、服务、DTO、持久化与 CUI 菜单。Iteration 2 没有推翻分层，而是修正了五类问题：

1. **关系语义**：`Score` 的双组合改为单一组合 + 普通关联。
2. **需求一致性**：成绩唯一键从 `(studentId, classCourseId)` 统一为 `(studentId, courseId)`；补 `UC-06a`。
3. **结果契约**：DTO 用 `DataStatus` 和可空指标显式表达“数据不足”，补 `updatedAt`。
4. **职责与类型**：拆分 `MenuFactory`，约束 `Repository<T extends Identifiable>`。
5. **需求字段补正**：`Employee` 补入需求中已定义的可选邮箱 `email`，并提供 `getEmail` / `updateEmail` 读取与更新契约。

| 维度 | Iteration 1 | Iteration 2 |
|---|---|---|
| 领域关系 | `Student ◆— Score`、`ClassCourse ◆— Score` | `Student ◆— Score`、`ClassCourse — Score` |
| 多重性写法 | 多端 `*` | 多端 `0..*` |
| 成绩唯一键 | `(studentId, classCourseId)` | `(studentId, courseId)` |
| 教师课程查看 | 无独立用例 | `UC-06a` |
| 统计 DTO | `scoreCount == 0` 隐式判断 | `DataStatus` + 可空指标 |
| 成绩明细 | 无 `updatedAt` | `ScoreDetail.updatedAt` |
| 仓储泛型 | `Repository<T>` | `Repository<T extends Identifiable>` |
| 菜单工厂 | `MenuFactory` 单类 | 接口 + `AdminMenuFactory` / `TeacherMenuFactory` |
| record 集合 | 未定义 | `List.copyOf`；空列表 ≠ 数据不足 |
| 员工邮箱模型（复审补正） | `Employee` 未列出需求中的可选邮箱 | `Employee.email` 与 `getEmail` / `updateEmail` 补入最终领域模型 |

---

## 2. 逐项演进

### 2.0 复审补正：员工邮箱

对 Iteration 2 的复审（[`design-review-iteration-2.md`](../../../codex_review/design-review-iteration-2.md)、[`design-review-iteration-2-recheck.md`](../../../codex_review/design-review-iteration-2-recheck.md)）发现，需求允许新增和修改员工邮箱（FR-EMP-03/04/10），且员工字段清单包含 `email`，但第 2 轮的 `Employee` 领域类未建模该字段。最终领域模型现补入可空的 `Employee.email`、`getEmail()` 和 `updateEmail(String)`；邮箱格式由 `EmployeeService` 校验，实体负责保存更新后的值，读取统一走 `getEmail()`。该补正同步到领域类图源与架构实体职责表。学生已于 2026-10-20 确认：`email` 归属于 `Employee`（不放到 `Person`），格式由 `EmployeeService` 校验。Iteration 2 整体另于同日确认冻结，见 3.1。

### 2.1 `Score` 的生命周期与关系

**Iteration 1：**

```text
Student    "1" *-- "*" Score : owns
ClassCourse "1" *-- "*" Score : assessed by
```

两个组合拥有者会让“删除学生级联删除成绩”和“有成绩时不得移除教学任务”两条规则在图上互相冲突。

**Iteration 2：**

```text
Student    "1" ◆-- "0..*" Score : owns
ClassCourse "1" --- "0..*" Score : assessed by
```

- `Student` 是唯一组合拥有者；删除普通学生时由 `StudentService` 级联删除成绩。
- `ClassCourse` 不是组合拥有者；`TeachingService.removeCourse` 在删除前检查成绩引用并拒绝。
- 归档保护状态下禁止删除学生，因此不会误触发级联。

**证据：** `docs/design/iterations/iteration-2/diagrams/class-domain-iteration-2.puml`；`docs/design/iterations/iteration-2/class-diagram-iteration-2-final.md` 第 2.3 节。

### 2.2 多重性写法

**Iteration 1：** 多端使用 `*`。

**Iteration 2：** 多端统一写 `0..*`，并在类图注释和文档中说明阅读方向。

**说明：** 这不是语义变化；`*` 在 UML 中本来就是 `0..*`。修改目的是消除“左侧 `1` 是否意味着父对象必须有子对象”的误读。父端的 `1` 表示子对象必须引用一个父对象，空部门、空班级、无成绩学生、待分配任务仍然允许。

**证据：** `class-diagram-iteration-2-final.md` 第 1 节、第 2.3 节。

### 2.3 成绩唯一键

**Iteration 1：**

```text
已有 (studentId, classCourseId) 则更新，否则新增
```

**Iteration 2：**

```text
由 classCourseId 解析 courseId
按 (studentId, courseId) 查找已有成绩
同一 ClassCourse → 更新原记录与 updatedAt
其他 ClassCourse → 不新建；能访问原 ClassCourse 时更新原记录，否则提示原班级教师维护
```

**影响：**

- `ScoreService.record` 不再按教学任务粒度判重。
- `DataLoader` 加载 `scores.csv` 时执行同一唯一性校验。
- UC-10 的异常流程、类图约束、架构设计第 3.4 节统一表述。
- 不支持重修同一课程；若以后需要，必须扩展成绩模型，不能只改唯一键。

**证据：** `class-diagram-iteration-2-final.md` 第 2.3、3.3 节；`architecture-design.md` 第 3.4、5.3 节；`use-case-model.md` UC-10。

### 2.4 教师查看课程目录

**Iteration 1：** `UC-06` 只连接管理员，FR-COURSE-05 没有用例落点。

**Iteration 2：**

- 新增 `UC-06a 查看课程目录`，连接管理员和教师。
- `UC-06` 保留为管理员的课程管理用例。
- 菜单增加“教师 → 我的教学 → 课程目录”（需求 6.3 子菜单的设计细化）。
- `CourseService.list` 对教师开放；写操作执行 `Session.requireAdmin()`。

**证据：** `docs/design/use-case/use-case-model.md` v0.2；`docs/design/use-case/use-case.puml`；`docs/design/iterations/iteration-2/diagrams/class-ui-iteration-2.puml`。

### 2.5 DTO 的“数据不足”与更新时间

**Iteration 1：**

- `ClassStatistics`/`CourseStatistics` 用 `scoreCount == 0` 推断无数据。
- `StudentStatistics` 没有 `scoreCount`。
- `ScoreDetail` 没有 `updatedAt`。

**Iteration 2：**

- 新增 `DataStatus { AVAILABLE, INSUFFICIENT }`。
- `ClassStatistics`、`CourseStatistics`、`StudentStatistics` 增加 `dataStatus`。
- `StudentStatistics` 增加 `scoreCount`。
- 无数据时指标改为可空 `Double`/`Integer`；`WarningResult`、`ClassWarning` 同步。
- `ScoreDetail` 增加 `updatedAt`。
- record 的 `List` 字段 `List.copyOf`；空列表表示“没有明细”，不是“数据不足”。

**证据：** `docs/design/iterations/iteration-2/class-diagram-iteration-2-final.md` 第 3.4 节；`docs/design/iterations/iteration-2/diagrams/class-dto-iteration-2.puml`。

### 2.6 泛型仓储约束

**Iteration 1：** `Repository<T>`、`CsvCodec<T>`、`CsvRepository<T>`。

**Iteration 2：** `Repository<T extends Identifiable>`、`CsvCodec<T extends Identifiable>`、`CsvRepository<T extends Identifiable>`。

**理由：** 让 ID 分配、`CsvCodec.idOf/withId` 与实体契约在类型层面一致，编译期排除没有 ID 的类型。

**证据：** `class-diagram-iteration-2-final.md` 第 3.5 节；`architecture-design.md` 第 4.3、5.2 节；`class-persistence-iteration-2.puml`。

### 2.7 菜单工厂拆分

**Iteration 1：** 单个 `MenuFactory` 同时装配管理员和教师菜单。

**Iteration 2：**

```text
MenuFactory（interface）
  ├── AdminMenuFactory
  └── TeacherMenuFactory
```

- 组合根创建两个实现，`LoginUI` 按登录角色选择。
- 角色判断只影响菜单外观；权限仍由 service 的 `Session` 校验。

**证据：** `class-ui-iteration-2.puml`；`architecture-design.md` 第 2.3、4.4 节。

### 2.8 保留项

以下第 1 轮设计经分析后保留，不进入第 2 轮修改：

| 项目 | 保留理由 |
|---|---|
| `Session` 合并可见范围 | 只有管理员/教师两种角色；拆分增加传递依赖 |
| UI 层分页 | service 返回完整列表；`TablePrinter` 切片；每页 10 条 |
| `ClassStatistics.studentCount` 全班口径 | 教师可见范围只影响成绩指标 |
| `Person` 继承 | 共享字段与资料更新行为明确 |
| `AbstractEntity` / `Identifiable` | 统一 ID、相等性与仓储约束 |
| `DataInitializer` 单类 | 一次性启动编排，拆分类不降低实际复杂度 |
| `StatisticsService` 单类 | 统一统计口径与可见范围过滤 |
| 7 个 `CsvCodec` | 每实体独立解析，体现 Strategy |

---

## 3. Iteration 1 的确认记录

正式 Iteration 1 由讨论稿经学生确认后固化。以下六项是确认过的设计决策，记录事实，不代替学生的设计说明：

| # | 决策 | 确认结果 |
|---|---|---|
| 1 | 根包名 | `com.pdc.learning` |
| 2 | `MenuFactory` | Iteration 1 保持单类，Iteration 2 再拆分 |
| 3 | `Session` / `AccessScope` | 合并为一个 `Session` 类 |
| 4 | DTO 形式 | 统一使用 Java `record` |
| 5 | `ClassStatistics.studentCount` | 全班学生数，不受教师可见课程范围影响 |
| 6 | 列表分页 | UI 层分页，每页 10 条 |

**关于“人工重画/修改清单”：** 该部分属于学生本人的设计过程记录，AI 不代为编造。学生应在提交前补充：

- 是否在讨论稿基础上手工重画了类图；
- 手工修改了哪些类、属性、方法或关系；
- 是否有未写入讨论稿、但由学生独立提出的设计点。

如果实际情况是“基于讨论稿确认、未手工重画”，应如实记录，不应虚构独立绘制过程。

### 3.1 Iteration 2 的确认记录

学生于 2026-10-20 确认最终排版，并同意冻结 Iteration 2 与用例模型 v0.2。冻结后的 `class-diagram-iteration-2-final.md` v1.2、五张 Iteration 2 图源及 `use-case-model.md` v0.2 作为报告 3.1、3.5 的提交版本。冻结不是“永远不会改”：实现阶段若发现设计不可行，按变更流程记录后更新，不能只改代码。

---

## 4. 对实现阶段的影响

1. **领域层**：`Score` 与 `Student` 的组合关系只影响 UML 语义；代码仍保存 `studentId`/`classCourseId`，由 service 维护级联。
2. **服务层**：`ScoreService.record` 必须先解析 `courseId` 再判重；`update`/`delete` 按成绩原 `ClassCourse` 校验权限。
3. **持久化层**：`Repository`/`CsvCodec`/`CsvRepository` 加 `extends Identifiable`；加载时校验成绩唯一键。
4. **DTO/UI**：无数据时使用 `DataStatus.INSUFFICIENT` 与 `null` 指标，UI 显示“数据不足”；成绩明细打印 `updatedAt`。
5. **菜单**：实现 `MenuFactory` 接口与两个实现；`CourseUI` 支持管理员 CRUD 与教师只读两种呈现。
6. **验收**：AC-10 的重复成绩、AC-16/AC-48 的数据不足、AC-25 的待分配任务、AC-28 的教师范围需要按第 2 轮契约复核。
7. **员工资料**：`Employee` 保存可空 `email` 并通过 `getEmail()` 读取；`EmployeeService.create/update` 校验邮箱格式；`EmployeeCsvCodec` 读写该列；`EmployeeUI` 的新增/修改表单包含选填邮箱。

---

## 5. 与报告章节的对应

| 报告章节 | 内容 |
|---|---|
| 3.3 Class diagram — Iteration 1 | `class-diagram-iteration-1.md`（冻结，不改） |
| 3.4 GenAI analysis of Iteration 1 | `genai-design-analysis.md` |
| 3.5 Class diagram — Iteration 2 (final) | `class-diagram-iteration-2-final.md` |
| 3.6 Summary of design evolution | 本文档 |
| 3.7 Critical Reflection | 学生本人撰写；本文档不代写 |
