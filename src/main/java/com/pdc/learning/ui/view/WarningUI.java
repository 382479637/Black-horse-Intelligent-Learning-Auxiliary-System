package com.pdc.learning.ui.view;

import com.pdc.learning.dto.ClassWarning;
import com.pdc.learning.dto.ClassWarningLevel;
import com.pdc.learning.dto.WarningLevel;
import com.pdc.learning.dto.WarningResult;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Student;
import com.pdc.learning.service.ClassService;
import com.pdc.learning.service.StudentService;
import com.pdc.learning.service.WarningService;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.ui.menu.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习预警界面。教师仅能看到自己任课班级与本人课程口径的预警。
 */
public class WarningUI {

    private static final String[] HEADERS = {
            "学号", "姓名", "平均分", "不及格门数", "等级", "触发规则"
    };

    private final WarningService warningService;
    private final ClassService classService;
    private final StudentService studentService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;

    public WarningUI(WarningService warningService,
                     ClassService classService,
                     StudentService studentService,
                     ConsoleIO io,
                     TablePrinter tablePrinter) {
        this.warningService = warningService;
        this.classService = classService;
        this.studentService = studentService;
        this.io = io;
        this.tablePrinter = tablePrinter;
    }

    public void show() {
        Menu menu = new Menu("学习预警");
        menu.addItem("1", "班级预警", this::classWarning);
        menu.addItem("2", "班级学生预警", this::studentWarnings);
        menu.addItem("3", "单个学生预警", this::singleStudent);
        menu.run(io);
    }

    private void classWarning() {
        SchoolClass schoolClass = chooseClass();
        if (schoolClass == null) {
            return;
        }
        ClassWarning warning = warningService.classWarning(schoolClass.getId());
        io.println();
        io.println("班级：" + warning.className());
        io.println("预警等级：" + UiSupport.classLevelText(warning.level()));
        if (warning.level() == ClassWarningLevel.INSUFFICIENT_DATA) {
            io.println("班级可见范围内暂无成绩，本次不参与等级比较。");
        } else {
            io.println("平均分：" + UiSupport.formatScore(warning.average())
                    + "    及格率：" + UiSupport.formatRate(warning.passRate())
                    + "    已录成绩：" + warning.scoreCount() + " 条");
        }
    }

    private void studentWarnings() {
        SchoolClass schoolClass = chooseClass();
        if (schoolClass == null) {
            return;
        }
        String filterRaw = io.readLine(
                "等级筛选 [1 重度 / 2 中度 / 3 轻度 / 4 正常 / 5 数据不足 / 0 全部]：");
        WarningLevel filter = UiSupport.parseLevelFilter(filterRaw);
        List<WarningResult> results = warningService.studentWarnings(schoolClass.getId());
        List<String[]> rows = new ArrayList<>();
        for (WarningResult result : results) {
            if (filter != null && result.level() != filter) {
                continue;
            }
            rows.add(new String[]{
                    result.studentNo(),
                    result.name(),
                    UiSupport.formatScore(result.average()),
                    result.failedCount() == null ? "-" : String.valueOf(result.failedCount()),
                    UiSupport.levelText(result.level()),
                    String.join("；", result.hitRules())
            });
        }
        io.println();
        io.println("班级：" + schoolClass.getCode() + " " + schoolClass.getName());
        if (rows.isEmpty()) {
            io.println("无符合条件的预警记录。");
            return;
        }
        tablePrinter.showPaged(rows, HEADERS, io);
    }

    private void singleStudent() {
        String studentNo = io.readLine("学号（空行返回）：").trim();
        if (studentNo.isEmpty()) {
            return;
        }
        Student student = studentService.search(studentNo, null, null).stream()
                .filter(item -> item.getStudentNo().equalsIgnoreCase(studentNo))
                .findFirst().orElse(null);
        if (student == null) {
            io.println("未找到可见学生：" + studentNo);
            return;
        }
        WarningResult result = warningService.evaluateStudent(student.getId());
        io.println();
        io.println("学生：" + result.studentNo() + " " + result.name());
        io.println("预警等级：" + UiSupport.levelText(result.level()));
        if (result.level() == WarningLevel.INSUFFICIENT_DATA) {
            io.println("可见范围内暂无成绩，判定为数据不足。");
            return;
        }
        io.println("平均分：" + UiSupport.formatScore(result.average())
                + "    不及格门数：" + result.failedCount());
        io.println("触发规则：" + (result.hitRules().isEmpty()
                ? "无（正常）" : String.join("；", result.hitRules())));
    }

    private SchoolClass chooseClass() {
        List<SchoolClass> classes = classService.list();
        if (classes.isEmpty()) {
            io.println("暂无可见班级。");
            return null;
        }
        List<String[]> rows = new ArrayList<>();
        for (SchoolClass schoolClass : classes) {
            rows.add(new String[]{
                    String.valueOf(schoolClass.getId()),
                    schoolClass.getCode(),
                    schoolClass.getName(),
                    schoolClass.getStatus().name()
            });
        }
        tablePrinter.showPaged(rows, new String[]{"ID", "编号", "名称", "状态"}, io);
        long id = UiSupport.readId(io, "班级 ID（0 返回）：", "班级 ID");
        if (id == 0) {
            return null;
        }
        return classes.stream().filter(item -> item.getId() == id)
                .findFirst().orElse(null);
    }
}
