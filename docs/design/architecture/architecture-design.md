# 黑马智能学习辅助系统 — 架构设计

| 项目 | 内容 |
|---|---|
| 文档版本 | v0.5（吸收两轮设计评审结论） |
| 日期 | 2026-10-20 |
| 课程 | COMP603/ENSE600 Program Design & Construction |
| 作业 | Project 1（40%），Component 1.2 Software Design |
| 需求基线 | `docs/requirements/requirements-analysis.md` v0.4 |
| 对应报告 | 3.2 范围/约束/假设、3.3 与 3.5 类图的支撑说明 |
| 图源 | `docs/design/architecture/architecture.puml` |
| 状态 | 与已冻结的 Iteration 2 最终设计一致；见 `docs/design/iterations/iteration-2/class-diagram-iteration-2-final.md` |
| 评审输入 | [`codex_review/design-review.md`](../../../codex_review/design-review.md)、[`codex_review/design-review-iteration-2.md`](../../../codex_review/design-review-iteration-2.md) |

> 本文档描述系统架构，不包含学生自撰的设计反思（报告 3.7）。该反思由学生本人独立撰写。

---

## 1. 设计目标与边界

### 1.1 设计目标

1. 用清晰的四层结构实现需求中的教务管理、成绩、统计与预警闭环。
2. 让业务规则集中在 service/model，UI 只负责输入输出，持久化只负责 CSV 读写。
3. 用接口、抽象类和具体类形成真实的 OOP 层次，而不是装饰性声明。
4. 保持依赖方向单向，使 Project 2 替换 CUI 或 CSV 时不需要重写业务层。
5. 在没有数据库、没有外部依赖、单进程运行的条件下，保证构建、运行和排错过程可重复。

### 1.2 范围（对应报告 3.2）

**范围内：**

- 管理员/教师登录、登出、修改密码、按角色生成菜单。
- 部门、员工、课程、班级、教学任务、学生的管理。
- 成绩录入、修改、删除、查询；教师限本人任课 ClassCourse。
- 班级统计、学生统计、班级排名、规则驱动的学习预警。
- CSV 持久化、全新初始化、部分初始化修复、加载诊断。
- 班级状态转换与 `FINISHED` 归档保护。

**范围外：**

- GUI、Apache Derby、JUnit、强制 Git（Project 2 内容）。
- 真实大模型、机器学习、聊天机器人、自动推荐。
- 考勤、作业、考试安排、课表、文件上传、邮件/短信。
- 多用户并发、网络部署、学生端登录。
- 批量结业/归档、数据备份恢复界面、国际化。

### 1.3 约束与假设

| 类别 | 设计约束 |
|---|---|
| 语言/构建 | Java 21、Maven 3.9+、标准目录结构；`maven.compiler.release=21` |
| 界面 | 控制台 CUI；不使用 GUI 框架 |
| 持久化 | 仅 JDK File I/O；7 个 UTF-8 CSV 文件位于项目相对路径 `data/` |
| 数据库 | Project 1 不引入；持久化通过接口隔离，Project 2 再替换实现 |
| 外部依赖 | 优先 JDK 标准库；不引入需要手工配置的库 |
| 运行模型 | 单机、单进程、单用户；不考虑并发写冲突 |
| 数据规模 | ≤1,000 学生、≤100 班级、≤50 课程、≤20,000 条成绩 |
| 编码 | 源码与数据 UTF-8；控制台使用终端字符集 |
| 安全 | 密码 PBKDF2 加盐哈希、不明文存储、输入不回显；加载日志脱敏 |
| 一致性 | 单文件临时文件 + 原子替换；不承诺数据库级 ACID；启动时校验引用 |

**关键假设：**

- 评分环境具备 JDK 21+ 与 Maven，可执行标准构建命令。
- 演示数据为合成数据，不涉及真实个人信息。
- 教师权限由 ClassCourse 的 `teacherId` 决定，班主任身份不授予权限。
- `Score` 绑定不可变的 `classCourseId`，学生转班不迁移历史成绩。
- 班级状态由管理员手工维护，日期只用于展示。

### 1.4 设计原则

- **单一职责：** UI、业务编排、领域规则、文件格式各归其位。
- **依赖倒置：** service 依赖 `Repository<T extends Identifiable>` 接口，不依赖 `CsvRepository<T extends Identifiable>`。
- **显式装配：** 依赖通过构造器注入，由 `ApplicationBootstrap` 统一创建；不使用全局单例。
- **领域优先：** 能在实体内部表达的规则（分数范围、容量判断、状态判断）放在 model；跨实体规则放在 service。
- **可诊断：** 加载失败、坏行、引用缺失都产生明确诊断，不静默丢数据。

---

## 2. 分层架构

### 2.1 层与依赖

| 层 | 包 | 职责 | 允许依赖 | 禁止 |
|---|---|---|---|---|
| Bootstrap | `app` | 创建对象、装配依赖、启动登录、退出保存 | 所有层 | 承载业务规则 |
| Presentation | `ui` | 菜单、输入、输出、格式校验、错误呈现 | `service`、`model`（只读）、`util` | 直接 File I/O、直接实现业务规则 |
| Application | `service` | 用例编排、权限、状态与引用校验、统计/预警计算 | `model`、`persistence` 接口、`util`、`dto` | 依赖具体 UI、解析 CSV 格式 |
| Domain | `model` | 实体、枚举、值对象、实体内部规则 | JDK 基础类型 | 依赖 UI、持久化实现、service |
| Persistence | `persistence` | 仓储契约、CSV 编解码、加载/保存、诊断 | `model`、`util` | 依赖 UI、承载业务规则 |
| DTO | `dto` | service 返回的统计/预警结果 | `model` 枚举、JDK | 承载持久化或 UI 逻辑 |
| Utility | `util` | 密码哈希、CSV 转义、输入校验、日期格式 | JDK | 依赖上层 |

**依赖方向：**

```text
app ──> ui ──> service ──> model
                 │
                 ├──> persistence（接口）
                 └──> util
persistence 实现 ──> model、util
service ──> dto
```

- `ui` 不导入 `persistence` 包。
- `model` 不导入 `service`、`persistence`、`ui`。
- `service` 只导入 `persistence` 中的 `Repository<T extends Identifiable>` 等接口，不导入 `CsvRepository<T extends Identifiable>`。
- `app` 是唯一同时看到接口与具体实现的组合根。

### 2.2 架构图

完整 PlantUML 源见 `docs/design/architecture/architecture.puml`。结构如下：

```text
┌─────────────────────────────────────────────────────────────┐
│ app: Main / ApplicationBootstrap                            │
└──────────────────────────────┬──────────────────────────────┘
                               │ 创建并装配
┌──────────────────────────────▼──────────────────────────────┐
│ ui: ConsoleIO / Menu / MenuItem / LoginUI / 模块 UI          │
└──────────────────────────────┬──────────────────────────────┘
                               │ 调用用例
┌──────────────────────────────▼──────────────────────────────┐
│ service: Auth / Department / Employee / Course / Class /    │
│          Teaching / Student / Score / Statistics / Warning  │
│          Session（含可见范围）                              │
└───────────────┬──────────────────────────────┬──────────────┘
                │ 读写领域对象                  │ 通过接口持久化
┌───────────────▼──────────────┐  ┌────────────▼──────────────┐
│ model: Person / Employee /   │  │ persistence: Repository / │
│ Student / Department / ...   │  │ CsvCodec / CsvRepository  │
└──────────────────────────────┘  └────────────┬──────────────┘
                                               │
                                  ┌────────────▼──────────────┐
                                  │ util: PasswordHasher /    │
                                  │ CsvUtil / Validators      │
                                  └───────────────────────────┘
```

### 2.3 包结构

```text
com.pdc.learning
├── app
│   ├── Main.java
│   └── ApplicationBootstrap.java
├── ui
│   ├── ConsoleIO.java
│   ├── TablePrinter.java
│   ├── LoginUI.java
│   ├── MenuFactory.java（接口）
│   ├── AdminMenuFactory.java
│   ├── TeacherMenuFactory.java
│   ├── menu
│   │   ├── Menu.java
│   │   ├── MenuItem.java
│   │   └── MenuAction.java
│   └── view
│       ├── DepartmentUI.java
│       ├── EmployeeUI.java
│       ├── CourseUI.java
│       ├── ClassUI.java
│       ├── TeachingUI.java
│       ├── StudentUI.java
│       ├── ScoreUI.java
│       ├── StatisticsUI.java
│       ├── WarningUI.java
│       └── AccountUI.java
├── service
│   ├── AuthService.java
│   ├── DepartmentService.java
│   ├── EmployeeService.java
│   ├── CourseService.java
│   ├── ClassService.java
│   ├── TeachingService.java
│   ├── StudentService.java
│   ├── ScoreService.java
│   ├── StatisticsService.java
│   ├── WarningService.java
│   └── Session.java
├── dto
│   ├── ClassStatistics.java
│   ├── CourseStatistics.java
│   ├── StudentStatistics.java
│   ├── ScoreDetail.java
│   ├── RankEntry.java
│   ├── StudentMetrics.java
│   ├── WarningResult.java
│   ├── ClassWarning.java
│   ├── WarningLevel.java
│   ├── ClassWarningLevel.java
│   └── DataStatus.java
├── model
│   ├── Identifiable.java
│   ├── AbstractEntity.java
│   ├── Person.java
│   ├── Employee.java
│   ├── Student.java
│   ├── Department.java
│   ├── Course.java
│   ├── SchoolClass.java
│   ├── ClassCourse.java
│   ├── Score.java
│   └── enums
│       ├── Gender.java
│       ├── Role.java
│       ├── EmployeeStatus.java
│       ├── ClassStatus.java
│       └── StudentStatus.java
├── persistence
│   ├── Repository.java
│   ├── CsvCodec.java
│   ├── LoadDiagnostics.java
│   ├── DataLoader.java
│   ├── DataInitializer.java
│   └── csv
│       ├── CsvRepository.java
│       ├── DepartmentCsvCodec.java
│       ├── EmployeeCsvCodec.java
│       ├── CourseCsvCodec.java
│       ├── SchoolClassCsvCodec.java
│       ├── ClassCourseCsvCodec.java
│       ├── StudentCsvCodec.java
│       └── ScoreCsvCodec.java
├── util
│   ├── PasswordHasher.java
│   ├── CsvUtil.java
│   ├── Validators.java
│   └── Dates.java
└── exception
    ├── BusinessException.java
    └── DataAccessException.java
```

> 包名 `com.pdc.learning` 是占位；实现前可按学号/姓名调整，但不要在类图中散落多个根包。

---

## 3. 运行时装配与主流程

### 3.1 启动流程

1. `Main` 调用 `ApplicationBootstrap.start()`。
2. 确定项目相对路径 `data/`，由 `DataInitializer` 判定初始化场景：
   - 7 个核心文件全部缺失/为空 → 全新初始化；
   - 至少一个文件有数据 → 部分初始化/正常加载。
3. `DataInitializer.ensureLayout()` 创建缺失的目录和文件表头；已有文件不覆盖。
4. 创建 7 个 `CsvCodec` 和 7 个 `CsvRepository`，再创建 `LoadDiagnostics` 与 `DataLoader`。
5. `DataLoader.loadAll()` 按 `departments → employees → courses → classes → class_courses → students → scores` 顺序加载，校验 ID、必填字段和引用；坏行跳过并记入诊断，不中断启动。
6. 如果是全新初始化，`DataInitializer.seedDemoData()` 写入演示数据。
7. 如果加载后没有可用管理员，`DataInitializer.ensureAdmin()` 按需求 5.4.1 创建默认管理员。
8. `LoadDiagnostics.writeTo(data/load-errors.log)` 覆盖写入本次启动诊断；写入失败只警告，不阻断。
9. 创建 `PasswordHasher`、`Session`、各 `Service`、`ConsoleIO`、各 UI 与 `MenuFactory` 的两个实现。
10. `LoginUI.run()` 进入登录循环；登录成功后按 `Session` 角色选择 `AdminMenuFactory` 或 `TeacherMenuFactory`，生成对应菜单。
11. 正常退出或 `Q` 退出时，`ApplicationBootstrap.shutdown()` 调用各仓储 `saveAll()` 作为兜底，再写一次诊断摘要。

### 3.2 登录与会话

```text
LoginUI
  → AuthService.login(username, password)
      → Repository<Employee>.findFirst(username)
      → PasswordHasher.verify(raw, salt, hash)
      → Employee.status == ACTIVE
      → Session.login(employee)
  → AdminMenuFactory / TeacherMenuFactory.createMenu(session)
```

- 用户名不存在与密码错误统一提示，不泄露账号是否存在。
- 账号存在且密码正确但状态为 `DISABLED` 时，提示账号禁用。
- `Session` 保存当前 `Employee`，并承担角色判断与可见范围判断（`isAdmin`、`isTeacher`、`teacherId`、`canAccess`、`filterClassCourses`）。
- 所有 service 在需要权限时调用 `Session.requireAdmin()`、`Session.isTeacher()` 或 `Session.canAccess(...)`，不依赖 UI 是否隐藏菜单。

### 3.3 退出与保存

- `0` 只退出登录，回到登录界面，不结束进程。
- `Q` 在菜单/命令提示中安全退出；数据字段中的 `Q` 按普通文本处理。
- 退出前调用 `saveAll()`，避免内存中的最后修改因异常路径丢失。
- 每个 `save`/`delete` 已经立即写文件，`saveAll()` 是兜底而非唯一保存点。

### 3.4 典型业务流

**成绩录入：**

```text
ScoreUI
  → 选择班级、ClassCourse
  → ScoreService.listByClassCourse(classCourseId)
  → 输入学号与分数
  → ScoreService.record(studentId, classCourseId, score)
      → Session 校验目标 ClassCourse 的教师权限
      → 学生存在；新录入时当前班级 = ClassCourse.classId，
        已有成绩的修改不受学生当前班级限制
      → ClassCourse.teacherId 非空
      → SchoolClass.status ∈ {ACTIVE, FINISHED}
      → 0 ≤ score ≤ 100
      → 由 classCourseId 解析 courseId
      → 按 (studentId, courseId) 查找已有成绩
          → 同一 ClassCourse：更新原记录与 updatedAt
          → 其他 ClassCourse（转班后同课程）：不新建；
            能访问原 ClassCourse 时更新原记录并保留原 classCourseId，
            否则提示由原班级任课教师维护
  → Repository<Score>.save(score)
```

**学生转班：**

```text
StudentService.transfer(studentId, targetClassId)
  → 学生存在且 status = ACTIVE
  → 当前班级 status ≠ FINISHED
  → 目标班级存在且 status ∈ {PLANNED, ACTIVE}
  → 目标班级未满
  → Student.transferTo(targetClassId)
  → Repository<Student>.save(student)
```

**统计与预警：**

```text
StatisticsUI / WarningUI
  → StatisticsService / WarningService
      → Session.filterClassCourses(...) 得到可见 ClassCourse
      → 聚合可见成绩
      → 无成绩 → 数据不足
      → 有成绩 → 计算平均分、及格率、排名或规则命中
  → 返回 DTO，由 UI 打印
```

---

## 4. 模块与类职责

### 4.1 领域模型

| 类 | 职责 | 关键规则 |
|---|---|---|
| `Identifiable` | 统一 ID 契约 | `getId()` |
| `AbstractEntity` | ID、相等性、基础状态 | `id` 由仓储分配；按 ID 比较 |
| `Person` | 人员公共属性 | 姓名、性别、手机号 |
| `Employee` | 员工/教师/账号；包含可选邮箱资料 | `isAdmin()`、`isActive()`、`canTeach()`、`getEmail()`、`updateEmail()` |
| `Student` | 学生学籍 | `isActive()`、`transferTo()` |
| `Department` | 部门 | 名称唯一（service 校验） |
| `Course` | 课程目录 | 课程编号唯一（service 校验） |
| `SchoolClass` | 班级 | `isFull()`、`canAcceptStudent()`、`isFinished()` |
| `ClassCourse` | 班级-课程-任课教师关联实体 | `teacherId` 可空；`isAssigned()` |
| `Score` | 成绩 | 0–100；`isPassed()`；`updatedAt` |

> 类名使用 `SchoolClass`，避免与 `java.lang.Class` 冲突；UI/文档中仍显示“班级”。
>
> 领域属性均为 `private`，service、UI 与 CSV codec 通过 getter 读取（如 `Employee.getEmail()`），不直接访问字段；类目录只列职责相关方法，不逐条列出常规 getter。

### 4.2 服务

| 服务 | 主要职责 | 依赖 |
|---|---|---|
| `AuthService` | 登录、登出、修改自己的密码 | `Repository<Employee>`、`PasswordHasher`、`Session` |
| `DepartmentService` | 部门 CRUD、搜索、员工数 | `Repository<Department>`、`Repository<Employee>` |
| `EmployeeService` | 员工 CRUD（含可选邮箱资料）、启停、角色、重置密码 | 员工/部门/班级/教学任务仓储、`PasswordHasher`、`Session` |
| `CourseService` | 课程 CRUD、删除引用检查；`list` 供教师只读 | `Repository<Course>`、`Repository<ClassCourse>`、`Repository<Score>`、`Session` |
| `ClassService` | 班级 CRUD、班主任、状态转换、删除检查 | 班级/员工/学生/教学任务/成绩仓储 |
| `TeachingService` | 教学任务增删、任课教师分配 | 教学任务/班级/课程/员工/成绩仓储 |
| `StudentService` | 学生 CRUD、转班、状态 | 学生/班级/教学任务/成绩仓储、`Session` |
| `ScoreService` | 成绩查询、录入/修改、删除、批量（Should）；按 `(studentId, courseId)` 判重 | 成绩/学生/教学任务/班级仓储、`Session` |
| `StatisticsService` | 班级/课程/学生统计、排名、学生指标 | 学生/班级/教学任务/成绩/课程/员工仓储、`Session` |
| `WarningService` | 学生/班级预警规则计算 | `StatisticsService`、`Session` |
| `Session` | 当前登录用户、角色判断、管理员/教师可见范围 | `Employee`、`ClassCourse` |

**职责边界：**

- service 负责跨实体校验、权限、状态转换、引用完整性。
- model 负责实体内部不依赖其他实体的规则（分数范围、容量判断、状态判断）。
- UI 不直接调用 repository，不直接修改实体；所有变更经 service。

**DTO 结果契约（Iteration 2）：**

- 统计/预警 DTO 使用 `DataStatus { AVAILABLE, INSUFFICIENT }` 显式表达“数据不足”，不只依赖 `scoreCount == 0`。
- 无可见成绩时，平均分、及格率、最高分、最低分、`failedCount` 等指标为 `null`；UI 显示“数据不足”，不把 `null` 当 0。
- 所有 `List` 字段在 record 紧凑构造器中 `List.copyOf`；空列表表示“没有明细”，不是“数据不足”。
- `ScoreDetail.updatedAt` 来自 `Score.updatedAt`，用于满足 FR-SCORE-07 的展示要求。

### 4.3 持久化

| 类 | 职责 |
|---|---|
| `Repository<T extends Identifiable>` | 泛型仓储契约：查询、保存、删除、全量保存、谓词查询；类型参数必须有 ID |
| `CsvCodec<T extends Identifiable>` | 单个实体的 CSV 表头、解析、格式化、ID 读写 |
| `CsvRepository<T extends Identifiable>` | 通用 CSV 仓储实现：内存索引、加载、原子写入、ID 分配 |
| `DataLoader` | 按依赖顺序加载 7 个文件，校验引用，移除坏行（内存中） |
| `DataInitializer` | 目录/文件/表头创建、全新初始化种子数据、管理员兜底 |
| `LoadDiagnostics` | 收集坏行、输出控制台摘要、覆盖写入脱敏日志 |

### 4.4 UI

| 类 | 职责 |
|---|---|
| `ConsoleIO` | 读行、读密码、读整数/小数、确认、输出 |
| `Menu` / `MenuItem` / `MenuAction` | 菜单循环、菜单项、可执行动作（Command/Strategy） |
| `LoginUI` | 登录/登出循环，登录成功后进入角色菜单 |
| `MenuFactory`（interface） | `createMenu(Session)` 契约；实现按角色装配菜单树 |
| `AdminMenuFactory` | 管理员菜单树：基础数据、教学管理、成绩、统计与预警 |
| `TeacherMenuFactory` | 教师菜单树：我的教学（含课程目录）、成绩、统计与预警、我的资料 |
| `DepartmentUI` 等模块 UI | 单个模块的菜单、输入、调用 service、打印结果 |
| `TablePrinter` | 表头、对齐输出、UI 层分页（`page`/`pageCount`） |

### 4.5 工具与异常

| 类 | 职责 |
|---|---|
| `PasswordHasher` | PBKDF2WithHmacSHA256、盐生成、哈希、恒定时间比较 |
| `CsvUtil` | CSV 引号转义、字段拆分、空值处理 |
| `Validators` | 用户名、密码、手机号、邮箱、日期、分数、容量等输入校验 |
| `Dates` | `yyyy-MM-dd` 与 `yyyy-MM-dd'T'HH:mm:ss` 解析/格式化 |
| `BusinessException` | 可预期业务错误；UI 显示消息，不打印栈 |
| `DataAccessException` | 文件 I/O 错误；启动阶段安全退出，运行阶段提示并可重试 |

---

## 5. 持久化设计

### 5.1 文件与格式

```text
data/
├── departments.csv
├── employees.csv
├── courses.csv
├── classes.csv
├── class_courses.csv
├── students.csv
├── scores.csv
└── load-errors.log      # 诊断文件，不参与加载；每次启动覆盖
```

- 每行一条记录，首行表头，UTF-8。
- 字段分隔符为逗号，特殊字符按双引号转义；空字段表示空值。
- 枚举存枚举名（如 `MALE`、`ACTIVE`、`FINISHED`）。
- 日期 `yyyy-MM-dd`；时间戳 `yyyy-MM-dd'T'HH:mm:ss`。
- `Class.headTeacherId`、`ClassCourse.teacherId` 可空；空字段分别表示“无班主任”“待分配”。
- `scores.csv` 字段为 `id,studentId,classCourseId,score,updatedAt`，不含 `classId`/`courseId`。
- ID 为内部 `long`；加载时按最大 ID 恢复自增，删除后不复用。

### 5.2 Repository 契约

```java
public interface Repository<T extends Identifiable> {
    List<T> findAll();
    Optional<T> findById(long id);
    T save(T entity);          // id == 0 时分配新 ID；保存后立即写文件
    void delete(long id);      // 删除后立即写文件
    void saveAll();            // 重写当前内存快照
    List<T> findAll(Predicate<T> filter);
    Optional<T> findFirst(Predicate<T> filter);
}
```

- service 只依赖该接口。
- `CsvRepository<T extends Identifiable>` 额外提供 `load(LoadDiagnostics)` 与 `removeInMemory(long)`；后者仅供 `DataLoader` 在启动校验阶段移除坏行，不触发写文件。
- `Identifiable` 约束保证仓储的 ID 分配、`CsvCodec.idOf/withId` 与实体契约在类型层面一致，避免泛型仓储接受无 ID 类型。
- `findAll()` 返回不可变视图，避免 UI 直接修改仓储内部集合。

### 5.3 加载与重建

1. `CsvRepository.load()` 读取文件；文件不存在或只有表头时为空仓储。
2. 每行交给 `CsvCodec.parse()`；解析失败、字段缺失、枚举非法 → 记录坏行并跳过。
3. 重复 ID → 保留首条，后续记录坏行并跳过。
4. `DataLoader` 按依赖顺序加载后校验引用：
   - `Employee.departmentId` → `Department`；
   - `SchoolClass.headTeacherId` → `Employee`（可空，缺失置空）；
   - `ClassCourse.classId` → `SchoolClass`；`courseId` → `Course`；`teacherId` → `Employee`（可空，缺失置空）；
   - `Student.classId` → `SchoolClass`；
   - `Score.studentId` → `Student`；`classCourseId` → `ClassCourse`。
5. 非空外键缺失 → 移除该行并记录；可空外键缺失 → 置空并记录；依赖被移除父行的子行同样移除。
6. 成绩唯一性校验：`Score.classCourseId` 解析出 `courseId` 后，按 `(studentId, courseId)` 检查；重复时保留首条，后续记录坏行并跳过。这样可发现 CSV 中同一学生同一课程挂在两个 `ClassCourse` 下的脏数据。
7. 加载完成后，`DataInitializer` 处理种子数据/管理员兜底；`LoadDiagnostics` 写日志。

### 5.4 保存与一致性

- 单文件：先写同目录临时文件，再 `Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING)`；不支持原子移动时退化为替换并记录警告。
- 多文件：不承诺 ACID。service 按依赖顺序调用保存，失败时向 UI 报告；启动时通过引用校验发现不一致。
- 关键操作保存顺序：

| 操作 | 顺序 |
|---|---|
| 删除学生 | 先删 `Score`，再删 `Student` |
| 删除班级 | 先确认无学生/无成绩；再删 `ClassCourse`，最后删 `SchoolClass` |
| 删除课程 | 确认无 `ClassCourse`/`Score` 引用后删 `Course` |
| 删除部门 | 确认无 `Employee` 后删 `Department` |
| 删除员工 | 确认无班主任/任课任务后删 `Employee` |
| 转班 | 只更新 `Student` |
| 录入/修改/删除成绩 | 只更新 `Score` |
| 增删教学任务 | 只更新 `ClassCourse` |

- 应用退出时调用所有仓储 `saveAll()` 作为兜底。

### 5.5 初始化

- `DataInitializer` 在创建仓储前判定“全新/部分”场景，避免把已有文件误判为空。
- 全新：创建 7 个文件并写入演示数据；种子班级直接为 `ACTIVE`；待分配任务不生成成绩。
- 部分：只补缺失文件和表头，不写演示业务数据；已有文件不覆盖。
- 管理员兜底：优先复用“教务部”，否则新建；用户名 `admin` 冲突时依次尝试 `admin2`、`admin3`；固定密码 `Admin@123`；不修改已有员工。
- 员工文件无法解析且无法安全追加 → 明确报错并安全退出，不静默继续。

### 5.6 诊断日志

- 每次启动覆盖 `data/load-errors.log`。
- 只写时间戳、文件名、行号、错误类别、安全摘要。
- 不写整行、密码、哈希、盐、手机号、邮箱。
- 日志创建/写入失败 → 控制台警告并继续加载，不阻断启动。
- 控制台始终输出各文件加载/跳过数量，日志只是补充。

---

## 6. 业务规则归属

| 规则 | 归属 | 说明 |
|---|---|---|
| 分数 0–100 | `Score.updateScore()` + `ScoreService` | 实体防御性校验，service 统一提示 |
| 班级容量 | `SchoolClass.isFull()` + `StudentService` / `ClassService` | 新增/转入/改容量 |
| 班级状态转换 | `ClassService.changeStatus()` | 只允许 `PLANNED → ACTIVE → FINISHED` |
| 员工状态转换 | `EmployeeService.changeStatus()` | 最后管理员、班主任/任课任务检查 |
| 学生状态转换 | `StudentService.changeStatus()` | `FINISHED` 班级只能保持或 `GRADUATED` |
| 转班条件 | `StudentService.transfer()` | 学生 `ACTIVE`、目标 `PLANNED`/`ACTIVE`、未满 |
| 删除引用检查 | 各 service | 部门/员工/课程/班级/学生/成绩 |
| 成绩录入条件 | `ScoreService.record()` | 学生当前班级匹配、教师已分配、班级非 `PLANNED` |
| 教师可见范围 | `Session` | 管理员全部；教师仅本人 `teacherId` 的 ClassCourse |
| 统计口径 | `StatisticsService` | 按可见范围聚合；转班不迁移历史成绩 |
| 班级学生数 | `StatisticsService` | `ClassStatistics.studentCount` 为全班学生数，不受教师可见课程范围影响 |
| 预警规则 | `WarningService` | 评估全部规则、收集命中、取最高等级 |
| CSV 格式 | `CsvCodec` / `CsvRepository` | 不把文件格式泄漏到 service |
| 输入格式 | `ConsoleIO` / `Validators` | UI 层做格式校验，service 做业务校验 |

---

## 7. OOP 与 SOLID 落点

### 7.1 四类 OOP 要素

| 要素 | 落点 |
|---|---|
| 抽象 | `Repository<T extends Identifiable>`、`CsvCodec<T extends Identifiable>`、`MenuAction` 接口；`Person`、`AbstractEntity` 抽象类 |
| 封装 | 实体字段私有；仓储内部 `Map` 不暴露；service 提供用例方法而非裸字段操作 |
| 继承 | `AbstractEntity → Person → Employee/Student`；`AbstractEntity → Department/Course/SchoolClass/ClassCourse/Score` |
| 多态 | service 通过 `Repository<T extends Identifiable>` 调用具体 CSV 仓储；`CsvCodec<T extends Identifiable>` 在运行时选择实体编解码；`MenuAction` 统一菜单项执行 |

### 7.2 SOLID 检查

| 原则 | 落点 | 检查问题 |
|---|---|---|
| SRP | 层与包按职责拆分；一个 service 对应一个业务模块 | 是否出现 UI 计算统计、service 拼 CSV、repository 校验业务规则 |
| OCP | 新增菜单项只需新增 `MenuItem`/`MenuAction`；新增实体只需新增 `CsvCodec` 与仓储装配 | 是否用大量 `if/else` 按类型分支 |
| LSP | `Employee`/`Student` 可替代 `Person` 的只读行为；不抛 `UnsupportedOperationException` | 子类是否破坏父类契约 |
| ISP | `Repository<T extends Identifiable>`、`CsvCodec<T extends Identifiable>`、`MenuAction` 均为小接口 | 是否出现巨大 `Manager` 接口 |
| DIP | service 依赖 `Repository<T extends Identifiable>`，不 `new CsvRepository<>()` | 是否在 service 内硬编码具体仓储 |

> `AbstractEntity` 不是装饰性抽象：它统一 ID、相等性和“新实体”判断，避免 7 个实体重复实现。`Person` 承载 Employee/Student 的公共人员字段。若最终代码删除这些抽象后只是少几行声明，才说明抽象失败。

---

## 8. 集合与性能

| 场景 | 选型 | 理由 |
|---|---|---|
| 仓储主索引 | `LinkedHashMap<Long, T>` | O(1) 按 ID 查找，保留文件顺序，输出稳定 |
| 列表查询结果 | `ArrayList<T>` | 顺序遍历、分页、排序 |
| 教师可见 ClassCourse | `Set<Long>` / `HashSet<Long>` | 快速包含判断 |
| 班级学生 | `List<Student>`（按 `classId` 过滤） | 规模小，过滤成本可接受 |
| 成绩聚合 | `Map<Long, List<Score>>`（按 `classCourseId`） | 一次分组多次统计 |
| 排名 | `List` + `Comparator` 或 `TreeMap` | 平均分降序、学号升序 |
| 唯一性检查 | service 内 `Set`/`Map` | 业务键唯一，不依赖文件顺序 |

- 数据规模上限约 1,000 学生、20,000 成绩；内存全量加载和流式过滤足够。
- 统计按请求即时计算，不缓存；成绩变更后结果立即一致。
- 分页在 UI 层完成，默认每页 10 条：service 返回已过滤、已排序的完整列表，`TablePrinter.page(...)` 按页码切片，`TablePrinter.pageCount(...)` 计算总页数。过滤/搜索后页码重置为 1；越界页夹到有效范围。

---

## 9. 设计模式与装配

| 模式/手法 | 落点 | 说明 |
|---|---|---|
| Repository | `Repository<T extends Identifiable>` + `CsvRepository<T extends Identifiable>` | 隔离业务层与 CSV 细节，Project 2 可替换为 Derby DAO |
| Strategy | `CsvCodec<T extends Identifiable>` 的 7 个实现 | 每个实体独立解析/格式化，仓储不写实体分支 |
| Command | `MenuAction` + `MenuItem` | 菜单项持有可执行动作，新增功能不改菜单循环 |
| Dependency Injection | `ApplicationBootstrap` 构造器注入 | 不使用 Singleton/静态服务定位器 |
| Facade | 各 service | 对 UI 暴露用例级方法，隐藏多仓储协作 |
| 组合根 | `ApplicationBootstrap` | 唯一创建具体实现的地方 |

> 不为了“用模式”引入 Observer、Factory 或空 DAO。Project 2 需要时再在持久化层替换，不预建数据库配置。

---

## 10. 设计迭代与交付

### 10.1 当前状态

| 工件 | 状态 |
|---|---|
| 需求基线 v0.4 | 已冻结 |
| 架构设计 v0.5 | 本文件，已吸收两轮设计评审结论 |
| 用例模型 v0.2 | `docs/design/use-case/use-case-model.md`，已补充 `UC-06a`，已冻结（2026-10-20） |
| 第 1 轮类图讨论稿 | `docs/design/iterations/iteration-1/class-diagram-iteration-1-draft.md`，保留为讨论记录 |
| 第 1 轮正式类图 | `docs/design/iterations/iteration-1/class-diagram-iteration-1.md` v1.0，已冻结，不再修改 |
| GenAI 分析（3.4） | `docs/design/analysis/genai-design-analysis.md`，逐条记录建议与采纳/拒绝 |
| 第 2 轮最终类图 | `docs/design/iterations/iteration-2/class-diagram-iteration-2-final.md` v1.2，含两轮复审补正，已冻结（2026-10-20） |
| 演进说明（3.6） | `docs/design/analysis/design-evolution.md` |
| 学生自撰设计反思（3.7） | 由学生本人撰写，本文档不代写 |

### 10.2 迭代步骤

1. 评审本架构与用例图，确认分层、包名、类名和范围。
2. 第 1 轮类图已固化为 `docs/design/iterations/iteration-1/class-diagram-iteration-1.md` v1.0，冻结后不再修改。
3. 用 GenAI 对正式第 1 轮做批判：职责、耦合、关系、缺失类、过度设计。
4. 逐条记录建议 → 采纳/拒绝 → 理由；形成 3.4 表格。
5. 修订为第 2 轮最终类图，更新本架构文档与包结构。
6. 编写演进说明；学生本人撰写 3.7 反思。
7. 进入 1.3 实现；代码中的类/方法/关系必须与最终类图一致，新增重要类回填类图。

### 10.3 与代码同步

- 最终类图中的每个类都应能在 `src/main/java` 找到。
- 代码中新增的重要类、接口、枚举必须回填最终类图。
- 如果实现阶段发现设计不可行，先改设计和演进说明，再改代码；不要只在代码里悄悄偏离。

---

## 11. 已确认的设计决策

| # | 问题 | 决策 |
|---|---|---|
| 1 | 根包名 | 使用 `com.pdc.learning` |
| 2 | `MenuFactory` 规模 | Iteration 1 保持单类；Iteration 2 拆成 `AdminMenuFactory` / `TeacherMenuFactory`，作为演进证据 |
| 3 | `Session` 与 `AccessScope` | 合并为一个 `Session` 类；服务只依赖 `Session` |
| 4 | DTO 形式 | 统一使用 Java `record` |
| 5 | 教师班级统计的“学生数” | `studentCount` 为全班学生数，不受教师可见课程范围影响；成绩相关指标仍按可见范围计算 |
| 6 | 分页位置 | UI 层分页；service 返回完整过滤/排序结果；每页 10 条 |
| 7 | `Score` 生命周期 | `Student` 是唯一组合拥有者；`ClassCourse—Score` 为普通关联 |
| 8 | 成绩唯一键 | `(studentId, courseId)`，`courseId` 经 `ClassCourse` 解析；转班同课程不新建成绩 |
| 9 | 数据不足表达 | `DataStatus` + 可空指标；空列表不等于数据不足 |
| 10 | 教师课程查看 | 新增 `UC-06a`；`CourseService.list` 只读开放，写操作 `requireAdmin()` |

---

## 附录 A：与报告的对应

| 报告章节 | 本文档内容 |
|---|---|
| 3.1 Use case diagram | 另见 `docs/design/use-case/use-case-model.md` 与 `docs/design/use-case/use-case.puml` |
| 3.2 Scope, constraints and assumptions | 第 1.2、1.3 节 |
| 3.3 Class diagram — Iteration 1 | 另见 `docs/design/iterations/iteration-1/class-diagram-iteration-1.md` 与 `docs/design/iterations/iteration-1/diagrams/class-*-iteration-1.puml` |
| 3.4 GenAI analysis of Iteration 1 | `docs/design/analysis/genai-design-analysis.md` |
| 3.5 Class diagram — Iteration 2 (final) | `docs/design/iterations/iteration-2/class-diagram-iteration-2-final.md` 与 `docs/design/iterations/iteration-2/diagrams/class-*-iteration-2.puml` |
| 3.6 Summary of design evolution | `docs/design/analysis/design-evolution.md` |
| 3.7 Critical Reflection | 学生本人撰写；本文档不代写 |
