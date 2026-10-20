# 最终类图与用例图重画记录

| 项目 | 内容 |
|---|---|
| 日期 | 2026-10-20 |
| 执行人 | 徐浩景（Haojing Xu，24H034160329） |
| 重画范围 | 最终 Iteration 2 的 5 张类图 + 1 张用例图 |
| 不重画 | 第 1 轮类图（`docs/design/iterations/iteration-1/`）保持原样 |
| 旧图归档 | `docs/design/archive/2026-10-20-iteration-2-before-redraw/` |

## 1. 所用工具

| 图 | 工具 | 说明 |
|---|---|---|
| 5 张类图 | PlantUML 1.2026.9 + Graphviz 16.1.0（dot 布局） | 图源仍为 `.puml`，便于版本化与逐类核对 |
| 用例图 | Python + Pillow 自写网格排版 | PlantUML 把 22 个用例排成单列（纵横比 0.34），在 A4 上只有 6 cm 宽、无法阅读，因此改为三列网格手工排版 |

导出命令（本机）：

```bash
java -Dfile.encoding=UTF-8 -DGRAPHVIZ_DOT=<graphviz>/bin/dot.exe      -jar plantuml.jar -tpng -charset UTF-8 -o <输出目录> <图源>.puml
```

## 2. 核对方法

重画前逐类对照 `src/main/java`，用脚本抽取真实签名后再改 `.puml`，不是把旧图重新导出：

1. 抽取 72 个类的声明、`extends` / `implements`、公开方法与构造器参数；
2. 与旧 `.puml` 中声明的类名、方法、依赖逐条比对；
3. 差异按 `docs/design/analysis/implementation-deviation-record.md` 的 D-01～D-11 归类；
4. 只把**代码里真实存在**的内容画进图里，涉及设计意图的部分放在图注（note）中。

## 3. 逐图改动

### 3.1 用例图（`docs/design/use-case/use-case.puml` → `use-case.png`）

- 保留 22 个用例（16 基础 + 6 扩展/独立），保留 UC-16 并连接教师；
- 保留 5 个 `<<extend>>`（UC-04a/05a/07a/09a/10a → 各自基础用例），方向为“扩展用例指向被扩展用例”；
- 全图不使用 `<<include>>`，不使用 actor 泛化（沿用 v0.2 冻结基线的规则）；
- 改为三列网格 + 系统边界内注释区，纵横比从 0.34 提升到 0.95，A4 上可按 13 cm 宽插入；
- 每个用例标注编号、中文名与英文名，便于与正文对照。

### 3.2 领域模型类图（`class-domain-iteration-2.puml`）

- 补齐代码中真实存在的取值方法：`Employee.getUsername/getDepartmentId/getRole/getStatus/getHireDate`、`Student.getStudentNo/getClassId/getEnrollmentDate/getStatus`、`Department.getName/getDescription`、`Course.getCode/getName/getDescription`、`SchoolClass`/`ClassCourse`/`Score` 的取值方法；
- 补 D-05：`Employee.updateUsername`、`Employee.updateDepartment`；
- 图注补 D-06：实体都提供“带 id 的构造器”，仅供 CSV 重建使用；
- 关系按 D 表重画：`Department 1—0..* Employee`、`SchoolClass 1—0..* Student`、`SchoolClass 1—0..* ClassCourse`、`Course 1—0..* ClassCourse`、`Employee 0..1—0..* ClassCourse`、`Student 1—0..* Score`（组合）、`ClassCourse 1—0..* Score`（普通关联）；
- 枚举补 `EmployeeStatus`，五个枚举齐全（Gender、Role、EmployeeStatus、ClassStatus、StudentStatus）。

### 3.3 服务、会话与权限类图（`class-service-iteration-2.puml`）

- 补 D-01：`ClassService`、`TeachingService` 依赖 `Session`；
- 补 D-03：`DepartmentService` 依赖 `Session`（写操作 `requireAdmin()`）；
- 补 D-02：`WarningService` 除 `StatisticsService`、`Session` 外，画上对 `StudentRepository`、`ClassRepository`、`ClassCourseRepository` 的依赖；
- 每个服务的仓储依赖用带标签的依赖线标明具体实体类型，不再用“…”省略。

### 3.4 统计与预警 DTO 类图（`class-dto-iteration-2.puml`）

- 与 11 个 `dto` 源文件逐字段核对，字段名、类型、可空性全部一致；
- 补 `DataStatus`、`WarningLevel`、`ClassWarningLevel` 三个枚举；
- 组合关系：`ClassStatistics ◆— CourseStatistics`、`StudentStatistics ◆— ScoreDetail`；
- 图注说明 `record` 构造器中 `List.copyOf` 的不可变语义，以及“空列表 ≠ 数据不足”。

### 3.5 持久化、工具与异常类图（`class-persistence-iteration-2.puml`）

- 旧图只用一条 “7 implementations” 注释代替 7 个编解码器，重画后逐一画出 7 个 `CsvCodec` 实现并连到接口；
- 补 `Validators`、`Dates`、`BusinessException`、`DataAccessException`；
- 补 D-07 的方法：`CsvRepository.file()/header()/lineOf(long)`、`LoadDiagnostics.recordStats/updateLoaded/issueCount`；
- `Repository` 接口补齐 7 个方法（含 `findAll(Predicate)`、`findFirst(Predicate)`）。

### 3.6 CUI 菜单与界面类图（`class-ui-iteration-2.puml`）

- 补 D-07：`UiSupport`（枚举解析、ID 读取、格式化）、`TablePrinter.showPaged`、`ConsoleIO.readCommand/readRequired`、`Menu.setZeroLabel`；
- 补 D-04：`TeacherMenuFactory` 对 `ClassUI`、`StudentUI`、`CourseUI` 的依赖；
- UC-16 落地后 `AccountUI` 增加 `showProfile()`，图注说明“只读、不显示密码哈希/盐”；
- 各 `XxxUI` 的构造器依赖（多个 service + `ConsoleIO` + `TablePrinter`，部分含 `Session`）按代码逐一画出；
- `MenuFactory` 拆分为接口 + `AdminMenuFactory` / `TeacherMenuFactory` 两个实现。

## 4. 报告替换位置

| 报告位置 | 图 | 文件 |
|---|---|---|
| 3.1，Figure 1 | 用例图 | `docs/design/use-case/use-case.png` |
| 3.5，Figure 3(a) | 领域模型 | `diagrams/png/class-domain-iteration-2.png` |
| 3.5，Figure 3(b) | 服务、会话与权限 | `diagrams/png/class-service-iteration-2.png` |
| 3.5，Figure 3(c) | 统计与预警 DTO | `diagrams/png/class-dto-iteration-2.png` |
| 3.5，Figure 3(d) | 持久化、工具与异常 | `diagrams/png/class-persistence-iteration-2.png` |
| 3.5，Figure 3(e) | CUI 菜单与界面 | `diagrams/png/class-ui-iteration-2.png` |

同时把报告 3.5 的文字与图对齐：接口由 4 个改为 5 个（补 `Identifiable`）、包由 4 个改为 6 个、补 `EmployeeStatus`/`DataStatus`/`WarningLevel`/`ClassWarningLevel`、补 `AdminMenuFactory`/`TeacherMenuFactory` 的实现关系与 `TeacherMenuFactory` 对三个界面的依赖。

## 5. 后续

- 第 1 轮类图未重画，但已统一缩放并拆成 Figure 2(a)–(e) 图注；
- 全部 10 张类图与 1 张用例图统一按“16 cm × 19 cm 框内等比缩放”，保证每张图占一页且文字尽量大；
- 报告尚未定稿：当前校样版放在 `10.23作业\_report_review\`，确认后再并入交付目录；
- 用例图与类图均未使用 PowerDesigner；若评阅要求说明工具，按本文件记录回答即可。
