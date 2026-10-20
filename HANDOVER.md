# 项目交接文档

**项目：** 黑马智能学习辅助系统（COMP603/ENSE600 Project 1，类别 A）
**交接阶段：** Component 1.3 已完成，Component 1.2 图重画、Component 1.4 报告与测试待接手
**交接日期：** 2026-10-20
**交接对象：** 接手后续图、报告、测试与视频的组员

> 配套文档：[`REPORT-WRITING-GUIDE.md`](REPORT-WRITING-GUIDE.md)（报告逐节编写建议）
> 设计偏差台账：[`docs/design/analysis/implementation-deviation-record.md`](docs/design/analysis/implementation-deviation-record.md)
> 设计目录索引：[`docs/design/README.md`](docs/design/README.md)

---

## 0. 先看这里：项目现在是什么状态

### 0.1 一句话现状

**Java CUI 系统已经实现并能构建运行；设计文档已冻结；报告只写了一半；最终类图和用例图需要重画；测试记录和演示视频还没有做。**

### 0.2 已经完成的部分

| 项目 | 状态 | 位置 |
|---|---|---|
| Java CUI 实现（Component 1.3） | 已完成一轮验收 | `src/main/java/` |
| Maven 构建 | 已通过 | `pom.xml`、`target/learning-assistant.jar` |
| 需求基线 v0.4 | 已冻结 | `docs/requirements/requirements-analysis.md` |
| 架构设计 v0.5 | 已冻结 | `docs/design/architecture/architecture-design.md` |
| 用例模型 v0.2 | 已冻结 | `docs/design/use-case/use-case-model.md` |
| 第 1 轮类图 | 已冻结 | `docs/design/iterations/iteration-1/` |
| 第 2 轮最终类图 | 已冻结 | `docs/design/iterations/iteration-2/` |
| 设计演进与 GenAI 分析 | 已完成 | `docs/design/analysis/` |
| 实现偏差记录 | 已完成 | `docs/design/analysis/implementation-deviation-record.md` |
| 报告 | 部分完成 | `Assignment1_Report_CJLU2026.docx` |
| 演示数据 | 干净种子 | `data/` |

### 0.3 你接手的三件事

1. **重画最终 Iteration 2 的 5 张类图 + 用例图**，导出 PNG 并替换报告 3.1、3.5 中的图片。
2. **续写报告**，补 1、2.1、2.2、3.1–3.6、附录 A/B；3.7 必须由学生本人写。
3. **做测试并写测试报告**，为演示视频和最终提交留下证据。

另有一项代码补充任务：**UC-16「查看本人资料」当前没有实现**。已确认的处理方式是保留该用例、后续补实现（见 3.2.3）。

### 0.4 五条红线

1. **2.6 和 3.7 必须由学生本人写，禁止 AI 代写或改写。** 可以讨论、可以检查逻辑，但不能把 AI 生成的句子放进这两节，也不能替别人签名。
2. **不要伪造“手动重画”或“测试通过”的证据。** 用了什么工具、改了什么、测了什么，按事实记录。
3. **不要删除或覆盖旧设计文件。** 重画后的图可以替换报告用图，但旧图应保留在 `docs/design/archive/` 或带日期的副本中。
4. **`data/` 当前是干净种子数据。** 做删除、损坏 CSV、改密码等破坏性测试时，必须在项目副本里做，不要污染交付用的 `data/`。
5. **Project 1 不要求 JUnit。** JUnit、数据库、GUI 是 Project 2 的内容；本轮不要为了“看起来完整”而引入。

---

## 1. 如何构建和运行

### 1.1 环境要求

| 项目 | 要求 | 检查命令 |
|---|---|---|
| JDK | 21 或更高 | `java -version` |
| Maven | 3.9 或更高 | `mvn -v` |
| 操作系统 | Windows / macOS / Linux | — |
| 网络 | 首次构建需要下载 Maven 插件；之后可离线 | — |

> 本机注意：`java -version` 可能显示 JDK 25，而 `mvn -v` 显示 Maven 正在使用 JDK 21。这是正常的，只要 Maven 使用的是 **21 或更高** 即可。如果 `mvn -v` 显示低于 21，把 `JAVA_HOME` 指向 JDK 21 再构建。

### 1.2 构建

在项目根目录执行：

```bash
cd /e/Java/Projects/PDC_Project_1
mvn clean package
```

成功标志：

- 终端最后出现 `BUILD SUCCESS`；
- 生成 `target/learning-assistant.jar`；
- 主类是 `com.pdc.learning.app.Main`。

本项目**没有第三方运行时依赖**，不需要手工加库、建数据库或改配置文件。

### 1.3 运行

```bash
java -jar target/learning-assistant.jar
```

必须在**项目根目录**启动，因为程序使用相对路径 `data/`。如果在其他目录启动，程序会在那个目录下新建 `data/`，看起来像“数据丢了”。

启动后会先打印数据加载摘要，然后进入登录界面。首次运行（`data/` 下 7 个 CSV 都没有数据行）会自动写入演示数据。

### 1.4 演示账号

| 用户名 | 密码 | 角色 | 说明 |
|---|---|---|---|
| `admin` | `Admin@123` | 管理员 | 全部管理功能 |
| `t01` | `Teach@123` | 教师 | 张伟，计算机学院 |
| `t02` | `Teach@123` | 教师 | 李娜，计算机学院 |
| `t03` | `Teach@123` | 教师 | 王强，数学学院 |
| `t04` | `Teach@123` | 教师 | 刘洋，计算机学院 |

密码在 CSV 中以 PBKDF2 加盐哈希保存，不保存明文。

### 1.5 数据文件与重置

```text
data/
├── departments.csv
├── employees.csv
├── courses.csv
├── classes.csv
├── class_courses.csv
├── students.csv
├── scores.csv
└── load-errors.log      # 每次启动覆盖写入，不要手工编辑
```

当前干净种子的数据量：

| 文件 | 数据行 |
|---|---:|
| `departments.csv` | 3 |
| `employees.csv` | 5 |
| `courses.csv` | 5 |
| `classes.csv` | 3 |
| `class_courses.csv` | 8 |
| `students.csv` | 21 |
| `scores.csv` | 61 |

**重置为首次运行状态：**

1. 先退出程序（主菜单 `Q`）。
2. 备份或删除 `data/` 目录。
3. 重新启动程序。

Windows PowerShell 示例：

```powershell
Remove-Item -Recurse -Force data
java -jar target\learning-assistant.jar
```

Git Bash 示例：

```bash
rm -rf data
java -jar target/learning-assistant.jar
```

> 删除前先确认没有需要保留的测试数据。建议把当前干净的 `data/` 复制到项目外，例如 `C:\temp\pdc-seed-backup`，测试后可以整体恢复。

### 1.6 常见问题

| 现象 | 原因 | 处理 |
|---|---|---|
| `mvn` 不是内部或外部命令 | Maven 未加入 PATH | 安装 Maven 3.9+，或使用 IDE 内置 Maven |
| 构建报 `release version 21 not supported` | Maven 用了低于 21 的 JDK | 设置 `JAVA_HOME` 到 JDK 21 |
| 启动后找不到数据 | 不在项目根目录启动 | `cd` 到项目根目录再运行 |
| 中文乱码 | Windows 终端编码不是 UTF-8 | 先执行 `chcp 65001`，并使用支持中文的字体 |
| `load-errors.log` 出现警告 | CSV 有坏行、重复 ID 或缺失外键 | 查看日志中的文件、行号、错误类别；不要直接忽略 |
| 退出后数据没有变化 | 没有走到保存流程，或启动目录不对 | 确认从项目根目录启动；正常退出会保存 |
| IDE 里运行找不到 `data/` | IDE 的工作目录默认是模块目录 | 把 Run Configuration 的 Working directory 设为项目根目录 |

---

## 2. 如何测试与编写测试报告

### 2.1 先明确：Project 1 没有 JUnit 要求

Project 1 的评分组件是：

- 1.1 需求生成与分析（20%）
- 1.2 软件设计（30%）
- 1.3 Java CUI 实现（30%）
- 1.4 AI Usage Log 与演示视频（20%）

**没有“测试”评分项，也不要求 JUnit。** JUnit 4、至少 5 个测试用例、数据库测试是 **Project 2** 的 Component 2.3.4。

因此本轮测试的定位是：

1. 证明系统在验收场景下可用、稳定；
2. 为演示视频提供“我确实跑过”的证据；
3. 为报告中的实现说明和已知风险提供依据；
4. 给接手成员自己一份可复查的质量记录。

**不要把测试报告硬塞进官方报告正文。** 官方报告模板没有测试章节；测试报告作为独立 QA 文档保留，必要时在 1. Project Setup 或演示视频中引用。

### 2.2 测试策略

按下面 6 层执行，先做冒烟，再做功能和边界，最后做破坏性测试。

| 层级 | 目标 | 典型内容 |
|---|---|---|
| 1. 冒烟 | 能构建、能启动、能登录 | `mvn clean package`；启动摘要；`admin` 登录；`Q` 退出 |
| 2. 功能 | 每个 FR 模块的主流程 | 部门、员工、课程、班级、教学任务、学生、成绩 CRUD |
| 3. 权限 | 管理员/教师边界 | 教师不能改课程、不能看非任课班级、不能删他人成绩 |
| 4. 边界 | 数值、日期、状态、容量 | 0/100/101 分；容量满；`PLANNED→ACTIVE→FINISHED`；非法转换 |
| 5. 健壮性 | 坏输入、坏文件、只读文件 | 非数字、空行、重复 ID、缺失外键、只读 `load-errors.log` |
| 6. 持久化与回归 | 重启后数据仍在；改完不破坏旧功能 | 退出重启；种子数据恢复；统计/预警随成绩变化 |

### 2.3 测试数据与账号

- 基线数据使用当前 `data/` 的干净种子（3/5/5/3/8/21/61）。
- 教师账号按任课范围不同：`t01`、`t02`、`t03`、`t04` 看到的数据不一样；测试教师权限时不要只用 `t01`。
- 预警测试使用种子中已有的正常、轻度、中度、重度场景，再补边界值。
- 改密码、删除、转班、批量录入等会改变数据的测试，**在项目副本里做**。

### 2.4 推荐执行流程

**第一步：建立沙箱副本**

```bash
# 在项目上一级目录执行
cp -r PDC_Project_1 PDC_Project_1_test
cd PDC_Project_1_test
mvn clean package
java -jar target/learning-assistant.jar
```

所有破坏性测试只在 `PDC_Project_1_test` 里做；交付目录 `PDC_Project_1` 的 `data/` 保持干净。

**第二步：按验收标准逐条执行**

完整验收标准在 [`docs/requirements/requirements-analysis.md`](docs/requirements/requirements-analysis.md) 第 10 节，共 AC-01 至 AC-50。建议按模块分组执行：

| 模块 | 对应 AC | 重点 |
|---|---|---|
| 登录与账户 | AC-01、02、03、18、19、43 | 正确/错误密码、禁用账号、修改密码三个失败分支 |
| 部门 | AC-04、21、33 | 删除有员工的部门、重名、组合筛选 |
| 员工 | AC-05、22、37、38、32 | 删除/禁用班主任或任课教师、最后一个管理员保护 |
| 课程 | AC-07、23 | 被引用课程不能删 |
| 班级 | AC-06、24、35、41、44、47、33 | 状态转换、容量、归档保护、筛选 |
| 教学任务 | AC-25 | 待分配、有成绩不能移除 |
| 学生 | AC-08、09、26、48 | 转班、历史成绩归属、级联删除 |
| 成绩 | AC-10、11、13、27、34、45 | 重复成绩、0 分、非法分数、批量录入、删除权限 |
| 统计 | AC-16、28 | 数据不足、教师/管理员可见范围 |
| 预警 | AC-17、29、36、42、46 | R1–R6、边界分、多规则命中、班级预警 |
| 系统与持久化 | AC-14、15、20、30、31、39、40、49、50 | 重启、坏行、干净构建、密码存储、日志覆盖与只读 |

**第三步：记录证据**

每条测试至少留下：

- 测试编号、对应 AC、前置条件、操作步骤、预期结果、实际结果、通过/失败；
- 失败时的复现步骤和截图/终端输出；
- 涉及持久化的，记录重启前后的数据；
- 涉及诊断日志的，保留 `data/load-errors.log` 的相关片段（注意不要泄露密码/哈希）。

**第四步：缺陷回归**

发现缺陷后记录：编号、严重度、复现步骤、影响范围、修复提交/日期、回归结果。修复后再跑一遍同一条 AC，不能只跑新功能。

### 2.5 测试报告模板

下面是一份可以直接复制到 Word/Markdown 的结构。把 `<...>` 换成真实内容，**不要保留示例结果**。

```text
# 黑马智能学习辅助系统 — 测试报告

## 1. 概述
- 被测版本：<版本号/日期>
- 测试人：<姓名>
- 测试日期：<日期>
- 测试环境：<操作系统> / JDK <版本> / Maven <版本>
- 测试依据：requirements-analysis.md v0.4 第 10 节 AC-01～AC-50

## 2. 测试范围
- 范围内：<模块/AC 范围>
- 范围外：<GUI、数据库、JUnit、并发等 Project 2 内容>
- 未执行项及原因：<...>

## 3. 测试数据与账号
- 数据基线：<种子数据数量>
- 沙箱位置：<项目副本路径>
- 账号：<管理员/教师账号，不要写真实个人密码>

## 4. 测试策略
<冒烟 / 功能 / 权限 / 边界 / 健壮性 / 持久化与回归>

## 5. 结果汇总
| 计划 | 执行 | 通过 | 失败 | 阻塞 | 通过率 |
|---:|---:|---:|---:|---:|---:|
| <n> | <n> | <n> | <n> | <n> | <%> |

## 6. 详细记录
| 编号 | 对应 AC | 前置条件 | 操作步骤 | 预期结果 | 实际结果 | 结果 | 证据 |
|---|---|---|---|---|---|---|---|
| T-001 | AC-01 | 干净种子 | 输入 admin/Admin@123 | 进入管理员菜单 | <填写> | <通过/失败> | <截图/日志> |

## 7. 缺陷记录
| 编号 | 严重度 | 关联 AC | 复现步骤 | 实际/预期 | 状态 | 回归结果 |
|---|---|---|---|---|---|---|
| D-001 | <高/中/低> | AC-xx | <步骤> | <差异> | <已修复/遗留> | <通过/失败> |

## 8. 残余风险与未覆盖
- <例如：只测了 Windows；未做并发；未做大数据量>

## 9. 结论
<是否可以交付；失败项是否影响 Must 验收；建议>

## 10. 签字
测试人：<姓名>    日期：<日期>
```

### 2.6 测试报告写作要求

- **按 AC 编号追溯**：每条测试至少对应一个 AC；没有 AC 的探索性测试单独标注。
- **写实际结果，不写“应该没问题”**：失败就写失败，阻塞就写阻塞。
- **区分 Must 和 Should**：AC-32、AC-33、AC-34 是 Should，未实现不阻塞 Must 验收；但要在报告中说明。
- **证据可复查**：截图要有日期/终端标题；日志片段要能对应到文件、行号、错误类别。
- **不要伪造**：没有跑的测试不要写成通过；不要复制别人的测试记录。
- **不要泄露敏感信息**：报告和截图里不要出现密码哈希、盐、真实个人手机号/邮箱。
- **测试环境写清楚**：Windows 终端的编码问题、JDK 版本、Maven 版本都可能影响结果。

### 2.7 退出标准

- 冒烟测试全部通过；
- AC-01～AC-31、AC-35～AC-50 的 Must 项全部执行并有记录；
- 所有失败项有缺陷记录和结论；
- 交付目录的 `data/` 已恢复为干净种子；
- 测试报告有测试人、日期、环境和结论。

---

## 3. 用例图与类图：最终方案、目录与重画要求

### 3.1 架构设计方案（最终版）

系统采用**分层架构 + 面向接口 + 构造器注入**，没有单例，没有静态服务定位器。所有对象在 `ApplicationBootstrap` 中装配。

```text
app（组合根）
  └──> ui（菜单、输入输出、错误呈现）
         └──> service（用例编排、权限、统计/预警）
                ├──> model（实体、枚举）
                ├──> persistence（Repository 接口、CSV 实现）
                └──> dto（统计/预警只读结果）
persistence 实现 ──> model、util
util / exception 被各层按需使用
```

| 层 | 包 | 职责 | 关键约束 |
|---|---|---|---|
| Bootstrap | `app` | 创建、装配、启动、退出保存 | 唯一同时看到接口和实现的地方 |
| Presentation | `ui` | 菜单、输入、输出、格式校验、错误呈现 | 不直接读写文件，不实现业务规则 |
| Application | `service` | 用例编排、权限、状态/引用校验、统计与预警 | 依赖 `Repository` 接口和 `Session` |
| Domain | `model` | 实体、枚举、实体内部规则 | 不依赖 UI、持久化、service |
| Persistence | `persistence` | 仓储契约、CSV 编解码、加载/保存、诊断 | 不依赖 UI，不承载业务规则 |
| DTO | `dto` | 统计/预警结果 | 不承载持久化或 UI 逻辑 |
| Utility | `util` | 密码哈希、CSV 转义、校验、日期 | 不依赖上层 |
| Exception | `exception` | 业务异常、数据访问异常 | 由上层转成用户可读提示 |

设计要点：

- **Repository**：`Repository<T extends Identifiable>` 隐藏 CSV 存储细节。
- **CsvCodec**：每个实体一个编解码器，相当于 Strategy。
- **MenuFactory**：接口 + `AdminMenuFactory` / `TeacherMenuFactory`，按角色装配菜单。
- **Session**：保存当前用户和可见范围，服务层用 `requireAdmin()`、`canAccess()` 等做权限校验。
- **组合根**：`ApplicationBootstrap` 负责所有依赖注入；`Main` 只负责启动和退出处理。
- **持久化**：7 个 UTF-8 CSV，临时文件 + 原子替换；启动时按依赖顺序加载并校验引用。

架构图源与导出图：

- [`docs/design/architecture/architecture.puml`](docs/design/architecture/architecture.puml)
- [`docs/design/architecture/architecture.png`](docs/design/architecture/architecture.png)
- [`docs/design/architecture/architecture-design.md`](docs/design/architecture/architecture-design.md)

### 3.2 用例图最终方案

#### 3.2.1 Actor

- **管理员（Administrator）**：维护组织、人员、课程、班级、教学任务、学生、成绩；查看全局统计与预警。
- **教师（Teacher）**：查看本人任课班级与学生；维护本人任课 `ClassCourse` 的成绩；查看本人范围内的统计与预警。

不画成 actor：

- 学生：本版本没有学生登录端；
- 课程评审者、开发者：是利益相关者，不操作系统；
- 数据库/文件系统：是内部实现，不是 actor。

#### 3.2.2 用例清单

最终用例模型共 22 个用例（16 个基础用例 + 6 个扩展/独立用例）：

| 编号 | 用例 | Actor | 优先级 |
|---|---|---|---|
| UC-01 | 登录 | 管理员、教师 | Must |
| UC-02 | 登出 | 管理员、教师 | Must |
| UC-03 | 修改密码 | 管理员、教师 | Must |
| UC-04 | 管理部门 | 管理员 | Must |
| UC-04a | 搜索部门 | 管理员 | Should |
| UC-05 | 管理员工 | 管理员 | Must |
| UC-05a | 重置员工密码 | 管理员 | Should |
| UC-06 | 管理课程 | 管理员 | Must |
| UC-06a | 查看课程目录 | 管理员、教师 | Must |
| UC-07 | 管理班级 | 管理员 | Must |
| UC-07a | 筛选班级 | 管理员 | Should |
| UC-08 | 管理教学任务 | 管理员 | Must |
| UC-09 | 管理学生 | 管理员 | Must |
| UC-09a | 学生转班 | 管理员 | Must |
| UC-10 | 管理成绩 | 管理员、教师 | Must |
| UC-10a | 批量录入成绩 | 管理员、教师 | Should |
| UC-11 | 查看班级统计 | 管理员、教师 | Must |
| UC-12 | 查看学生统计 | 管理员、教师 | Must |
| UC-13 | 查看学习预警 | 管理员、教师 | Must |
| UC-14 | 查看我的教学 | 教师 | Must |
| UC-15 | 查看我的学生 | 教师 | Must |
| UC-16 | 查看本人资料 | 教师 | Must |

关系规则：

- `UC-04a`、`UC-05a`、`UC-07a`、`UC-09a`、`UC-10a` 用 `<<extend>>` 指向对应基础用例。
- `UC-06a` 是独立用例，不是 `UC-06` 的扩展：教师只读，管理员可增删改。
- 不使用 `<<include>>`：登录是前置条件，不是每次必然发生的公共子用例。
- 不使用 actor 泛化：管理员和教师直接连接各自用例。

#### 3.2.3 UC-16 的特殊处理（已确认）

冻结用例模型把 `UC-16 查看本人资料` 标为 Must，但当前代码只实现了“修改密码”，没有教师查看本人资料的界面。

**已确认的处理：保留 UC-16，后续补实现。**

接手成员需要：

1. 重画用例图时保留 `UC-16`，连接教师；
2. 在教师菜单增加只读的“我的资料”入口（建议主菜单 `5. 我的资料`，与用例模型第 4 节的菜单映射一致）；
3. 只读展示当前登录教师自己的资料，不显示密码哈希/盐；不要提供修改角色/状态的入口；
4. 实现后同步更新 `AccountUI` 的类图（例如增加 `showProfile()`）和实现偏差记录中的 D-10；
5. 不要因为“当前没实现”就从用例图中删掉 UC-16。

#### 3.2.4 用例图文件与目录

| 内容 | 路径 |
|---|---|
| 用例模型说明 | [`docs/design/use-case/use-case-model.md`](docs/design/use-case/use-case-model.md) |
| PlantUML 源 | [`docs/design/use-case/use-case.puml`](docs/design/use-case/use-case.puml) |
| 导出图 | [`docs/design/use-case/use-case.png`](docs/design/use-case/use-case.png) |
| 报告位置 | 3.1 Use case diagram，Figure 1 |

重画后：

- 用新 PNG 替换报告 3.1 的图片；
- 保留旧图副本（建议放到 `docs/design/archive/`，文件名带日期）；
- 如果修改了用例清单或关系，必须同步更新 `use-case-model.md` 的用例表和追溯表。

### 3.3 类图最终方案

#### 3.3.1 五张分工图

最终 Iteration 2 类图按职责拆成 5 张，避免一张图连线过多、打印不可读：

| 图 | 内容 | 文件 |
|---|---|---|
| 领域模型 | `AbstractEntity`、`Person`、`Employee`、`Student`、`Department`、`Course`、`SchoolClass`、`ClassCourse`、`Score`、枚举 | `class-domain-iteration-2.puml` |
| 服务、会话与权限 | `Session`、`AuthService`、`DepartmentService`、`EmployeeService`、`CourseService`、`ClassService`、`TeachingService`、`StudentService`、`ScoreService`、`StatisticsService`、`WarningService` | `class-service-iteration-2.puml` |
| 统计与预警 DTO | `ClassStatistics`、`CourseStatistics`、`StudentStatistics`、`ScoreDetail`、`RankEntry`、`StudentMetrics`、`WarningResult`、`ClassWarning`、`WarningLevel`、`ClassWarningLevel`、`DataStatus` | `class-dto-iteration-2.puml` |
| 持久化与工具 | `Repository`、`CsvCodec`、`CsvRepository`、7 个 Codec、`DataLoader`、`DataInitializer`、`LoadDiagnostics`、`PasswordHasher`、`CsvUtil`、`Validators`、`Dates`、异常类 | `class-persistence-iteration-2.puml` |
| CUI 菜单与界面 | `ConsoleIO`、`TablePrinter`、`Menu`、`MenuItem`、`MenuAction`、`MenuFactory`、`AdminMenuFactory`、`TeacherMenuFactory`、`LoginUI`、各 `XxxUI`、`UiSupport` | `class-ui-iteration-2.puml` |

#### 3.3.2 最终关系（必须在图上体现）

- **继承**：`Employee`、`Student` → `Person` → `AbstractEntity`。
- **实现**：`CsvRepository` → `Repository`；7 个 Codec → `CsvCodec`；`AdminMenuFactory`、`TeacherMenuFactory` → `MenuFactory`；实体 → `Identifiable`；`MenuItem` 使用 `MenuAction`。
- **组合**：`Student ◆— Score`（学生是成绩生命周期的唯一组合拥有者）。
- **普通关联**：`ClassCourse — Score`（教学任务不是成绩的组合拥有者）。
- **业务关联**：
  - `Department 1 — 0..* Employee`
  - `SchoolClass 1 — 0..* Student`
  - `SchoolClass 1 — 0..* ClassCourse`
  - `Course 1 — 0..* ClassCourse`
  - `Employee 0..1 — 0..* ClassCourse`（待分配时教师为空）
  - `Student 1 — 0..* Score`
  - `ClassCourse 1 — 0..* Score`
- **依赖**：`UI → Service`；`Service → Repository`；`LoginUI → AuthService + MenuFactory`；`ApplicationBootstrap → 所有层`。

#### 3.3.3 必须反映的实现偏差

冻结类图早于实现。重画最终类图时，**以代码为准**，并把下列偏差画进去（详见 [`implementation-deviation-record.md`](docs/design/analysis/implementation-deviation-record.md)）：

| 编号 | 画图时要体现的内容 |
|---|---|
| D-01 | `ClassService`、`TeachingService` 依赖 `Session` |
| D-02 | `WarningService` 除 `StatisticsService`、`Session` 外，还依赖 `StudentRepository`、`ClassRepository`、`ClassCourseRepository` |
| D-03 | `DepartmentService` 依赖 `Session`，写操作执行 `requireAdmin()` |
| D-04 | `TeacherMenuFactory` 依赖 `ClassUI`、`StudentUI`、`CourseUI` |
| D-05 | `Employee` 有 `updateUsername`、`updateDepartment`（以及已有的 `getEmail`/`updateEmail`） |
| D-06 | 实体有带 ID 的构造器（用于 CSV 重建；可在图上用构造器签名或注释体现） |
| D-07 | `UiSupport`；`TablePrinter.showPaged`；`ConsoleIO.readCommand/readRequired`；`Menu.setZeroLabel`；`CsvRepository.file/header/lineOf/removeInMemory` |
| D-08 | `DataInitializer` 接收 `LoadDiagnostics` |
| D-10 | `AccountUI.show()` 直接执行修改密码；补 UC-16 后增加只读资料查看方法 |
| D-11 | `AccountUI` 对外只保留 `show()`（补 UC-16 后再加 `showProfile()`） |
| D-12 | 非法字段输入是菜单级重试，不是同字段原地重试（可在图注/说明中写，不必画成类） |
| D-13 | 首次运行摘要顺序（属于运行时行为，不需要画进类图） |

#### 3.3.4 报告 3.5 文字中的事实错误（重画时一并修正）

当前报告 3.5 的类图说明有几处与代码/设计不一致：

- 写“4 interfaces”，实际有 **5 个接口**：`Repository`、`CsvCodec`、`MenuAction`、`MenuFactory`、`Identifiable`。
- 写“4 concrete class packages”，后面却列了 6 个包；建议写 **model、dto、persistence、service、ui、app**，另加 util/exception。
- `Model` 枚举列表漏了 `EmployeeStatus`。
- DTO 列表漏了 `DataStatus`、`WarningLevel`、`ClassWarningLevel`。
- Persistence 列表漏了 `Repository`、`CsvCodec`、`CsvRepository`、`LoadDiagnostics` 之外的接口/异常说明。
- UI 列表要包含 `AccountUI`、`UiSupport`、`Menu/MenuItem/MenuAction`。
- 不要把 `TeacherMenuFactory` 画成只依赖 `AccountUI`/`ScoreUI` 等少数界面；它还需要 `ClassUI`、`StudentUI`、`CourseUI`。

#### 3.3.5 文件与目录

| 内容 | 路径 |
|---|---|
| 最终类图说明 | [`docs/design/iterations/iteration-2/class-diagram-iteration-2-final.md`](docs/design/iterations/iteration-2/class-diagram-iteration-2-final.md) |
| PlantUML 源（5 张） | `docs/design/iterations/iteration-2/diagrams/class-*-iteration-2.puml` |
| PNG（5 张） | `docs/design/iterations/iteration-2/diagrams/png/class-*-iteration-2.png` |
| 第 1 轮类图（不重画） | `docs/design/iterations/iteration-1/` |
| 报告位置 | 3.3（第 1 轮，Figure 2）；3.5（最终，Figure 3） |

### 3.4 重画范围与工具

**已确认：只重画最终 Iteration 2 的 5 张类图 + 用例图。** 第 1 轮类图继续使用现有版本。

#### 推荐工具

| 工具 | 适合 | 注意 |
|---|---|---|
| PowerDesigner | 手动建模、导出高质量 PNG，评阅时容易说明“本人重画” | 需要在本机重建模型；不要把 PlantUML 源直接当作 PowerDesigner 模型 |
| PlantUML（VS Code 扩展或 `plantuml.jar`） | 快速改源、版本化、导出稳定 | 必须自己逐类核对并修改；不要只把旧文件重新导出就称为“重画” |
| draw.io / StarUML | 需要更自由的排版 | 导出 PNG 时注意字体和清晰度 |

#### 重画步骤

1. 先读最终类图文档和实现偏差记录，列出每个包的类、方法、依赖。
2. 对照 `src/main/java` 核对类名、方法名、构造器依赖；以代码为准。
3. 一张一张画，不要先画一张“大杂烩”再拆。
4. 导出 PNG：至少 2x 缩放或 300 dpi；报告打印后要能看清类名和关系标签。
5. 用新 PNG 替换报告 3.5 的 5 张图；报告 3.1 替换用例图。
6. 保留旧图的日期副本；不要删除 `docs/design/` 下的原文件。
7. 记录重画信息：工具、日期、重画人、改了哪些类/关系。建议新建
   `docs/design/iterations/iteration-2/redraw-record.md`，只写事实，不写反思。

#### 导出命令参考

```bash
# 5 张类图；-o 相对于每个源文件所在目录
java -jar plantuml.jar -tpng -scale 2 -o png docs/design/iterations/iteration-2/diagrams/*.puml

# 用例图
java -jar plantuml.jar -tpng -scale 2 docs/design/use-case/use-case.puml
```

> `-scale 2` 不是所有 PlantUML 版本都支持；如果报错，改用 `skinparam dpi 200` 或导出 SVG 后在 Word 中缩放。

### 3.5 重画后的自检清单

- [ ] 用例图有系统边界、管理员/教师两个 actor、22 个用例；
- [ ] `UC-16` 保留并连接教师；
- [ ] 5 个 `<<extend>>` 关系方向正确；
- [ ] 没有 `<<include>>`，没有 actor 泛化；
- [ ] 5 张类图覆盖领域、服务、DTO、持久化、UI；
- [ ] 每个类都能在 `src/main/java` 找到，或能说明是接口/枚举/异常；
- [ ] 5 个接口、2 个抽象类、6 个主要包的数量写对；
- [ ] `Student ◆— Score`、`ClassCourse — Score` 的多重性和菱形正确；
- [ ] 多端使用 `0..*`，没有把“允许空集合”画成 `1..*`；
- [ ] `Session` 依赖已画进 `DepartmentService`、`ClassService`、`TeachingService`；
- [ ] `WarningService` 的三个仓储依赖已画；
- [ ] `TeacherMenuFactory` 的 `ClassUI`、`StudentUI`、`CourseUI` 依赖已画；
- [ ] `EmployeeStatus`、`DataStatus`、`WarningLevel`、`ClassWarningLevel` 没有漏；
- [ ] `AccountUI` 补 UC-16 后更新了方法；
- [ ] 图片在 Word 中打印清晰，图注编号连续。

---

## 4. 报告续写

报告的逐节状态、具体问题和修改建议见配套文档：

**→ [`REPORT-WRITING-GUIDE.md`](REPORT-WRITING-GUIDE.md)**

这里只给三条最重要的提醒：

1. **先复制再改。** 不要直接覆盖 `Assignment1_Report_CJLU2026.docx`；复制成带日期的版本（例如 `Assignment1_Report_CJLU2026_v2_20261020.docx`），保留当前版本作为对照。
2. **2.6 和 3.7 只能由学生本人写。** 不要用 AI 生成、改写、润色到失去本人语言；不要替别人签名。
3. **删除模板说明和灰色占位符。** 报告里所有蓝色 GUIDANCE 框、`[Insert ...]`、示例行、`(Add or remove rows as needed...)` 都要在提交前清理。

---

## 5. 交接验收清单

接手成员完成以下事项后，才算交接完成：

- [ ] 能在自己的机器上 `mvn clean package` 成功；
- [ ] 能从项目根目录 `java -jar target/learning-assistant.jar` 启动并登录；
- [ ] 知道如何重置 `data/`，并已在沙箱副本里做过至少一次破坏性测试；
- [ ] 已读 `implementation-deviation-record.md`，知道 D-01～D-13；
- [ ] 已重画用例图 + 最终 5 张类图，PNG 已替换进报告；
- [ ] 已补 UC-16「查看本人资料」的只读实现，并同步用例图/类图/偏差记录；
- [ ] 已按 AC-01～AC-50 执行测试，形成测试报告；
- [ ] 报告 1、2.1、2.2、3.1–3.6、附录 A/B 已完成；
- [ ] 2.6、3.7 由学生本人完成并签字；
- [ ] 报告目录已更新（Ctrl+A → F9），模板说明已删除；
- [ ] `data/` 已恢复为干净种子；
- [ ] 演示视频 ≤ 5 分钟，覆盖 GenAI 使用、系统演示、代码结构；
- [ ] 最终 ZIP 包含源码、报告、视频，文件名符合课程要求。

---

## 6. 已知问题与待决策

| # | 事项 | 当前状态 | 建议 |
|---|---|---|---|
| 1 | `UC-16 查看本人资料` 未实现 | 用例模型标 Must，代码没有入口 | 保留用例，补只读“我的资料”入口；实现后更新图与偏差记录 |
| 2 | AC-19 非法字段输入是菜单级重试 | 已在偏差记录 D-12 说明 | 如果评阅要求“同一字段原地重试”，再统一改造读取辅助方法；不要零散改 7 个界面 |
| 3 | 报告 3.5 没有最终类图 | 只有 `[Insert your FINAL class diagram here...]` 占位 | 重画后插入 5 张图，图注写 Figure 3(a)–(e) |
| 4 | 报告 3.6 第一行两列内容相同 | 看起来像复制错误 | 按 `design-evolution.md` 重写为“第 1 轮是什么、第 2 轮改成什么、为什么” |
| 5 | 报告 2.2 的优先级与需求基线不一致 | FR-2～FR-6 写成 Secondary，需求中这些模块是 Must | 模块级用 Essential；Should 子项单独标 Desirable；不要把整个模块降级 |
| 6 | 报告 3.4 只有“采纳”，没有拒绝 | 评分标准明确说“拒绝坏建议并说明理由”得分更高 | 从 `genai-design-analysis.md` 选 2–3 条部分采纳/拒绝的记录补进去 |
| 7 | 附录 A.2 还有酒店预订示例 | 模板残留 | 删除示例行，换成真实提示词记录 |
| 8 | 附录 B 贡献表空白 | 未填写 | 按实际贡献填写，百分比合计 100%；成员变化时同步所有声明 |
| 9 | Windows 终端编码 | stdout/stderr 可能不同编码 | 测试时先 `chcp 65001`；日志乱码时用正确编码查看，不要误判为功能失败 |

---

## 7. 文件地图

| 你想找 | 去哪里 |
|---|---|
| 构建配置 | `pom.xml` |
| 入口与组合根 | `src/main/java/com/pdc/learning/app/Main.java`、`ApplicationBootstrap.java` |
| 业务服务 | `src/main/java/com/pdc/learning/service/` |
| 实体与枚举 | `src/main/java/com/pdc/learning/model/` |
| CSV 持久化 | `src/main/java/com/pdc/learning/persistence/` |
| CUI 界面 | `src/main/java/com/pdc/learning/ui/` |
| 需求与验收标准 | `docs/requirements/requirements-analysis.md` |
| 架构设计 | `docs/design/architecture/architecture-design.md` |
| 用例模型 | `docs/design/use-case/use-case-model.md` |
| 第 1 轮类图 | `docs/design/iterations/iteration-1/` |
| 第 2 轮最终类图 | `docs/design/iterations/iteration-2/` |
| GenAI 设计分析 | `docs/design/analysis/genai-design-analysis.md` |
| 设计演进 | `docs/design/analysis/design-evolution.md` |
| 实现偏差 | `docs/design/analysis/implementation-deviation-record.md` |
| 外部设计评审 | `codex_review/` |
| 报告 | `Assignment1_Report_CJLU2026.docx` |
| 原始作业说明 | `Projects1and2.pdf` |
| 种子数据 | `data/` |

---

## 8. 建议的接手顺序

1. **第一天：** 构建、运行、登录、重置数据；读需求第 10 节和偏差记录。
2. **第二天：** 重画用例图 + 最终 5 张类图；先画领域图，再画服务图，最后 UI/持久化。
3. **第三天：** 补 UC-16；跑冒烟 + 权限 + 持久化测试。
4. **第四天：** 跑完 Must 验收项，记录缺陷和证据。
5. **第五天：** 写测试报告；开始改报告 1、2、3.1–3.6。
6. **第六天：** 附录 A/B；检查 2.6/3.7 是否由学生本人完成。
7. **第七天：** 更新目录、检查图、恢复 `data/`、录视频、打包 ZIP。
