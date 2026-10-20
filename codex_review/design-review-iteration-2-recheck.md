# Iteration 2 设计复审报告（未确认稿）

## 审查范围

本轮复核需求基线 `requirements-analysis.md` v0.4 与 `docs/design/` 当前版本，包括 README、架构设计、用例模型、Iteration 2 类图文档、GenAI 分析、演进说明、Iteration 2 PlantUML 源及 PNG。未审阅学生自撰反思。

## 结论

设计主体目前基本连贯，上一轮指出的员工邮箱字段缺失已补入 `Employee`、架构说明和演进记录；领域图 PNG 也已同步显示该字段。成绩生命周期/唯一键、教师可见范围、归档规则和仓储分层未发现新的阻断性问题。

尚有一项需在学生确认前明确的设计契约：`Employee.email` 是私有字段，但未说明跨包的 `EmployeeService`、`EmployeeUI` 与 `EmployeeCsvCodec` 如何读取它。另有一处 GenAI 分析状态用语未反映第二轮候选设计已经形成。两项均不要求把设计视为已正式确认；建议学生确认或调整后再冻结。

## 发现

### 中低风险：邮箱只定义了写入行为，没有读取契约

Iteration 2 已在 `Employee` 中加入私有 `email` 与 `updateEmail(String)`，并说明邮箱格式由 `EmployeeService` 校验。演进说明进一步要求员工详情可展示邮箱，`EmployeeCsvCodec` 读写该列，员工新增/修改表单也接收该字段。但领域类目录和 `class-domain-iteration-2.puml` 没有 `getEmail()` 或等价读取方法。

由于模型规定实体属性为 private，且持久化、service、UI 分属不同包，当前设计没有说明这些调用方如何读取邮箱；特别是 CSV 格式化必须能够将邮箱写回文件。冻结前应明确读取方式，例如增加 `getEmail(): String`，或定义由 service/DTO 提供展示值、由 codec 使用明确映射机制。将选择同步到类目录与图源即可。此项是设计契约的可实现性缺口，不代表业务需求本身错误。

### 低风险：GenAI 分析状态未准确表达当前候选设计

`genai-design-analysis.md` 状态栏仍写“采纳项待学生确认后进入最终设计”，但 Iteration 2 候选设计已形成，且其元数据正确标注“待学生确认后冻结”。为避免被理解成建议尚未进入第二轮，可将状态说明为“采纳项已纳入 Iteration 2 待确认稿；待学生确认后冻结”。此修改只更新过程状态，不代表替学生确认。

## 已复核通过的事项

- `Employee.email` 已写入领域类目录、PlantUML 图源和架构实体职责说明；PNG 中也能看到 `email` 与 `updateEmail`，上轮图源/PNG 不一致的问题已处理。
- 项目明确 Iteration 2 仍待学生确认后冻结；报告按未确认稿审查，不将其表述为正式确认版本。
- `Score` 由 `Student` 唯一组合拥有，`ClassCourse` 为普通关联；删除有成绩教学任务的限制与此相符。
- 成绩业务唯一键 `(studentId, courseId)`、转班后原教学任务归属和加载校验说明基本一致。
- `FINISHED` 班级归档、删除保护、成绩维护权限以及服务层权限校验均有对应设计说明。
- `DataStatus`、可空统计指标与空集合语义分开；泛型仓储受 `Identifiable` 约束；教师课程目录有 `UC-06a` 追溯。

## 建议确认顺序

1. 决定 `Employee.email` 的读取契约，并同步类目录与图源；随后重新渲染领域图 PNG。
2. 将 GenAI 分析状态改为“已纳入待确认稿、待学生确认后冻结”的准确表述。
3. 学生本人确认迭代后，再统一 README、架构和类图文档中的冻结状态。

本轮是文档和图示复核，无实现代码可供构建或运行测试。以上意见不替代学生对 Iteration 2 的正式确认，也不代写学生自撰反思。
