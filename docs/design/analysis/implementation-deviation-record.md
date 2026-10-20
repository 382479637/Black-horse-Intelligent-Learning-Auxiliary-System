# Component 1.3 实现偏差记录

| 项目 | 内容 |
|---|---|
| 适用阶段 | Component 1.3（Java CUI 实现，第 1 轮） |
| 记录日期 | 2026-10-20 |
| 需求基线 | [`docs/requirements/requirements-analysis.md`](../../requirements/requirements-analysis.md) v0.4 |
| 设计基线 | 架构 v0.5、Iteration 1 类图 v1.0、Iteration 2 最终类图 v1.2（均已冻结） |
| 文档性质 | 实现与冻结设计之间的偏差台账；不修改冻结文档本身 |

> 本文件只记录工程事实、处理决定与原因，不包含 Critical Reflection，也不替代学生本人的需求决策说明。

## 1. 背景

Component 1.3 按冻结设计实现后，实际代码与类图/架构文档出现了若干差异。差异分为三类：

1. **实现必需的补充**：冻结类图未列出、但代码运行或分层校验必需的依赖、构造器与辅助方法；
2. **需求优先的对齐**：需求基线明确要求、但实现初稿取值更严或入口不同，按需求修正；
3. **验收口径的记录**：需求文字存在解释空间，记录当前实现选择及残余风险。

处理原则：**需求基线优先于类图细节；冻结文档不直接改写；所有差异集中记录在本文件**，待文档解冻或进入下一轮时再统一回写。

## 2. 偏差与处理

### 2.1 服务与会话依赖

| 编号 | 偏差 | 处理 | 原因 |
|---|---|---|---|
| D-01 | `ClassService`、`TeachingService` 构造器注入 `Session`，冻结服务类图未标注 | 保留实现，记录于此 | 写操作需要 `session.requireAdmin()`，读取需要按教师身份过滤；`Session` 由组合根注入，不形成循环依赖 |
| D-02 | `WarningService` 注入 `StudentRepository`、`ClassRepository`、`ClassCourseRepository`，冻结类图只标了 `StatisticsService` 与 `Session` | 保留实现，记录于此 | 预警需要按班级、学生、教学任务解析可见范围与“数据不足”口径；若全部经 `StatisticsService` 转发会引入不必要的大接口 |
| D-03 | `DepartmentService` 原先不依赖 `Session`；已补 `Session` 并在 `create`、`update`、`delete` 中调用 `requireAdmin()` | 已修正 | 需求要求“菜单隐藏 + Service 校验”双层权限；仅靠菜单隐藏不满足服务层防御。该修改使实现再次偏离类图，属于有意选择 |
| D-04 | `TeacherMenuFactory` 额外依赖 `ClassUI`、`StudentUI`、`CourseUI`，冻结 UI 类图未列全 | 保留实现，记录于此 | 需求 6.3 的“我的教学”包含“我的班级 / 我的学生 / 课程目录”，菜单装配必须持有对应界面对象 |
| D-14 | `AccountUI` 构造器新增 `EmployeeService`、`DepartmentService`、`TablePrinter`，冻结 UI 类图只标了 `AuthService` 与 `ConsoleIO` | 保留实现，记录于此（补 UC-16 时新增） | UC-16 按用例→服务追溯表经 `EmployeeService.findById` 读取本人资料，用 `DepartmentService` 解析部门名称、`TablePrinter` 统一表格输出；与 `EmployeeUI` 的既有做法一致，未改动 service 接口 |

### 2.2 实体、DTO 与辅助 API

| 编号 | 偏差 | 处理 | 原因 |
|---|---|---|---|
| D-05 | `Employee` 增加 `updateUsername`、`updateDepartment` | 保留实现，记录于此 | 员工资料修改需要同时更新用户名与部门；类图只列了主要字段与部分行为 |
| D-06 | 实体增加公共“带 ID 构造器” | 保留实现，记录于此 | CSV 编解码器用 `withId` 重建持久化对象，需要在不触发业务校验的前提下恢复 ID |
| D-07 | 新增 `UiSupport`，以及 `TablePrinter.showPaged`、`ConsoleIO.readCommand/readRequired`、`Menu.setZeroLabel`、`CsvRepository.file/header/lineOf/removeInMemory` | 保留实现，记录于此 | 分页、命令读取、菜单零项文案、诊断定位与内存删除属于实现层复用；不改变分层边界，也不暴露给业务接口 |
| D-08 | `DataInitializer` 接收 `LoadDiagnostics` | 保留实现，记录于此 | “无管理员时自动创建 admin”需要写入加载诊断，避免兜底行为不可追踪 |

### 2.3 需求优先的行为对齐

| 编号 | 偏差 | 处理 | 原因 |
|---|---|---|---|
| D-09 | 初稿禁止禁用当前登录账号；需求只禁止删除当前账号 | 已移除该额外限制；保留“不能禁用最后一个启用的管理员” | 需求未禁止本人禁用；最后一个启用管理员仍有保护，不会导致系统失去管理入口。已用“第二管理员”场景验证本人禁用成功 |
| D-10 | 主菜单“修改密码”原先进入“我的账户”二级菜单（含查看资料）；UC-16 查看本人资料一直未实现 | 已改为直接进入密码修改流程；后续按用例模型补实现 UC-16，教师主菜单新增独立只读入口“5. 我的资料” | 需求 6.2/6.3 的菜单项就是“修改密码”，不应多一层子菜单；用例模型 UC-16 标为 Must、需求 FR-EMP-08 要求教师可查看本人资料，故以独立只读入口实现，与“修改密码”分离 |
| D-11 | `AccountUI` 原先对外只保留 `show()` | 保留 `show()` 作为密码修改入口，并新增 `showProfile()` 作为 UC-16 只读资料入口 | 冻结 UI 类图的 `AccountUI` 职责已含“修改密码/本人资料”；补 UC-16 后按职责补出 `showProfile()`，并同步更新类图与本记录 |
| D-12 | 非法数据字段（日期、容量、枚举、分数等）初稿在解析失败后由菜单边界提示“操作失败”并返回当前菜单，未在同一字段原地重试 | 保留菜单级重试，记录为已知口径 | 所有解析异常都在 `Menu` 边界捕获，控制台不出现异常栈；空行取消、`0`/`Q` 字面值语义在各字段保持一致。若改为逐字段原地重试，需要在 7 个界面重复实现读取循环，容易引入提示与取消语义不一致。**残余风险**：若验收要求严格同字段重试，需要统一引入带校验的读取辅助方法 |
| D-13 | 首次运行摘要原显示各文件“加载 0 条”，随后才写入种子数据 | 已修正：种子与管理员兜底完成后刷新已加载数量，再打印摘要 | 摘要应反映启动完成后的真实数据量；跳过数量仍保留加载阶段结果。已验证首次运行显示 3/5/5/3/8/21/61 |

### 2.4 依赖与分层说明

- UI 构造器接收多个 Service 与 `Session`，由 `ApplicationBootstrap` 统一装配；未引入单例或静态服务定位器。
- 统计与预警菜单通过 `StatisticsUI.showClassStatistics()`、`StatisticsUI.showStudentStatistics()` 与 `WarningUI.show()` 组合，没有使用 `StatisticsUI.show()` 的独立“班级排名”入口；排名仍由班级统计流程提供，需求菜单也没有单列“班级排名”。
- `data/load-errors.log` 的写入失败警告使用 `System.err`；在标准 Windows 控制台中可见。若 stdout 被单独重定向为 UTF-8 而 stderr 仍为 GBK，警告文本需要按控制台编码查看，但不影响启动与加载。

## 3. 修改文件

| 文件 | 修改 |
|---|---|
| `src/main/java/com/pdc/learning/service/DepartmentService.java` | 注入 `Session`，写操作增加 `requireAdmin()` |
| `src/main/java/com/pdc/learning/service/EmployeeService.java` | 移除“禁止禁用当前登录账号”限制 |
| `src/main/java/com/pdc/learning/ui/view/AccountUI.java` | `show()` 直接执行密码修改，移除账户二级菜单；补 UC-16：新增 `showProfile()` 只读资料与 `EmployeeService`/`DepartmentService`/`TablePrinter` 依赖 |
| `src/main/java/com/pdc/learning/ui/TeacherMenuFactory.java` | 教师主菜单新增“5. 我的资料”入口（UC-16） |
| `src/main/java/com/pdc/learning/persistence/LoadDiagnostics.java` | 新增 `updateLoaded`，用于种子写入后刷新摘要 |
| `src/main/java/com/pdc/learning/app/ApplicationBootstrap.java` | 注入 `Session` 到 `DepartmentService`；启动摘要前刷新各文件加载数量；补 UC-16：装配 `AccountUI` 时注入 `EmployeeService`、`DepartmentService`、`TablePrinter` |

## 4. 验证结果

| 验证项 | 结果 |
|---|---|
| `mvn clean package` | 成功 |
| 主菜单“修改密码” | 直接提示“旧密码（空行取消）”，不再出现“我的账户” |
| 教师“5. 我的资料”（UC-16） | 显示本人用户名/姓名/性别/手机号/邮箱/部门/角色/状态/入职日期；不含密码哈希/盐，不提供修改角色或状态的入口 |
| 本人禁用 | 存在第二个启用管理员时可成功禁用当前账号 |
| 最后一个管理员保护 | 仅剩一个启用管理员时仍拒绝禁用 |
| 部门写操作 | 管理员新增/修改流程正常；服务层已有 `requireAdmin()` |
| 首次运行摘要 | 种子写入后显示实际数量，不再显示 0 |
| 回归数据 | 验收后 `data/` 已恢复为干净种子：部门 3、员工 5、课程 5、班级 3、教学任务 8、学生 21、成绩 61 |

## 5. 后续建议

1. 若冻结类图在下一轮解冻，建议补上 `ClassService`、`TeachingService`、`DepartmentService` 对 `Session` 的依赖，以及 `WarningService` 的三个仓储依赖。
2. 若评审确认 AC-19 要求“同一字段原地重试”，再统一引入字段级校验读取辅助方法，避免逐界面手写循环。
3. 本文件随实现变更持续更新；冻结的需求、架构与类图原文保持不变。
