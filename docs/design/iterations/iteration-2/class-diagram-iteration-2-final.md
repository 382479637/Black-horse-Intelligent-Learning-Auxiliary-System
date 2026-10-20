# 类图 — Iteration 2（最终）

| 项目 | 内容 |
|---|---|
| 文档版本 | v1.2（Iteration 2 最终设计，含两轮复审补正；已冻结） |
| 日期 | 2026-10-20 |
| 课程 | COMP603/ENSE600 Program Design & Construction |
| 作业 | Project 1（40%），Component 1.2 Software Design |
| 需求基线 | `docs/requirements/requirements-analysis.md` v0.4 |
| 用例基线 | `docs/design/use-case/use-case-model.md` v0.2 |
| 图源 | `diagrams/class-domain-iteration-2.puml`、`diagrams/class-service-iteration-2.puml`、`diagrams/class-dto-iteration-2.puml`、`diagrams/class-persistence-iteration-2.puml`、`diagrams/class-ui-iteration-2.puml` |
| 状态 | 第 2 轮最终设计，已由学生确认并冻结（2026-10-20）；采纳项见 `docs/design/analysis/genai-design-analysis.md`，复审补正见 `codex_review/design-review-iteration-2.md` 与 `design-review-iteration-2-recheck.md` |
| 第 1 轮基线 | [`class-diagram-iteration-1.md`](../iteration-1/class-diagram-iteration-1.md) v1.0（冻结） |
| 演进说明 | [`design-evolution.md`](../../analysis/design-evolution.md) |

> 本文件是 Iteration 2 最终设计，与 Iteration 1 分开保存；Iteration 1 不被覆盖。第 2 轮包含 `docs/design/analysis/genai-design-analysis.md` 中采纳或部分采纳的修改，以及第 2 轮复审确认的需求字段补正；未采纳项仍在第 1 轮基线上保留。
>
> 本文档不包含学生自撰的设计反思（3.7）。

### Iteration 2 变更摘要

| # | 变更 | 第 1 轮 | 第 2 轮 |
|---|---|---|---|
| 1 | `Score` 生命周期 | `Student` 与 `ClassCourse` 双组合 | `Student` 唯一组合；`ClassCourse—Score` 普通关联 |
| 2 | 多重性写法 | 多端写 `*` | 多端显式写 `0..*` |
| 3 | 成绩唯一键 | `(studentId, classCourseId)` | `(studentId, courseId)`，`courseId` 经 `ClassCourse` 解析 |
| 4 | 教师课程目录 | 无独立用例 | 新增 `UC-06a` |
| 5 | DTO 数据不足 | 依赖 `scoreCount == 0` 隐式判断 | 新增 `DataStatus` 与可空指标 |
| 6 | 成绩更新时间 | `ScoreDetail` 无 `updatedAt` | `ScoreDetail.updatedAt` |
| 7 | 泛型仓储 | `Repository<T>` | `Repository<T extends Identifiable>` |
| 8 | 菜单工厂 | 单个 `MenuFactory` | 接口 + `AdminMenuFactory` / `TeacherMenuFactory` |
| 9 | record 集合 | 未定义 | `List.copyOf`，空列表与“数据不足”分离 |
| 10 | 员工邮箱 | `Employee` 未建模需求中的可选邮箱 | `Employee.email` + `getEmail()` / `updateEmail(String)` |

### 保留的设计决策

| 项目 | 决策 |
|---|---|
| 根包名 | `com.pdc.learning` |
| `Session` / `AccessScope` | 合并为一个 `Session` 类，服务只依赖 `Session` |
| DTO | 统一使用 Java `record` |
| `ClassStatistics.studentCount` | 全班学生数，不受教师可见课程范围影响 |
| 列表分页 | UI 层分页；service 返回完整过滤/排序结果；`TablePrinter.page` 切片；每页 10 条 |
| `DataInitializer` | 保留单类，负责一次性启动编排 |
| `StatisticsService` | 保留单类，统一统计口径与可见范围过滤 |

---

## 1. 建模约定

| 项目 | 约定 |
|---|---|
| 类名 | PascalCase；`SchoolClass` 代替 `Class`，避免与 `java.lang.Class` 冲突 |
| 属性 | `-` private；类型使用 Java 类型；ID 使用 `long`，可空外键使用 `Long` |
| 方法 | `+` public；只列职责相关方法，不机械列出所有 getter/setter |
| 属性读取 | 属性一律 `private`；常规读取通过 getter 暴露（如 `getName()`、`getEmail()`），service、UI 与 CSV codec 不直接访问字段；只有需要明确跨包读取契约时才在类目录单独列出 getter |
| 关系 | 继承/实现、关联、组合；只在语义成立时使用聚合/组合；每个部分最多一个组合拥有者 |
| 多重性 | 两端都标；多端统一写 `0..*`，不写 `*`；`1` 在父端表示子对象必须引用一个父对象，`0..*` 在子端表示父对象可以没有子对象 |
| 关联实现 | 领域对象之间主要保存 ID，不保存对象引用；由 service 解析，避免循环对象图 |
| 时间 | 日期 `LocalDate`，时间戳 `LocalDateTime` |
| DTO 集合 | record 的 `List` 字段在紧凑构造器中 `List.copyOf`；空列表表示“没有明细”，不表示“数据不足” |
| DTO 空值 | 无可见成绩时，统计指标用 `null`，不用 0 冒充；由 `DataStatus` 显式区分 |

**包结构**见 [`architecture-design.md`](../../architecture/architecture-design.md) 第 2.3 节。

---

## 2. 领域模型类图

完整 PlantUML 源：[`diagrams/class-domain-iteration-2.puml`](diagrams/class-domain-iteration-2.puml)。

### 2.1 结构概览

```text
Identifiable
   ▲
AbstractEntity
   ▲
   ├── Person
   │      ├── Employee
   │      └── Student
   ├── Department
   ├── Course
   ├── SchoolClass
   ├── ClassCourse
   └── Score

Department   1 ────── 0..* Employee
SchoolClass  0..* ──── 0..1 Employee      （headTeacherId）
ClassCourse  0..* ──── 0..1 Employee      （teacherId）
SchoolClass  1 ◆────── 0..* ClassCourse   （组合）
Course       1 ────── 0..* ClassCourse
SchoolClass  1 ────── 0..* Student        （当前班级）
Student      1 ◆────── 0..* Score         （组合；唯一生命周期拥有者）
ClassCourse  1 ────── 0..* Score          （普通关联；有成绩时不得移除任务）
```

### 2.2 领域类目录

#### `Identifiable`（interface）

| 成员 | 说明 |
|---|---|
| `+ getId(): long` | 所有实体统一 ID 契约 |

#### `AbstractEntity`（abstract class，implements `Identifiable`）

| 成员 | 说明 |
|---|---|
| `- id: long` | 内部 ID；0 表示新实体 |
| `+ getId(): long` | 读取 ID |
| `# setId(long): void` | 仅子类/仓储可设置 |
| `+ isNew(): boolean` | `id == 0` |
| `+ equals(Object): boolean` | 按 ID 比较 |
| `+ hashCode(): int` | 与 equals 一致 |

#### `Person`（abstract class，extends `AbstractEntity`）

| 成员 | 说明 |
|---|---|
| `- name: String` | 姓名 |
| `- gender: Gender` | 性别枚举 |
| `- phone: String` | 手机号，可空 |
| `+ getName(): String` | 读取姓名 |
| `+ getGender(): Gender` | 读取性别 |
| `+ getPhone(): String` | 读取手机号 |
| `+ updateProfile(String, Gender, String): void` | 更新公共人员资料 |

#### `Employee`（extends `Person`）

| 成员 | 说明 |
|---|---|
| `- username: String` | 登录用户名，唯一 |
| `- email: String` | 邮箱，可空；由 `EmployeeService` 校验格式后更新 |
| `- passwordHash: String` | PBKDF2 派生密钥 Base64 |
| `- passwordSalt: String` | 16 字节盐 Base64 |
| `- departmentId: long` | 所属部门 |
| `- role: Role` | `ADMIN` / `TEACHER` |
| `- status: EmployeeStatus` | `ACTIVE` / `DISABLED` |
| `- hireDate: LocalDate` | 入职日期 |
| `+ isAdmin(): boolean` | 角色判断 |
| `+ isActive(): boolean` | 状态判断 |
| `+ canTeach(): boolean` | `TEACHER && ACTIVE` |
| `+ getEmail(): String` | 读取邮箱；供 `EmployeeService`、`EmployeeUI` 与 `EmployeeCsvCodec` 使用 |
| `+ updateEmail(String): void` | 更新员工邮箱；允许空值，格式由 service 校验 |
| `+ updatePassword(String salt, String hash): void` | 更新密码材料 |
| `+ changeStatus(EmployeeStatus): void` | 状态变更；跨实体规则在 service |
| `+ changeRole(Role): void` | 角色变更；跨实体规则在 service |
| `+ getPasswordSalt(): String` | 认证/持久化读取 |
| `+ getPasswordHash(): String` | 认证/持久化读取 |

#### `Student`（extends `Person`）

| 成员 | 说明 |
|---|---|
| `- studentNo: String` | 学号，唯一 |
| `- classId: long` | 当前班级 |
| `- enrollmentDate: LocalDate` | 入学日期 |
| `- status: StudentStatus` | `ACTIVE` / `SUSPENDED` / `GRADUATED` |
| `+ isActive(): boolean` | 状态判断 |
| `+ transferTo(long classId): void` | 更新当前班级；历史成绩不变 |
| `+ changeStatus(StudentStatus): void` | 状态变更；跨实体规则在 service |

#### `Department`（extends `AbstractEntity`）

| 成员 | 说明 |
|---|---|
| `- name: String` | 部门名称，唯一 |
| `- description: String` | 说明，可空 |
| `+ rename(String): void` | 改名 |
| `+ updateDescription(String): void` | 更新说明 |

#### `Course`（extends `AbstractEntity`）

| 成员 | 说明 |
|---|---|
| `- code: String` | 课程编号，唯一 |
| `- name: String` | 课程名称 |
| `- description: String` | 说明，可空 |
| `+ update(String code, String name, String description): void` | 更新课程资料 |

#### `SchoolClass`（extends `AbstractEntity`）

| 成员 | 说明 |
|---|---|
| `- code: String` | 班级编号，唯一 |
| `- name: String` | 班级名称 |
| `- headTeacherId: Long` | 班主任 ID，可空 |
| `- startDate: LocalDate` | 开始日期，仅展示 |
| `- endDate: LocalDate` | 结束日期，仅展示 |
| `- capacity: int` | 容量 |
| `- status: ClassStatus` | `PLANNED` / `ACTIVE` / `FINISHED` |
| `+ isFull(int currentCount): boolean` | 当前人数 ≥ 容量 |
| `+ canAcceptStudent(): boolean` | 状态为 `PLANNED` 或 `ACTIVE` |
| `+ isFinished(): boolean` | 状态为 `FINISHED` |
| `+ changeStatus(ClassStatus): void` | 设置状态；转换合法性在 service |
| `+ assignHeadTeacher(Long): void` | 设置/清空班主任 |
| `+ updateDetails(...): void` | 更新编号/名称/日期/容量 |

#### `ClassCourse`（extends `AbstractEntity`）

| 成员 | 说明 |
|---|---|
| `- classId: long` | 所属班级，创建后不可变 |
| `- courseId: long` | 课程，创建后不可变 |
| `- teacherId: Long` | 任课教师，可空=待分配 |
| `- createdAt: LocalDateTime` | 创建时间 |
| `+ isAssigned(): boolean` | `teacherId != null` |
| `+ assignTeacher(Long): void` | 分配/更换教师 |
| `+ unassignTeacher(): void` | 置为待分配 |

#### `Score`（extends `AbstractEntity`）

| 成员 | 说明 |
|---|---|
| `- studentId: long` | 学生 |
| `- classCourseId: long` | 教学任务，创建后不可变 |
| `- score: double` | 0–100 |
| `- updatedAt: LocalDateTime` | 最后修改时间 |
| `+ isPassed(): boolean` | `score >= 60` |
| `+ updateScore(double, LocalDateTime): void` | 校验范围并更新时间 |

#### 枚举

| 枚举 | 值 |
|---|---|
| `Gender` | `MALE`, `FEMALE`, `OTHER`, `UNKNOWN` |
| `Role` | `ADMIN`, `TEACHER` |
| `EmployeeStatus` | `ACTIVE`, `DISABLED` |
| `ClassStatus` | `PLANNED`, `ACTIVE`, `FINISHED` |
| `StudentStatus` | `ACTIVE`, `SUSPENDED`, `GRADUATED` |

### 2.3 关系与多重性

| 关系 | 多重性 | 语义 | 代码映射 |
|---|---|---|---|
| `Department — Employee` | 1 : 0..* | 一个部门可有零到多名员工；每名员工必须属于一个部门 | `Employee.departmentId` |
| `SchoolClass — Employee` | 0..* : 0..1 | 一个班级可有零或一位班主任；一位教师可担任零至多个班级的班主任 | `SchoolClass.headTeacherId` |
| `ClassCourse — Employee` | 0..* : 0..1 | 一项教学任务可有零或一位教师；一位教师可承担零至多项任务 | `ClassCourse.teacherId` |
| `SchoolClass ◆— ClassCourse` | 1 : 0..* | 教学任务属于班级；班级删除时级联删除教学任务 | `ClassCourse.classId` |
| `Course — ClassCourse` | 1 : 0..* | 一门课程可被零至多个班级选用；教学任务引用课程 | `ClassCourse.courseId` |
| `SchoolClass — Student` | 1 : 0..* | 班级可有零至多名当前在班学生；学生可转班 | `Student.classId` |
| `Student ◆— Score` | 1 : 0..* | 成绩属于学生；删除普通学生级联删除成绩，归档保护除外 | `Score.studentId` |
| `ClassCourse — Score` | 1 : 0..* | 成绩引用教学任务；有成绩时不得移除教学任务 | `Score.classCourseId` |

**约束：**

- `Student` 是 `Score` 的唯一组合拥有者；`ClassCourse` 不是组合拥有者，只被成绩引用。删除普通学生时由 `StudentService` 级联删除成绩；归档保护状态下禁止删除学生，因此不触发级联。
- `TeachingService.removeCourse` 必须先检查该 `ClassCourse` 是否已有成绩；有成绩则拒绝，避免留下悬空 `Score`。
- `Score.classCourseId` 创建后不可变；学生转班只改 `Student.classId`。
- `Score` 的业务唯一键是 `(studentId, courseId)`，`courseId` 经 `ClassCourse.courseId` 解析；同一学生同一课程最多一条成绩。
- 已有成绩属于其他 `ClassCourse`（转班后同课程）时，不新建成绩；当前用户能访问原 `ClassCourse` 时更新原记录并保留原 `classCourseId`，否则提示由原班级任课教师维护。
- `ClassCourse.teacherId` 为空表示“待分配”，此时不得录入新成绩。
- `SchoolClass.headTeacherId` 为空表示“无班主任”；班主任不授予权限。
- `ClassCourse.classId`、`ClassCourse.courseId` 创建后不可变。
- `ClassCourse` 端点角色名为 `teacher`；`SchoolClass` 端点角色名为 `headTeacher`；读取关联时以代码中的 ID 字段为准。

---

## 3. 服务、DTO 与持久化类图

为避免单张大图过宽、连线交叉，服务、DTO、持久化拆成三张图：

| 图层 | PlantUML 源 |
|---|---|
| 服务与会话/权限 | [`diagrams/class-service-iteration-2.puml`](diagrams/class-service-iteration-2.puml) |
| 统计与预警 DTO | [`diagrams/class-dto-iteration-2.puml`](diagrams/class-dto-iteration-2.puml) |
| 持久化与工具 | [`diagrams/class-persistence-iteration-2.puml`](diagrams/class-persistence-iteration-2.puml) |

### 3.1 结构概览

```text
UI ──> Service ──> Repository<T>（接口）
                 └──> Model / DTO
CsvRepository<T> ..|> Repository<T>
CsvRepository<T> ──> CsvCodec<T>
DataLoader ──> Repository<T> × 7
DataInitializer ──> Repository<T> × 7
```

### 3.2 会话与权限

#### `Session`

| 成员 | 说明 |
|---|---|
| `- currentUser: Employee` | 当前登录用户；未登录为 null |
| `+ login(Employee): void` | 建立会话 |
| `+ logout(): void` | 清除会话 |
| `+ currentUser(): Optional<Employee>` | 读取当前用户 |
| `+ isAdmin(): boolean` | 当前用户是 `ADMIN` |
| `+ isTeacher(): boolean` | 当前用户是 `TEACHER` |
| `+ requireAdmin(): void` | 非管理员抛 `BusinessException` |
| `+ requireTeacher(): void` | 非教师抛 `BusinessException` |
| `+ teacherId(): Long` | 教师 ID；管理员为 null |
| `+ canAccess(ClassCourse): boolean` | 管理员恒真；教师仅 `teacherId` 匹配 |
| `+ filterClassCourses(List<ClassCourse>): List<ClassCourse>` | 按当前用户过滤可见教学任务 |

> `Session` 同时保存当前用户并承担可见范围判断，不再单独设置 `AccessScope`。

### 3.3 服务类

| 服务 | 关键方法 | 主要依赖 |
|---|---|---|
| `AuthService` | `login`, `logout`, `changePassword`, `currentUser`, `session` | `Repository<Employee>`, `PasswordHasher`, `Session` |
| `DepartmentService` | `list`, `search`, `create`, `update`, `delete`, `employeeCount` | `Repository<Department>`, `Repository<Employee>` |
| `EmployeeService` | `search`, `create`, `update`, `delete`, `changeStatus`, `changeRole`, `resetPassword`, `findById`, `listTeachers` | 员工/部门/班级/教学任务仓储、`PasswordHasher`、`Session` |
| `CourseService` | `list`, `create`, `update`, `delete`（`list` 供教师只读；写操作要求管理员） | `Repository<Course>`, `Repository<ClassCourse>`, `Repository<Score>`, `Session` |
| `ClassService` | `list`, `filter`, `create`, `update`, `delete`, `changeStatus`, `assignHeadTeacher` | 班级/员工/学生/教学任务/成绩仓储 |
| `TeachingService` | `listByClass`, `addCourse`, `assignTeacher`, `removeCourse`, `listByTeacher` | 教学任务/班级/课程/员工/成绩仓储 |
| `StudentService` | `list`, `search`, `listByClass`, `create`, `update`, `delete`, `transfer`, `changeStatus` | 学生/班级/教学任务/成绩仓储、`Session` |
| `ScoreService` | `listByClassCourse`, `findByStudentAndCourse`, `record`, `update`, `delete`, `batchRecord`(Should) | 成绩/学生/教学任务/班级仓储、`Session` |
| `StatisticsService` | `classStatistics`, `courseStatistics`, `studentStatistics`, `classRanking`, `studentMetrics` | 学生/班级/教学任务/成绩/课程/员工仓储、`Session` |
| `WarningService` | `studentWarnings`, `classWarning`, `evaluateStudent` | `StatisticsService`, `Session` |

**方法级说明：**

- `AuthService.login(username, password)`：查找员工 → 校验哈希 → 校验 `ACTIVE` → `Session.login`。
- `EmployeeService.changeStatus(id, status)`：检查最后启用管理员、班主任/任课任务引用。
- `ClassService.changeStatus(id, target)`：只允许 `PLANNED → ACTIVE → FINISHED`；`FINISHED` 为归档终态。
- `StudentService.transfer(studentId, targetClassId)`：学生 `ACTIVE`、当前非 `FINISHED`、目标 `PLANNED`/`ACTIVE`、未满。
- `StudentService.delete(id)`：`FINISHED` 班级/历史归档成绩保护；否则先删成绩再删学生。
- `ScoreService.record(studentId, classCourseId, score)`：校验教师分配、班级状态、分数范围；新录入还要求学生的当前班级等于 `ClassCourse.classId`，已有成绩的修改不受学生当前班级限制。由 `classCourseId` 解析 `courseId`，按 `(studentId, courseId)` 查找已有成绩。同一 `ClassCourse` 则更新原记录与 `updatedAt`；其他 `ClassCourse` 不新建，能访问原 `ClassCourse` 时更新原记录并保留原 `classCourseId`，否则提示由原班级任课教师维护。
- `ScoreService.update(id, score)` / `delete(id)`：权限按成绩原 `classCourseId` 对应的 `ClassCourse` 校验，而不是按学生当前班级或新选班级校验。
- `CourseService.list()`：管理员与教师都可调用；`create`/`update`/`delete` 先执行 `Session.requireAdmin()`，教师只能通过 `UC-06a` 查看。
- `StatisticsService.classStatistics(classId)`：按 `Session` 的可见范围聚合；`studentCount` 统计全班学生，`scoreCount`/均分等只统计可见成绩；返回 `ClassStatistics`。可见范围无成绩时 `dataStatus = INSUFFICIENT`，指标为 `null`。
- `StatisticsService.studentMetrics(studentId)`：供 `WarningService` 使用；无成绩返回 `scoreCount=0`。
- `WarningService.evaluateStudent(studentId)`：评估 R1–R6，收集全部命中，取最高等级。

### 3.4 DTO

完整 PlantUML 源：[`diagrams/class-dto-iteration-2.puml`](diagrams/class-dto-iteration-2.puml)。

| 类型 | 字段 |
|---|---|
| `ClassStatistics` | `classId, className, studentCount, scoreCount, DataStatus dataStatus, Double average, Double passRate, Double highest, Double lowest, List<CourseStatistics> courses` |
| `CourseStatistics` | `classCourseId, courseCode, courseName, teacherName, scoreCount, DataStatus dataStatus, Double average, Double passRate, Double highest, Double lowest` |
| `StudentStatistics` | `studentId, studentNo, name, scoreCount, DataStatus dataStatus, Double average, Integer failedCount, Integer rank, String rankNote, List<ScoreDetail> details` |
| `ScoreDetail` | `classId, className, courseId, courseCode, courseName, double score, LocalDateTime updatedAt` |
| `RankEntry` | `rank, studentId, studentNo, name, double average, int failedCount` |
| `StudentMetrics` | `studentId, scoreCount, Double average, Integer failedCount` |
| `WarningResult` | `studentId, studentNo, name, WarningLevel level, Double average, Integer failedCount, List<String> hitRules` |
| `ClassWarning` | `classId, className, ClassWarningLevel level, Double passRate, Double average, scoreCount` |
| `WarningLevel`（enum） | `SEVERE, MODERATE, MILD, NORMAL, INSUFFICIENT_DATA` |
| `ClassWarningLevel`（enum） | `NEEDS_ATTENTION, NORMAL, INSUFFICIENT_DATA` |
| `DataStatus`（enum） | `AVAILABLE, INSUFFICIENT` |

**DTO 契约：**

- DTO 使用 Java `record`；所有 `List` 字段在紧凑构造器中执行 `List.copyOf`，空列表表示“没有明细/没有命中规则”，不是“数据不足”。
- `dataStatus == INSUFFICIENT` 时，平均分、及格率、最高分、最低分、`failedCount` 等指标为 `null`；UI 显示“数据不足”，不得把 `null` 当 0 打印。
- `rank == null` 时，`rankNote` 说明“无排名（本班暂无成绩）”或“数据不足”。
- `ClassStatistics.studentCount` 为全班学生数，不随教师可见课程范围缩小；`scoreCount` 与其余指标按可见范围计算。
- `ScoreDetail.updatedAt` 直接来自 `Score.updatedAt`，满足 FR-SCORE-07 的展示要求。
- `scoreCount == 0` 与 `dataStatus == INSUFFICIENT` 必须一致，由 `StatisticsService`/`WarningService` 保证。

### 3.5 持久化类

#### `Repository<T extends Identifiable>`（interface）

```java
List<T> findAll();
Optional<T> findById(long id);
T save(T entity);
void delete(long id);
void saveAll();
List<T> findAll(Predicate<T> filter);
Optional<T> findFirst(Predicate<T> filter);
```

#### `CsvCodec<T extends Identifiable>`（interface）

```java
String[] header();
T parse(String[] fields);
String[] format(T entity);
long idOf(T entity);
T withId(T entity, long id);
```

实现：`DepartmentCsvCodec`、`EmployeeCsvCodec`、`CourseCsvCodec`、`SchoolClassCsvCodec`、`ClassCourseCsvCodec`、`StudentCsvCodec`、`ScoreCsvCodec`。

#### `CsvRepository<T extends Identifiable>`（implements `Repository<T>`）

| 成员 | 说明 |
|---|---|
| `- file: Path` | CSV 路径 |
| `- codec: CsvCodec<T>` | 实体编解码 |
| `- store: Map<Long, T>` | `LinkedHashMap` 内存索引 |
| `- nextId: long` | 下一个可用 ID |
| `+ load(LoadDiagnostics): void` | 读取、解析、检查重复 ID |
| `+ findAll(): List<T>` | 不可变视图 |
| `+ findById(long): Optional<T>` | 按 ID 查询 |
| `+ save(T): T` | 分配 ID（如需要）并立即写文件 |
| `+ delete(long): void` | 删除并立即写文件 |
| `+ saveAll(): void` | 重写当前快照 |
| `+ removeInMemory(long): void` | 仅供 `DataLoader` 移除坏行，不写文件 |
| `- writeAtomically(): void` | 临时文件 + 原子替换 |

#### `DataLoader`

| 成员 | 说明 |
|---|---|
| `+ loadAll(): void` | 按依赖顺序加载 7 个仓储 |
| `- validateReferences(): void` | 校验非空/可空外键，移除坏行；成绩按 `(studentId, courseId)` 校验唯一性 |
| `- removeInvalid(...): void` | 从内存移除并记录诊断 |

#### `DataInitializer`

| 成员 | 说明 |
|---|---|
| `+ ensureLayout(Path): void` | 创建目录、缺失文件与表头 |
| `+ isFresh(): boolean` | 是否全新初始化 |
| `+ seedDemoData(): void` | 写入种子数据 |
| `+ ensureAdmin(): void` | 管理员兜底 |

#### `LoadDiagnostics`

| 成员 | 说明 |
|---|---|
| `+ add(String file, int line, String category, String summary): void` | 记录一条问题 |
| `+ writeTo(Path): void` | 覆盖写脱敏日志；失败只警告 |
| `+ printSummary(PrintStream): void` | 控制台汇总 |
| `+ hasIssues(): boolean` | 是否有问题 |
| `LoadIssue`（嵌套 record） | `file, line, category, summary` |

#### 工具

| 类 | 关键方法 |
|---|---|
| `PasswordHasher` | `generateSalt()`, `hash(password, salt)`, `verify(password, salt, expected)` |
| `CsvUtil` | `escape(String)`, `split(String)`, `join(String[])` |
| `Validators` | `username`, `password`, `phone`, `email`, `date`, `score`, `capacity` |
| `Dates` | `parseDate`, `formatDate`, `parseDateTime`, `formatDateTime` |

---

## 4. UI 类图（补充）

完整 PlantUML 源：[`diagrams/class-ui-iteration-2.puml`](diagrams/class-ui-iteration-2.puml)。

```text
Menu ◆── MenuItem ──> MenuAction
ConsoleIO <── Menu
LoginUI ──> AuthService
LoginUI ──> MenuFactory（接口）
AdminMenuFactory / TeacherMenuFactory ..|> MenuFactory
MenuFactory 实现 ──> 各模块 UI
模块 UI ──> 对应 Service
```

| 类 | 职责 | 关键方法 |
|---|---|---|
| `MenuAction`（interface） | 可执行菜单动作 | `execute()` |
| `MenuItem` | 菜单项：键、标签、动作 | `matches(String)`, `execute()` |
| `Menu` | 菜单循环与显示 | `addItem(...)`, `run(ConsoleIO)` |
| `ConsoleIO` | 输入输出、密码、确认 | `readLine`, `readPassword`, `readInt`, `readDouble`, `confirm`, `print` |
| `TablePrinter` | 表头、对齐、分页 | `printTable(...)`, `page(list, page, pageSize)`, `pageCount(total, pageSize)` |
| `LoginUI` | 登录/登出循环 | `run()` |
| `MenuFactory`（interface） | 菜单工厂契约 | `createMenu(Session)` |
| `AdminMenuFactory` | 装配管理员菜单树 | `createMenu(Session)` |
| `TeacherMenuFactory` | 装配教师菜单树 | `createMenu(Session)` |
| `DepartmentUI` | 部门交互 | `show()` |
| `EmployeeUI` | 员工交互 | `show()` |
| `CourseUI` | 课程交互；管理员 CRUD，教师只读 | `show()` |
| `ClassUI` | 班级交互 | `show()` |
| `TeachingUI` | 教学任务交互 | `show()` |
| `StudentUI` | 学生交互 | `show()` |
| `ScoreUI` | 成绩交互 | `show()` |
| `StatisticsUI` | 统计交互 | `show()` |
| `WarningUI` | 预警交互 | `show()` |
| `AccountUI` | 本人资料查看（UC-16，只读）与修改密码 | `show()`, `showProfile()` |

> UI 只调用 service，不导入 `persistence`；所有业务错误由 service 抛 `BusinessException`，UI 捕获并显示消息。
>
> **菜单工厂：** `AdminMenuFactory` 与 `TeacherMenuFactory` 是两棵独立的菜单树；组合根创建两个实现，`LoginUI` 按登录角色选择。角色判断只决定菜单外观，权限仍由 service 的 `Session` 校验，不能靠隐藏菜单实现安全。
>
> **分页规则：** 分页在 UI 层完成。service 返回已过滤、已排序的完整列表；`TablePrinter` 按 `page`/`pageSize` 切片，默认每页 10 条。过滤或搜索后页码重置为 1；第一页/最后一页继续翻页只提示、不越界；数据变化后当前页越界时夹到最后一页。

---

## 5. 用例 → 服务 → 仓储追溯

| 用例 | 负责服务方法 | 主要仓储 |
|---|---|---|
| UC-01 登录 | `AuthService.login` | `Employee` |
| UC-03 修改密码 | `AuthService.changePassword` | `Employee` |
| UC-04 管理部门 | `DepartmentService.*` | `Department`, `Employee` |
| UC-05 管理员工 | `EmployeeService.*` | `Employee`, `Department`, `SchoolClass`, `ClassCourse` |
| UC-06 管理课程 | `CourseService.create/update/delete` + 管理员查看 | `Course`, `ClassCourse`, `Score` |
| UC-06a 查看课程目录 | `CourseService.list`（教师只读） | `Course` |
| UC-07 管理班级 | `ClassService.*` | `SchoolClass`, `Employee`, `Student`, `ClassCourse`, `Score` |
| UC-08 管理教学任务 | `TeachingService.*` | `ClassCourse`, `SchoolClass`, `Course`, `Employee`, `Score` |
| UC-09 管理学生 | `StudentService.*` | `Student`, `SchoolClass`, `ClassCourse`, `Score` |
| UC-09a 学生转班 | `StudentService.transfer` | `Student`, `SchoolClass` |
| UC-10 管理成绩 | `ScoreService.*` | `Score`, `Student`, `ClassCourse`, `SchoolClass` |
| UC-11 班级统计 | `StatisticsService.classStatistics` | `Student`, `SchoolClass`, `ClassCourse`, `Score`, `Course`, `Employee` |
| UC-12 学生统计 | `StatisticsService.studentStatistics` | `Student`, `ClassCourse`, `Score`, `Course`, `SchoolClass` |
| UC-13 学习预警 | `WarningService.*` → `StatisticsService.studentMetrics` | `Score`, `ClassCourse`, `Student` |
| UC-14 我的教学 | `TeachingService.listByTeacher` | `ClassCourse`, `SchoolClass`, `Course`, `Employee` |
| UC-15 我的学生 | `StudentService.listByClass` + `Session` 可见范围 | `Student`, `ClassCourse` |
| UC-16 本人资料 | `EmployeeService.findById` | `Employee` |

---

## 6. 第 2 轮采纳、保留与遗留风险

### 6.1 采纳的设计变更

| # | 变更 | 依据 |
|---|---|---|
| 1 | `Score` 只由 `Student` 组合拥有；`ClassCourse—Score` 为普通关联 | GenAI 分析 3.1；生命周期语义 |
| 2 | 多端显式写 `0..*`，并在文档中说明阅读方向 | GenAI 分析 3.2；可读性 |
| 3 | 成绩唯一键统一为 `(studentId, courseId)` | GenAI 分析 3.3；需求 4.1、FR-STU-05 |
| 4 | 新增 `UC-06a 查看课程目录` | GenAI 分析 3.4；FR-COURSE-05 |
| 5 | DTO 增加 `DataStatus`、可空指标、`scoreCount`、`updatedAt` | GenAI 分析 3.7；FR-SCORE-07、FR-STAT-05 |
| 6 | `Repository`/`CsvCodec`/`CsvRepository` 约束 `T extends Identifiable` | GenAI 分析 3.11；类型契约 |
| 7 | `MenuFactory` 拆为接口 + 两个实现 | GenAI 分析 3.10；职责分离 |
| 8 | record 集合统一 `List.copyOf`，空列表与“数据不足”分离 | GenAI 分析 3.12 |

### 6.2 保留的设计

| 项目 | 保留理由 |
|---|---|
| `Session` 合并可见范围 | 角色只有两种；拆分增加传递依赖，不增加实际隔离 |
| UI 层分页 | 数据规模小；service 返回完整列表便于测试与复用 |
| `ClassStatistics.studentCount` 全班口径 | 教师可见范围只影响成绩指标 |
| `Person` 继承 | 共享字段与资料更新行为明确，继承层次浅 |
| `AbstractEntity` / `Identifiable` | 统一 ID、相等性和仓储约束 |
| `DataInitializer` 单类 | 一次性启动编排，拆分类不降低实际复杂度 |
| `StatisticsService` 单类 | 班级/课程/学生统计共享同一套可见范围与聚合口径 |
| 7 个 `CsvCodec` | 每实体独立解析，仓储不写实体分支；体现 Strategy |

### 6.3 遗留风险

| 风险 | 影响 | 处理 |
|---|---|---|
| 同一学生同一课程的历史成绩归属原 `ClassCourse` | 新班级任课教师可能看不到/不能改该成绩 | 按原 `ClassCourse` 权限处理；UI 提示归属；不新建重复成绩 |
| CSV 无跨文件事务 | 删除学生/班级时部分写入可能留下不一致 | 按依赖顺序保存；启动时引用校验并移除坏行 |
| 教师可见范围导致“数据不足”与管理员结果不同 | 同一学生两侧显示不同等级 | 需求已接受；DTO 用 `DataStatus` 明确表达 |
| `MenuFactory` 拆分后登录选择逻辑 | `LoginUI` 需要两个实现 | 组合根注入；按 `Session` 角色选择；权限仍在 service |
| `CourseUI` 同时服务管理员与教师 | 需要只读/可写两种呈现 | `Session` 控制入口；service 写操作 `requireAdmin()` |

> 第 1 轮的候选批判点已在本节按“采纳/保留”归档；完整逐条判断见 [`genai-design-analysis.md`](../../analysis/genai-design-analysis.md)。

---

## 7. 最终设计与后续

1. 本文件是 Iteration 2 最终设计，与 Iteration 1 分开保存；Iteration 1 已冻结，不被覆盖。
2. 本文档的采纳项来自 `docs/design/analysis/genai-design-analysis.md`（3.4），逐项对照见 `docs/design/analysis/design-evolution.md`（3.6）。
3. 学生确认本文件后，冻结为最终设计；实现阶段（Component 1.3）的类、方法、关系必须与本文档一致。
4. 如果实现阶段发现设计不可行，先修改本文档与演进说明，再改代码；不要只在代码里悄悄偏离。
5. 本文档不包含 3.7 Critical Reflection；该部分由学生本人撰写。

---

## 附录：图源文件

| 文件 | 内容 |
|---|---|
| [`diagrams/class-domain-iteration-2.puml`](diagrams/class-domain-iteration-2.puml) | 领域模型：实体、枚举、继承、关联、组合 |
| [`diagrams/class-service-iteration-2.puml`](diagrams/class-service-iteration-2.puml) | 服务、会话/权限、服务对仓储接口的依赖 |
| [`diagrams/class-dto-iteration-2.puml`](diagrams/class-dto-iteration-2.puml) | 统计与预警 DTO、`DataStatus` |
| [`diagrams/class-persistence-iteration-2.puml`](diagrams/class-persistence-iteration-2.puml) | 仓储、编解码、加载/初始化/诊断 |
| [`diagrams/class-ui-iteration-2.puml`](diagrams/class-ui-iteration-2.puml) | CUI 菜单、菜单工厂、输入输出、模块 UI |
