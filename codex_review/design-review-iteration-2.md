# Iteration 2 设计复审与优化报告

## 审查范围

本轮对照 `.claude/skills/pdc-project1-architecture-standards/SKILL.md`、`.claude/skills/pdc-project1-development-process/SKILL.md`、需求基线 `docs/requirements/requirements-analysis.md` v0.4，复核 `docs/design/README.md`、架构设计、用例模型、Iteration 2 类图文档、GenAI 分析、演进说明及 Iteration 2 PlantUML 图源。没有审阅学生自撰反思。

## 结论

第二轮已解决上一轮主要结构问题，整体设计与需求基本一致，暂未发现阻断进入实现阶段的架构缺陷。本次复审确认一项中风险需求遗漏并已补正：员工邮箱虽在需求和员工资料流程中出现，Iteration 2 的 `Employee` 却没有邮箱属性。源文档、图源和架构实体职责表已经同步补入可选邮箱及更新职责。

仍有一项交付一致性待完成：领域图 PNG 尚未按更新后的 PlantUML 源重新渲染；另有一处状态文字需在确认冻结状态后统一。两项不改变领域设计结论，但提交报告前应处理。

## 发现与处理

### 中风险：员工邮箱没有进入领域模型 — 已修正

需求 FR-EMP-03/04/10 允许新增、修改可选邮箱，字段清单也将 `email` 列为 Employee 数据；但 Iteration 2 的 `Employee` 类没有 `email` 属性，也没有承载更新的领域操作。虽然 `Validators.email` 和员工服务职责已经出现，单靠校验器仍不足以表达员工实体实际保存该字段。

已将可空的 `Employee.email` 与 `updateEmail(String)` 加入领域类目录及 `class-domain-iteration-2.puml`，并在架构实体职责表中记录。邮箱格式由 service 校验，Employee 保存更新值；未将邮箱放到 `Person`，因为当前需求只为员工定义邮箱字段。

修订位置：`docs/design/class-diagram-iteration-2-final.md`、`docs/design/diagrams/class-domain-iteration-2.puml`、`docs/design/architecture-design.md`、`docs/design/design-evolution.md`。

### 低风险：PNG 图尚未跟随图源更新 — 待渲染

`diagrams/png/class-domain-iteration-2.png` 是现有图源的渲染产物。本轮修改了对应 `.puml`，所以 PNG 现在不能作为修改后领域模型的准确呈现。检查环境未发现 PlantUML 命令、JAR 或 VS Code PlantUML 扩展，未能在本轮重新导出。提交前应使用 PlantUML 按 `docs/design/README.md` 的方式重渲染 `class-domain-iteration-2.puml`，并检查 `Employee.email` / `updateEmail` 出现在图中。

### 低风险：冻结状态说明尚未统一 — 待确认后处理

`README.md` 和 `genai-design-analysis.md` 仍有“待学生确认后冻结”的表述；Iteration 2 文档将自身称为最终设计，同时又将冻结条件设为学生确认。若学生已完成确认，应统一这些状态文字；若尚未确认，保留待确认状态即可。该项是流程元数据，不影响设计模型本身。学生应自行决定并维护确认状态。

## 已复核且基本成立的设计点

- `Score` 只有 `Student` 作为组合拥有者，`ClassCourse—Score` 是普通关联；教学任务存在成绩时拒绝删除，避免双重生命周期规则冲突。
- UML 多重性使用显式 `0..*`；空部门、空班级和无成绩学生均可表达，没有把父端 `1` 错读成至少拥有一个子对象。
- 成绩唯一性统一为 `(studentId, courseId)`，由 `ClassCourse` 解析课程；加载、服务录入/更新和转班后的历史成绩说明保持一致。
- `FINISHED` 班级归档、学生删除保护、教学任务移除保护与成绩更正/删除权限在需求和服务设计中有对应约束。
- 统计 DTO 将 `DataStatus`、可空指标与空列表语义分开；教师可见范围导致的数据不足也有说明。
- 仓储类型约束为 `T extends Identifiable`；菜单工厂按管理员和教师职责拆分；用例追溯中包含教师只读课程目录 `UC-06a`。

## 验证与限制

- 已逐项对照需求字段、服务规则、领域类目录和图源；邮箱缺口修订已核对回读。
- 本轮是设计文档审查和修订，没有实现代码可供构建或运行测试。
- PlantUML 图源已修改，但没有可用渲染工具，PNG 更新待完成。
- 未审阅学生自撰反思；本报告不替代该工件。
