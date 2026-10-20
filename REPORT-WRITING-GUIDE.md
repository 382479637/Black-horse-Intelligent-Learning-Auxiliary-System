# 报告续写建议

**适用文档：** `Assignment1_Report_CJLU2026.docx`
**读者：** 接手报告续写的组员
**编写日期：** 2026-10-20
**配套文档：** [`HANDOVER.md`](HANDOVER.md)

> 本文只给“写什么、补哪里、怎么检查”的建议，不提供 2.6 和 3.7 的可粘贴文本。这两节必须由学生本人撰写。

---

## 1. 先做版本隔离

不要直接覆盖当前报告。建议：

1. 保留 `Assignment1_Report_CJLU2026.docx` 不动，作为交接基线。
2. 复制一份为：

   ```text
   Assignment1_Report_CJLU2026_v2_20261020.docx
   ```

   日期用实际修改日期。
3. 在副本上打开 Word 的“审阅 → 修订”（Track Changes），所有改动留痕。
4. 另存一份“干净版”用于最终提交；带修订的版本只留在本地，便于对照和说明修改。
5. 如果多人同时改，按章节分工，避免两个人同时改同一个 `.docx`。

这样做满足两个目的：当前版本可追溯；修改后的版本与当前版本明确区分。

---

## 2. 当前报告状态

| 位置 | 当前状态 | 需要做什么 |
|---|---|---|
| 封面表格 | 项目名、组号、学生姓名/学号均为空 | 填写真实信息 |
| How to use this template | 模板说明仍在 | 提交前整段删除 |
| Table of contents | 字段未更新 | 最后在 Word 中 Ctrl+A → F9 |
| 1. How to build and run | 灰色示例占位 | 换成真实构建/运行命令、主类、账号 |
| 2.1 | 有内容，但多为短语条目 | 补成“做什么、给谁、为什么规模合适”的完整论证 |
| 2.2 | 10 行 FR 表，优先级和描述有偏差 | 按需求基线校正，删除模板提示行 |
| 2.6 | 已有学生文字；声明表缺学号/签名/日期 | 只能由学生本人补充；补全声明 |
| 3.1 | 已插入用例图，但无文字说明 | 替换为重画图；补 actor/边界/关系说明 |
| 3.2 | 有 scope/constraints/assumptions 条目 | 修正笔误，整理成表，核对需求第 7/8 节 |
| 3.3 | 已插入第 1 轮 5 张图 | 图注改为 Figure 2(a)–(e)，分别说明 |
| 3.4 | 只有 2 行，且都写 Accepted | 扩到 6–8 行，加入部分采纳/拒绝及理由 |
| 3.5 | 只有占位文字，**没有最终类图** | 插入重画的 5 张图；修正类/接口/包的数量 |
| 3.6 | 4 行；第一行两列内容相同 | 按 `design-evolution.md` 重写为真正的两轮对比 |
| 3.7 | 四个小节全是占位 | 只能由学生本人写；补声明 |
| A.1 | 只有 ChatGPT / GPT-5 一行 | 按实际使用的工具补全 |
| A.2 | 仍有“酒店预订系统”示例 | 删除示例，换成真实提示词记录 |
| A.3 | 只有一行示例 | 按项目部分补全 |
| Appendix B | 空白；还留着 Individual project 占位 | 按小组实际情况填写，百分比合计 100% |
| 全文 | 蓝色 GUIDANCE 框、灰色占位符仍在 | 提交前全部删除/替换 |

---

## 3. 全文通用清理

提交前逐项检查：

- [ ] 删除所有蓝色 `GUIDANCE` 说明框；
- [ ] 删除所有灰色 `[...]` 占位符；
- [ ] 删除所有 `(Add or remove rows as needed...)` 提示；
- [ ] 删除 A.2 的酒店预订示例行；
- [ ] 保留所有编号标题，不要因为“没有内容”就删标题；
- [ ] 更新目录：Word 中 Ctrl+A，然后 F9；
- [ ] 图注编号连续：Figure 1、Figure 2(a)–(e)、Figure 3(a)–(e)；
- [ ] 图片在打印预览中能看清类名、关系标签和中文；
- [ ] 全文语言一致：正文用英文；引用的中文提示词或界面文字可以保留，但最好加简短英文说明；
- [ ] 术语统一：`ClassCourse` 不要一会儿写“教学任务”、一会儿写“班级课程”而不解释；
- [ ] 不要出现密码哈希、盐、真实个人手机号/邮箱；
- [ ] 不要让报告里出现 AI 生成的 2.6/3.7 段落。

---

## 4. 逐节建议

### 4.1 封面与 1. Project Setup

**封面表格：**

- Project title：`黑马智能学习辅助系统 / Intelligent Learning Assistant System`
- Group ID：按 Canvas 上的组号填写
- Student 1 / Student 2：姓名 + 学号，和 Appendix B 一致

**1. How to build and run：**

评分要求“marker 不需要手工配置就能构建运行”。建议写清楚：

- 构建工具：Maven；
- JDK：21 或更高；
- 主类：`com.pdc.learning.app.Main`；
- 构建命令和运行命令；
- 必须从项目根目录运行（因为 `data/` 是相对路径）；
- 演示账号；
- 首次运行会自动生成演示数据；
- 如何重置数据（可选，但方便 marker 复现）。

可直接使用的事实性内容：

```text
Build:  mvn clean package
Run:    java -jar target/learning-assistant.jar
Main:   com.pdc.learning.app.Main

Demo accounts:
  admin / Admin@123   (Administrator)
  t01   / Teach@123   (Teacher)
  t02   / Teach@123   (Teacher)
  t03   / Teach@123   (Teacher)
  t04   / Teach@123   (Teacher)
```

注意：

- 不要写 `mvn javafx:run` 或 `exec:java`，本项目不是 JavaFX，也没有配置 exec 插件；
- 不要要求 marker 手工建 `data/` 或导入 SQL；
- 如果写“run from project root”，要说明原因；
- 可以加一句 Windows 终端编码建议（`chcp 65001`），但不要把它写成必须步骤。

### 4.2 2.1 Project topic and scope justification

评分关注两点：**题目是否合适**、**规模是否既有趣又能完成**。当前内容有素材，但偏条目化。建议按下面四块组织：

1. **What / Who**：系统做什么，给谁用（管理员、教师；没有学生端）。
2. **Why interesting**：解决什么真实问题；亮点是什么（可解释的成绩预警 R1–R6，而不是“AI 黑盒”）。
3. **Why big enough**：不是只能录数据的玩具；有 7 个实体、7 个 CSV、10 个服务、2 种角色、完整闭环。
4. **Why finishable**：无数据库、无 GUI、无并发、无网络；2 人可完成；范围边界清楚。

写作提醒：

- 当前 `Justification: simple system architecture, less code and easy to demonstrate` 太笼统；用具体数字支撑“less code”和“easy to demonstrate”。
- “7 entity, 7 CSV and 10 services”可以保留，但要和最终代码一致。
- 明确“不做什么”：不做学生登录、不做选课、不做作业/考试、不做多用户/并发。这样“规模合适”才可信。
- 可引用需求文档第 7.3 节的规模假设，但不要整段复制。

### 4.3 2.2 Core functional requirements

当前表有两个主要问题：

1. **优先级与需求基线不一致**：FR-2～FR-6 写成 `Secondary`，但需求基线中部门、员工、课程、班级、教学任务都是 Must。报告里的“Secondary”会让评阅者以为这些模块可删。
2. **描述太窄或有笔误**：
   - FR-2 只写 “Department & Employee display”，实际部门管理包含增删改查；
   - FR-3 写 “Reorder password”，应为 “Reset password”，而且员工管理远不止重置密码；
   - FR-5 只写 “Head teacher & capacity”，漏了班级状态、日期、删除约束；
   - FR-6 只写 “Course & teacher”，漏了“待分配”“有成绩不能移除”；
   - FR-9 只写 “Rank for students and classes”，漏了班级/学生/课程统计；
   - FR-10 只写 “Powered by rules”，建议点明 R1–R6 和班级预警。

建议的核对表（模块级用 Essential；Should 子项单独标 Desirable）：

| 报告 FR | 建议优先级 | 描述应覆盖 |
|---|---|---|
| FR-1 登录/登出/改密 | Essential | 角色菜单、统一错误提示、禁用账号、修改密码 |
| FR-2 部门管理 | Essential | 增删改查、重名拒绝、有关联员工时不能删；搜索为 Desirable |
| FR-3 员工管理 | Essential | 增删改查、启停、角色、重置密码（Desirable）、引用保护 |
| FR-4 课程管理 | Essential | 增删改查、被引用不能删、教师只读目录 |
| FR-5 班级管理 | Essential | 增删改查、班主任、容量、状态转换、归档保护；筛选为 Desirable |
| FR-6 教学任务 | Essential | 班级-课程-教师、待分配、有成绩不能移除 |
| FR-7 学生管理 | Essential | 增删改查、转班、状态、历史成绩归属 |
| FR-8 成绩管理 | Essential | 录入/修改/删除、0 分、唯一键、权限；批量录入为 Desirable |
| FR-9 统计分析 | Essential | 班级/学生/课程统计、排名、数据不足 |
| FR-10 学习预警 | Essential | R1–R6、取最高等级、班级预警、数据不足 |

其他建议：

- 模板要求 `Essential / Desirable`，不要混用 `Secondary`；如果一定要用 Secondary，先在表下定义它等价于 Desirable。
- 每个 FR 可以加一列“对应 AC”，或表下写 “Acceptance criteria: see Section 10 of the requirements analysis”。这样 3.x 和测试报告都能追溯。
- 删除表下的 `(Add or remove rows as needed...)` 提示。
- 这张表是“模块级需求”，不要把它写成菜单清单；菜单在 3.2 或 2.1 里说明即可。

### 4.4 2.6 Critical Reflection — Student-Authored

**这一节只能由学生本人写。** 当前已有文字，说明作者已经写了一版；接手成员不要用 AI 重写、扩写、润色到改变作者语言。

建议作者本人检查：

- 是否明确回答了三个问题：
  1. 哪些功能/需求是必需的，为什么；
  2. 哪些不必要或不现实，为什么排除；
  3. 为什么做这些设计决策。
- 每个判断是否落到具体 FR 编号；当前主要提到 FR-1、FR-7～FR-10，可以检查是否漏了部门/员工/课程/班级/教学任务。
- 是否解释了“取舍”而不只是“列表”：例如为什么不做学生端、为什么不做 AI 助手、为什么不做多用户。
- 是否把 GenAI 的建议和自己的判断区分开；如果引用 AI 原话，是否明确标成 quotation。
- 是否避免空泛句（“AI 很有帮助”“这样比较好”）。

**声明表：**

- 填 Student ID、Signature、Date；
- 只让实际作者签名；
- 如果成员有变化，先和作者确认，不能替别人签。

### 4.5 3.1 Use case diagram

当前已有一张图，但没有任何文字说明。建议：

1. 用重画后的用例图替换图片；
2. 保留图注 `Figure 1: Use case diagram`；
3. 图下加 3–5 句说明：
   - 两个 actor：管理员、教师；学生不是 actor；
   - 系统边界内是 22 个用例；
   - `UC-04a/05a/07a/09a/10a` 用 `<<extend>>`，`UC-06a` 是独立用例；
   - 不使用 `<<include>>` 的原因（登录是前置条件，不是公共子用例）；
   - 管理员和教师直接连接各自用例，不做 actor 泛化。
4. 检查 `UC-16` 是否保留并连接教师（已确认保留，后续补实现）。

不要在图里画数据库、文件系统或“开发者”作为 actor。

### 4.6 3.2 System scope, constraints and assumptions

当前内容方向正确，但建议整理成三块表格，便于评阅：

| Scope | Constraints | Assumptions |
|---|---|---|
| 单机 CUI；管理员/教师；7 个实体；成绩-统计-预警闭环 | 无数据库、无 GUI、无并发、离线、Java 21、Maven | 单用户；每员工一个部门；每学生一个当前班级；课程全校共享；分数 0–100；数据量可全部载入内存 |

需要修正的地方：

- `demonstration data is generated automatically for ;` 缺宾语，补成完整表达或删掉分号；
- `uses Chinese UTF-8` 可以改成“UTF-8 CSV / 终端编码按系统设置”；
- 明确“CSV 可手工编辑，但坏行会被跳过并记录”；
- 不要写“多用户”“容器”“并发”这些范围外内容为系统能力。

### 4.7 3.3 Class diagram — Iteration 1

当前已插入 5 张图，方向正确。需要改的是图注和说明：

- 一张图注 `Figure 2` 不能覆盖 5 张图；改成 `Figure 2(a)–(e)`，每张加小标题：
  - (a) Domain model
  - (b) Services, session and permissions
  - (c) Statistics and warning DTOs
  - (d) Persistence and utilities
  - (e) CUI menus and factories
- 保留“为什么拆成 5 张”的解释：一张图太大、连线交叉、打印不可读。
- 既然已确认**不重画第 1 轮**，就按事实写“Iteration 1 的初始设计”，不要声称这是你手工重画的。
- 检查 5 张图是否都能在打印预览中看清。

### 4.8 3.4 GenAI analysis of Iteration 1

当前只有 2 行，而且两行都写 `Accepted`。评分标准明确说：**拒绝一个坏建议并说明理由，比全部接受更能得分。**

建议：

1. 从 [`docs/design/analysis/genai-design-analysis.md`](docs/design/analysis/genai-design-analysis.md) 的 12 条结论中选 6–8 条代表性的。
2. 至少包含：
   - 2 条“采纳”（例如 Score 双组合、成绩唯一键）；
   - 2 条“部分采纳/拒绝”（例如多重性 `1..*` 的语义、`DataInitializer`/`StatisticsService` 是否拆）；
   - 1 条“过程改进”（两轮闭环、图源/PNG 不一致）。
3. `Accepted / Rejected` 列建议用：`Accepted`、`Partially accepted`、`Rejected`。
4. “Reason for your decision”要具体，不要只写 “solve conflict”；写清楚“第 1 轮是什么、问题是什么、第 2 轮怎么改、为什么”。
5. 当前 prompt 摘要里有 `***` 路径占位，必须换成真实路径或删掉；提示词可以翻译，但不要编造没有用过的提示。
6. 删除表下模板提示行。

可参考的对应关系：

| 建议 | 结论 | 第 2 轮处理 |
|---|---|---|
| `Score` 被 `Student` 和 `ClassCourse` 双组合 | 采纳 | `Student` 唯一组合；`ClassCourse` 改普通关联 |
| 多重性 `1` 被误读为父对象必须有子对象 | 部分采纳 | 多端统一 `0..*`，但不承认原图语义错误 |
| 成绩唯一键 `(studentId, courseId)` vs `(studentId, classCourseId)` | 采纳 | 统一为 `(studentId, courseId)` |
| 教师查看课程目录没有用例 | 采纳 | 新增 `UC-06a` |
| `MenuFactory`/`DataInitializer`/`StatisticsService` 可能是上帝类 | 部分采纳 | 拆 `MenuFactory`；保留后两者并说明理由 |
| `Repository<T>` 未约束 `Identifiable` | 采纳 | 改为 `<T extends Identifiable>` |
| DTO 未表达“数据不足”/`updatedAt` | 采纳 | 增加 `DataStatus`、可空指标、`updatedAt` |

### 4.9 3.5 Class diagram — Iteration 2 (final)

**这是目前最大的缺口：报告里只有占位文字，没有最终类图。**

需要做：

1. 插入重画的 5 张最终类图，图注用 `Figure 3(a)–(e)`。
2. 修正下面的文字错误：
   - “4 interfaces” → **5 个接口**：`Repository`、`CsvCodec`、`MenuAction`、`MenuFactory`、`Identifiable`；
   - “4 concrete class packages” → 实际列了 6 个包，建议写 **model、dto、persistence、service、ui、app**，另加 `util`、`exception`；
   - `Model` 枚举补 `EmployeeStatus`；
   - DTO 补 `DataStatus`、`WarningLevel`、`ClassWarningLevel`；
   - Persistence 补 `Repository`、`CsvCodec`、`CsvRepository`；
   - UI 补 `AccountUI`、`UiSupport`、`Menu/MenuItem/MenuAction`；
   - `TeacherMenuFactory` 补 `ClassUI`、`StudentUI`、`CourseUI` 依赖；
   - `DepartmentService`、`ClassService`、`TeachingService` 补 `Session`；
   - `WarningService` 补三个仓储依赖。
3. 关系说明保留并展开：
   - 继承：`Employee/Student → Person → AbstractEntity`；
   - 实现：`CsvRepository → Repository`，7 个 Codec → `CsvCodec`，两个 MenuFactory → `MenuFactory`，实体 → `Identifiable`；
   - 组合：`Student ◆— Score`；
   - 普通关联：`ClassCourse — Score`；
   - 业务关联：部门-员工、班级-学生、班级-教学任务、课程-教学任务、教师-教学任务、学生-成绩、教学任务-成绩；
   - 依赖：UI → Service，Service → Repository，LoginUI → AuthService + MenuFactory，ApplicationBootstrap → 所有层。
4. 加一句“最终类图与 `src/main/java` 对应；实现偏差见 `implementation-deviation-record.md`”。
5. 补 UC-16 实现后，`AccountUI` 增加只读资料方法，类图同步更新。

### 4.10 3.6 Summary of design evolution

当前表的第一个变更行，Iteration 1 和 Iteration 2 两列内容完全相同，看起来像复制错误。建议按 [`docs/design/analysis/design-evolution.md`](docs/design/analysis/design-evolution.md) 重写，至少包含：

| Change | Iteration 1 | Iteration 2 | Reason |
|---|---|---|---|
| Score ownership | `Student ◆— Score` 和 `ClassCourse ◆— Score` 双组合 | `Student` 唯一组合；`ClassCourse — Score` 普通关联 | 组合表示生命周期负责；删除学生级联成绩，删除教学任务被成绩阻止 |
| Multiplicity | 多端 `*` | 多端 `0..*` | 避免把“允许空集合”误读为“必须有子对象” |
| Score unique key | `(studentId, classCourseId)` | `(studentId, courseId)` | 与需求一致；转班同课程不重复建分 |
| Teacher course catalog | 无独立用例 | 新增 `UC-06a` | 落实 FR-COURSE-05；教师只读 |
| Data status | `scoreCount == 0` 隐式判断 | `DataStatus` + 可空指标 + `updatedAt` | 显式表达“数据不足”，避免空列表歧义 |
| Type/assembly | `Repository<T>`；单个 `MenuFactory` | `<T extends Identifiable>`；接口 + 两个实现 | 类型安全；按角色拆分菜单工厂 |

写作提醒：

- 每一行都要回答“改了什么”和“为什么改”，不要只写 “solve conflict”；
- 不要写“Iteration 1 和 Iteration 2 一样”；如果确实没改，就不要放在演进表里；
- 删除模板提示行。

### 4.11 3.7 Critical Reflection — Student-Authored

**这一节目前全是占位，必须由学生本人写。** 不能由 AI 生成，也不能让接手成员代写后署作者名。

四个问题必须逐一回答：

1. GenAI 提出了哪些设计建议；
2. GenAI 有哪些错误、局限或糟糕决策（要具体到类/关系/模式）；
3. 如何修正/改进；
4. 为什么最终设计更合适（OOP、SOLID、可维护性、需求契合）。

可以引用的具体事实（只作为讨论素材，不是答案）：

- `Score` 的双组合 vs `Student` 唯一组合；
- 多重性 `*` → `0..*`；
- 成绩唯一键从 `(studentId, classCourseId)` → `(studentId, courseId)`；
- 新增 `UC-06a`；
- `DataStatus`/可空指标/`updatedAt`；
- `Repository<T extends Identifiable>`；
- `MenuFactory` 拆分；
- `Employee.email` 的补入。

写作提醒：

- 不要写“AI 很有帮助”“AI 给了一些建议”这种空话；
- 每个判断尽量落到类名、关系名、模式名；
- 如果引用 AI 原话，明确标成 quotation，然后写自己的分析；
- 声明表填姓名、学号、签名、日期；只让实际作者签。

### 4.12 Appendix A: AI Usage Log

**A.1 GenAI tools used**

当前只有 `ChatGPT / GPT-5` 一行。按实际情况补：

| Tool | Model / version | Mainly used for |
|---|---|---|
| Claude Code | 按实际显示填写 | 需求整理、设计讨论、代码实现、测试脚本 |
| Codex | 按实际显示填写 | 设计评审（`codex_review/`） |
| ChatGPT | 按实际显示填写 | 需求头脑风暴、设计批判（如果确实用过） |

要求：

- 不确定版本就写 `unknown` 或工具显示的名称，不要猜；
- 只列实际用过的工具；
- 不要把“AI 辅助”写成“AI 独立完成”。

**A.2 Prompt log**

- **删除酒店预订示例行**；
- 增加 6–10 行，覆盖：需求、设计、实现、调试、重构、报告；
- 每行写：阶段、工具、主要 prompt 摘要、AI 产出、你改了什么/拒绝了什么/为什么；
- 最后一列最重要，要体现人的判断；
- 可以引用 `codex_review/` 里的真实评审结论，但不要编造 prompt；
- 删除灰色示例和 `(Add or remove rows...)` 提示。

可用的真实素材：

- `docs/design/analysis/genai-design-analysis.md` 的 12 条建议与采纳/拒绝；
- `codex_review/design-review.md`、`design-review-iteration-2.md`、`design-review-iteration-2-recheck.md`；
- `implementation-deviation-record.md` 中实现阶段修正的依赖、密码入口、摘要顺序等。

**A.3 Human vs AI contribution summary**

按部分填写，例如：

| Part | Mainly human-designed | Mainly AI-generated (then reviewed) | Notes |
|---|---|---|---|
| Requirements scope | 按实际 | 按实际 | 学生决定保留/排除哪些功能 |
| Class structure | 按实际 | 按实际 | 两轮设计、拒绝/修正了哪些建议 |
| Implementation | 按实际 | 按实际 | AI 生成后人工修改、调试、验收 |
| Testing | 按实际 | 按实际 | 测试用例与执行由谁完成 |
| Report | 按实际 | 按实际 | 2.6/3.7 必须学生本人写 |
| Video | 按实际 | 按实际 | 演示与讲解由谁完成 |

不要把所有部分都写成 “AI-generated”，也不要虚构“全部人工”。

### 4.13 Appendix B: Contribution Report

- 填成员姓名、学号、主要贡献、百分比；
- 百分比合计必须是 100%；
- 贡献描述覆盖设计、代码、测试、报告、视频；
- 如果成员有变化，同步更新 2.6、3.7 声明表中的姓名；
- 如果是小组，删除 “Individual project — all work completed by the named student.” 这行占位；
- 贡献要和 AI Usage Log、Git/文件记录大致一致，不要互相矛盾。

---

## 5. 报告内容的证据来源

| 报告章节 | 主要来源 |
|---|---|
| 1. Project Setup | `README.md`、`pom.xml`、实际构建/运行结果 |
| 2.1 | `docs/requirements/requirements-analysis.md` 第 1、7.3 节 |
| 2.2 | 需求文档第 3 节 FR 表、第 10 节 AC |
| 2.6 | 学生本人；`Student-Authored Critical Reflection.md` 仅作个人草稿，不要 AI 改写 |
| 3.1 | `docs/design/use-case/use-case-model.md`、`use-case.puml` |
| 3.2 | 需求文档第 7、8 节；`architecture-design.md` 第 1.2、1.3 节 |
| 3.3 | `docs/design/iterations/iteration-1/` |
| 3.4 | `docs/design/analysis/genai-design-analysis.md`、`codex_review/` |
| 3.5 | `docs/design/iterations/iteration-2/`、`implementation-deviation-record.md` |
| 3.6 | `docs/design/analysis/design-evolution.md` |
| 3.7 | 学生本人；不要从设计分析文档直接复制成反思 |
| Appendix A | 实际提示词记录、`codex_review/`、偏差记录 |
| Appendix B | 小组实际分工记录 |

---

## 6. 提交前检查清单

- [ ] 在干净环境执行 `mvn clean package` 成功；
- [ ] 按 1. Project Setup 的命令能启动并登录；
- [ ] 所有编号标题保留；
- [ ] 所有 GUIDANCE 框、灰色占位符、示例行已删除；
- [ ] 目录已更新（Ctrl+A → F9）；
- [ ] 3.1 用例图已替换为重画版本；
- [ ] 3.3 第 1 轮 5 张图图注为 Figure 2(a)–(e)；
- [ ] 3.5 已插入最终 5 张图，图注为 Figure 3(a)–(e)；
- [ ] 3.5 的接口/包/类数量与代码一致；
- [ ] 3.6 每行都有“第 1 轮 / 第 2 轮 / 原因”；
- [ ] 2.6、3.7 由学生本人完成并签字；
- [ ] 附录 A 的工具有实际使用记录，A.2 没有酒店示例；
- [ ] 附录 B 百分比合计 100%；
- [ ] 全文没有密码哈希、盐、真实个人联系方式；
- [ ] 报告导出 PDF 后图片清晰、分页正常；
- [ ] 最终 ZIP 包含源码、报告、视频，文件名符合课程要求；
- [ ] `data/` 已恢复为干净种子。

---

## 7. 不要做的事

1. **不要用 AI 写 2.6 或 3.7。** 这两节是评分中的“学生自撰”部分，AI 生成文本会直接违反要求。
2. **不要伪造重画记录或测试结果。** 没画的图、没跑的测试，不要写成已完成。
3. **不要为了“完整”加入 JUnit、数据库、GUI。** 这些是 Project 2 的内容，Project 1 不需要。
4. **不要静默修改冻结设计文档。** 如果实现和图不一致，按 `implementation-deviation-record.md` 的方式记录；不要只改代码或只改图。
5. **不要覆盖原始报告。** 保留 `Assignment1_Report_CJLU2026.docx` 作为基线，修改版另存。
6. **不要把模板示例当成项目事实。** 酒店预订、`ChatGPT GPT-5` 如果没实际使用，不能留在提交版里。
7. **不要把“教师查看本人资料”从用例图删掉。** 已确认保留 UC-16，后续补实现；删掉会让用例模型、报告和需求三者不一致。

---

## 8. 一句话总结

**先把图和代码对齐，再把测试跑实，最后写报告；2.6 和 3.7 留给学生本人，其他部分按本指南逐节替换占位符。**
