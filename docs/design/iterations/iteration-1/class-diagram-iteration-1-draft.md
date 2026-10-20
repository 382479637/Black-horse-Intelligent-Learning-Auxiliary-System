# 类图讨论稿 — Iteration 1 候选

| 项目 | 内容 |
|---|---|
| 文档版本 | v0.2（讨论稿；已按学生确认决策更新） |
| 日期 | 2026-10-19 |
| 课程 | COMP603/ENSE600 Program Design & Construction |
| 作业 | Project 1（40%），Component 1.2 Software Design |
| 需求基线 | `docs/requirements/requirements-analysis.md` v0.4 |
| 用例基线 | `docs/design/use-case/use-case-model.md` v0.1 |
| 图源 | `diagrams/class-domain-iteration-1.puml`、`diagrams/class-service-iteration-1.puml`、`diagrams/class-dto-iteration-1.puml`、`diagrams/class-persistence-iteration-1.puml`、`diagrams/class-ui-iteration-1.puml` |
| 状态 | 已确认内容固化为 [`class-diagram-iteration-1.md`](class-diagram-iteration-1.md)；本文件保留为讨论记录 |

> **流程说明：** 作业要求先有初版设计，再用 GenAI 批判并迭代。本文件是为了讨论而生成的 AI 辅助草案，不直接冒充学生的独立初版。建议你确认类、关系和方法后，用 PlantUML 或 PowerDesigner 重画/定稿为正式 Iteration 1；随后再做 3.4 的 GenAI 分析和 3.5 的 Iteration 2。
>
> 本文档不包含学生自撰的设计反思（3.7）。

### 已确认的设计决策

| 项目 | 决策 |
|---|---|
| 根包名 | `com.pdc.learning` |
| `MenuFactory` | Iteration 1 保持单类；Iteration 2 拆成 `AdminMenuFactory` / `TeacherMenuFactory`，作为演进证据 |
| `Session` / `AccessScope` | 合并为一个 `Session` 类，服务只依赖 `Session` |
| DTO | 统一使用 Java `record` |
| `ClassStatistics.studentCount` | 全班学生数，不受教师可见课程范围影响 |
| 列表分页 | UI 层分页；service 返回完整过滤/排序结果；`TablePrinter.page` 切片；每页 10 条 |

---

## 1. 建模约定

| 项目 | 约定 |
|---|---|
| 类名 | PascalCase；`SchoolClass` 代替 `Class`，避免与 `java.lang.Class` 冲突 |
| 属性 | `-` private；类型使用 Java 类型；ID 使用 `long`，可空外键使用 `Long` |
| 方法 | `+` public；只列职责相关方法，不机械列出所有 getter/setter |
| 关系 | 继承/实现、关联、组合；只在语义成立时使用聚合/组合 |
| 多重性 | 两端都标；文字解释见第 5 节 |
| 关联实现 | 领域对象之间主要保存 ID，不保存对象引用；由 service 解析，避免循环对象图 |
| 时间 | 日期 `LocalDate`，时间戳 `LocalDateTime` |

**包结构**见 [`architecture-design.md`](../../architecture/architecture-design.md) 第 2.3 节。

---

## 2. 领域模型类图

完整 PlantUML 源：[`diagrams/class-domain-iteration-1.puml`](diagrams/class-domain-iteration-1.puml)。

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

Department   1 ────── * Employee
SchoolClass  0..* ──── 0..1 Employee      （headTeacherId）
ClassCourse  0..* ──── 0..1 Employee      （teacherId）
SchoolClass  1 ◆────── * ClassCourse      （组合）
Course       1 ────── * ClassCourse
SchoolClass  1 ────── * Student           （当前班级）
Student      1 ◆────── * Score            （组合）
ClassCourse  1 ◆────── * Score            （组合）
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
| `- passwordHash: String` | PBKDF2 派生密钥 Base64 |
| `- passwordSalt: String` | 16 字节盐 Base64 |
| `- departmentId: long` | 所属部门 |
| `- role: Role` | `ADMIN` / `TEACHER` |
| `- status: EmployeeStatus` | `ACTIVE` / `DISABLED` |
| `- hireDate: LocalDate` | 入职日期 |
| `+ isAdmin(): boolean` | 角色判断 |
| `+ isActive(): boolean` | 状态判断 |
| `+ canTeach(): boolean` | `TEACHER && ACTIVE` |
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
| `Department — Employee` | 1 : * | 一个部门有多名员工；每名员工属于一个部门 | `Employee.departmentId` |
| `SchoolClass — Employee` | 0..* : 0..1 | 一个班级可有零或一位班主任；一位教师可担任零至多个班级的班主任 | `SchoolClass.headTeacherId` |
| `ClassCourse — Employee` | 0..* : 0..1 | 一项教学任务可有零或一位教师；一位教师可承担零至多项任务 | `ClassCourse.teacherId` |
| `SchoolClass ◆— ClassCourse` | 1 : * | 教学任务属于班级；班级删除时级联删除教学任务 | `ClassCourse.classId` |
| `Course — ClassCourse` | 1 : * | 一门课程可被多个班级选用；教学任务引用课程 | `ClassCourse.courseId` |
| `SchoolClass — Student` | 1 : * | 班级有当前在班学生；学生可转班 | `Student.classId` |
| `Student ◆— Score` | 1 : * | 成绩属于学生；删除学生级联删除成绩（归档保护除外） | `Score.studentId` |
| `ClassCourse ◆— Score` | 1 : * | 成绩属于教学任务；有成绩时不得移除教学任务 | `Score.classCourseId` |

**约束：**

- `Score.classCourseId` 创建后不可变；学生转班只改 `Student.classId`。
- `ClassCourse.teacherId` 为空表示“待分配”，此时不得录入新成绩。
- `SchoolClass.headTeacherId` 为空表示“无班主任”；班主任不授予权限。
- `ClassCourse.classId`、`ClassCourse.courseId` 创建后不可变。

---

## 3. 服务、DTO 与持久化类图

为避免单张大图过宽、连线交叉，服务、DTO、持久化拆成三张图：

| 图层 | PlantUML 源 |
|---|---|
| 服务与会话/权限 | [`diagrams/class-service-iteration-1.puml`](diagrams/class-service-iteration-1.puml) |
| 统计与预警 DTO | [`diagrams/class-dto-iteration-1.puml`](diagrams/class-dto-iteration-1.puml) |
| 持久化与工具 | [`diagrams/class-persistence-iteration-1.puml`](diagrams/class-persistence-iteration-1.puml) |

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
| `CourseService` | `list`, `create`, `update`, `delete` | `Repository<Course>`, `Repository<ClassCourse>`, `Repository<Score>` |
| `ClassService` | `list`, `filter`, `create`, `update`, `delete`, `changeStatus`, `assignHeadTeacher` | 班级/员工/学生/教学任务/成绩仓储 |
| `TeachingService` | `listByClass`, `addCourse`, `assignTeacher`, `removeCourse`, `listByTeacher` | 教学任务/班级/课程/员工/成绩仓储 |
| `StudentService` | `list`, `search`, `listByClass`, `create`, `update`, `delete`, `transfer`, `changeStatus` | 学生/班级/教学任务/成绩仓储、`Session` |
| `ScoreService` | `listByClassCourse`, `record`, `update`, `delete`, `batchRecord`(Should) | 成绩/学生/教学任务/班级仓储、`Session` |
| `StatisticsService` | `classStatistics`, `courseStatistics`, `studentStatistics`, `classRanking`, `studentMetrics` | 学生/班级/教学任务/成绩/课程/员工仓储、`Session` |
| `WarningService` | `studentWarnings`, `classWarning`, `evaluateStudent` | `StatisticsService`, `Session` |

**方法级说明：**

- `AuthService.login(username, password)`：查找员工 → 校验哈希 → 校验 `ACTIVE` → `Session.login`。
- `EmployeeService.changeStatus(id, status)`：检查最后启用管理员、班主任/任课任务引用。
- `ClassService.changeStatus(id, target)`：只允许 `PLANNED → ACTIVE → FINISHED`；`FINISHED` 为归档终态。
- `StudentService.transfer(studentId, targetClassId)`：学生 `ACTIVE`、当前非 `FINISHED`、目标 `PLANNED`/`ACTIVE`、未满。
- `StudentService.delete(id)`：`FINISHED` 班级/历史归档成绩保护；否则先删成绩再删学生。
- `ScoreService.record(studentId, classCourseId, score)`：校验学生当前班级、教师分配、班级状态、分数范围；重复则更新。
- `StatisticsService.classStatistics(classId)`：按 `Session` 的可见范围聚合；`studentCount` 统计全班学生，`scoreCount`/均分等只统计可见成绩；返回 `ClassStatistics`。
- `StatisticsService.studentMetrics(studentId)`：供 `WarningService` 使用；无成绩返回 `scoreCount=0`。
- `WarningService.evaluateStudent(studentId)`：评估 R1–R6，收集全部命中，取最高等级。

### 3.4 DTO

完整 PlantUML 源：[`diagrams/class-dto-iteration-1.puml`](diagrams/class-dto-iteration-1.puml)。

| 类型 | 字段 |
|---|---|
| `ClassStatistics` | `classId, className, studentCount, scoreCount, average, passRate, highest, lowest, List<CourseStatistics> courses` |
| `CourseStatistics` | `classCourseId, courseCode, courseName, teacherName, scoreCount, average, passRate, highest, lowest` |
| `StudentStatistics` | `studentId, studentNo, name, average, failedCount, Integer rank, String rankNote, List<ScoreDetail> details` |
| `ScoreDetail` | `classId, className, courseId, courseCode, courseName, score` |
| `RankEntry` | `rank, studentId, studentNo, name, average, failedCount` |
| `StudentMetrics` | `studentId, scoreCount, average, failedCount` |
| `WarningResult` | `studentId, studentNo, name, WarningLevel level, average, failedCount, List<String> hitRules` |
| `ClassWarning` | `classId, className, ClassWarningLevel level, passRate, average, scoreCount` |
| `WarningLevel`（enum） | `SEVERE, MODERATE, MILD, NORMAL, INSUFFICIENT_DATA` |
| `ClassWarningLevel`（enum） | `NEEDS_ATTENTION, NORMAL, INSUFFICIENT_DATA` |

> DTO 使用 Java `record`。`rank` 为 `null` 时，`rankNote` 说明“无排名（本班暂无成绩）”或“数据不足”。`ClassStatistics.studentCount` 为全班学生数，不随教师可见课程范围缩小。

### 3.5 持久化类

#### `Repository<T>`（interface）

```java
List<T> findAll();
Optional<T> findById(long id);
T save(T entity);
void delete(long id);
void saveAll();
List<T> findAll(Predicate<T> filter);
Optional<T> findFirst(Predicate<T> filter);
```

#### `CsvCodec<T>`（interface）

```java
String[] header();
T parse(String[] fields);
String[] format(T entity);
long idOf(T entity);
T withId(T entity, long id);
```

实现：`DepartmentCsvCodec`、`EmployeeCsvCodec`、`CourseCsvCodec`、`SchoolClassCsvCodec`、`ClassCourseCsvCodec`、`StudentCsvCodec`、`ScoreCsvCodec`。

#### `CsvRepository<T>`（implements `Repository<T>`）

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
| `- validateReferences(): void` | 校验非空/可空外键，移除坏行 |
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

完整 PlantUML 源：[`diagrams/class-ui-iteration-1.puml`](diagrams/class-ui-iteration-1.puml)。

```text
Menu ◆── MenuItem ──> MenuAction
ConsoleIO <── Menu
LoginUI ──> AuthService
MenuFactory ──> 各模块 UI
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
| `MenuFactory` | 装配管理员/教师菜单 | `createMenu(Session)` |
| `DepartmentUI` | 部门交互 | `show()` |
| `EmployeeUI` | 员工交互 | `show()` |
| `CourseUI` | 课程交互 | `show()` |
| `ClassUI` | 班级交互 | `show()` |
| `TeachingUI` | 教学任务交互 | `show()` |
| `StudentUI` | 学生交互 | `show()` |
| `ScoreUI` | 成绩交互 | `show()` |
| `StatisticsUI` | 统计交互 | `show()` |
| `WarningUI` | 预警交互 | `show()` |
| `AccountUI` | 修改密码/本人资料 | `show()` |

> UI 只调用 service，不导入 `persistence`；所有业务错误由 service 抛 `BusinessException`，UI 捕获并显示消息。
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
| UC-06 管理课程 | `CourseService.*` | `Course`, `ClassCourse`, `Score` |
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

## 6. 已知不确定点与候选批判点

以下不是最终结论，而是第 1 轮评审时值得让 GenAI 批判、由学生逐条判断的点。**不要把这些当作已采纳的设计理由。**

| # | 候选问题 | 可能的风险 |
|---|---|---|
| 1 | `MenuFactory` 同时装配管理员/教师菜单 | 可能变成上帝类；可拆 `AdminMenuFactory` / `TeacherMenuFactory` |
| 2 | `DataInitializer` 同时负责文件创建、种子数据、管理员兜底 | 职责过多；可拆 `DataFileLayout` + `SeedData` + `AdminFallback` |
| 3 | `StatisticsService` 同时负责班级、课程、学生统计和学生指标 | 类可能过大；可拆 `ClassStatisticsService` / `StudentStatisticsService` |
| 4 | `CsvRepository<T>` + 7 个 `CsvCodec` | 泛型仓储是否过度抽象；codec 样板代码是否太多 |
| 5 | `DataLoader` 负责跨文件引用校验 | 持久化层是否承担了本应属于 service 的完整性规则 |
| 6 | `Person` 继承 | Employee/Student 共享字段有限；是否应改用组合 |
| 7 | `AbstractEntity` + `Identifiable` | 是否只是“少写几行 ID”；是否属于装饰性抽象 |
| 8 | DTO 数量较多 | 是否可用更少的记录/嵌套类型；是否影响可读性 |
| 9 | `SchoolClass` 命名 | 与需求中的“班级”术语是否一致；是否需要别名说明 |
| 10 | `ClassCourse` 关联实体 | 是否应把任课教师直接放在 `SchoolClass` 或 `Course` 上；当前设计是否过度 |

> 已确认的 `Session`/`AccessScope` 合并与 UI 层分页不再列入候选批判点；若 GenAI 仍提出，按已确认决策记录“拒绝并说明”。

> 下一步：学生确认/重画正式 Iteration 1 后，用 GenAI 对**正式版本**做批判，逐条记录“建议 → 采纳/拒绝 → 理由”。本表只用于提示评审方向，不代替 3.4 的分析表或 3.7 的反思。

---

## 7. 与正式 Iteration 1 的关系

1. 本稿的类、属性、方法、关系与已确认决策已固化到 [`class-diagram-iteration-1.md`](class-diagram-iteration-1.md)。
2. 正式 Iteration 1 冻结后不再修改；后续修订进入 Iteration 2。
3. 正式 Iteration 1 交由 GenAI 批判，结论记录到 `docs/design/analysis/genai-design-analysis.md`（3.4），再形成 Iteration 2。
4. 本文件保留为讨论记录，不作为报告中的“正式第 1 轮”提交。

---

## 附录：图源文件

| 文件 | 内容 |
|---|---|
| [`diagrams/class-domain-iteration-1.puml`](diagrams/class-domain-iteration-1.puml) | 领域模型：实体、枚举、继承、关联、组合 |
| [`diagrams/class-service-iteration-1.puml`](diagrams/class-service-iteration-1.puml) | 服务、会话/权限、服务对仓储接口的依赖 |
| [`diagrams/class-dto-iteration-1.puml`](diagrams/class-dto-iteration-1.puml) | 统计与预警 DTO、枚举 |
| [`diagrams/class-persistence-iteration-1.puml`](diagrams/class-persistence-iteration-1.puml) | 仓储、编解码、加载/初始化/诊断 |
| [`diagrams/class-ui-iteration-1.puml`](diagrams/class-ui-iteration-1.puml) | CUI 菜单、输入输出、模块 UI |
