package com.pdc.learning.ui.view;

import com.pdc.learning.dto.ClassStatistics;
import com.pdc.learning.dto.CourseStatistics;
import com.pdc.learning.dto.RankEntry;
import com.pdc.learning.dto.StudentStatistics;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Student;
import com.pdc.learning.service.ClassService;
import com.pdc.learning.service.StatisticsService;
import com.pdc.learning.service.StudentService;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.ui.menu.Menu;
import com.pdc.learning.util.Dates;

import java.util.ArrayList;
import java.util.List;

/**
 * 统计分析界面。可见范围由 service 按 Session 限定。
 */
public class StatisticsUI {

    private static final String[] COURSE_HEADERS = {
            "课程编号", "课程名称", "任课教师", "已录成绩", "平均分", "及格率", "最高", "最低"
    };
    private static final String[] RANK_HEADERS = {
            "排名", "学号", "姓名", "平均分", "不及格门数"
    };
    private static final String[] DETAIL_HEADERS = {
            "班级", "课程编号", "课程名称", "分数", "更新时间"
    };

    private final StatisticsService statisticsService;
    private final ClassService classService;
    private final StudentService studentService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;

    public StatisticsUI(StatisticsService statisticsService,
                        ClassService classService,
                        StudentService studentService,
                        ConsoleIO io,
                        TablePrinter tablePrinter) {
        this.statisticsService = statisticsService;
        this.classService = classService;
        this.studentService = studentService;
        this.io = io;
        this.tablePrinter = tablePrinter;
    }

    public void show() {
        Menu menu = new Menu("统计分析");
        menu.addItem("1", "班级统计", this::showClassStatistics);
        menu.addItem("2", "学生统计", this::showStudentStatistics);
        menu.addItem("3", "班级排名", this::showClassRanking);
        menu.run(io);
    }

    /**
     * 班级统计，供“统计与预警 → 班级统计”直接调用。
     */
    public void showClassStatistics() {
        SchoolClass schoolClass = chooseClass();
        if (schoolClass == null) {
            return;
        }
        ClassStatistics statistics = statisticsService.classStatistics(schoolClass.getId());
        io.println();
        io.println("班级：" + statistics.className()
                + "    学生数：" + statistics.studentCount()
                + "    已录成绩：" + statistics.scoreCount() + " 条");
        io.println("平均分：" + UiSupport.formatScore(statistics.average())
                + "    及格率：" + UiSupport.formatRate(statistics.passRate())
                + "    最高：" + UiSupport.formatScore(statistics.highest())
                + "    最低：" + UiSupport.formatScore(statistics.lowest()));

        io.println();
        io.println("课程明细");
        List<String[]> rows = new ArrayList<>();
        for (CourseStatistics course : statistics.courses()) {
            rows.add(new String[]{
                    course.courseCode(),
                    course.courseName(),
                    course.teacherName() == null ? "待分配" : course.teacherName(),
                    String.valueOf(course.scoreCount()),
                    UiSupport.formatScore(course.average()),
                    UiSupport.formatRate(course.passRate()),
                    UiSupport.formatScore(course.highest()),
                    UiSupport.formatScore(course.lowest())
            });
        }
        tablePrinter.showPaged(rows, COURSE_HEADERS, io);

        io.println();
        io.println("学生排名");
        showRankingRows(statisticsService.classRanking(schoolClass.getId()));
    }

    /**
     * 学生统计，供“统计与预警 → 学生统计”直接调用。
     */
    public void showStudentStatistics() {
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
        StudentStatistics statistics = statisticsService.studentStatistics(student.getId());
        io.println();
        io.println("学生：" + statistics.studentNo() + " " + statistics.name()
                + "    已录成绩：" + statistics.scoreCount() + " 条");
        if (statistics.rank() != null) {
            io.println("平均分：" + UiSupport.formatScore(statistics.average())
                    + "    不及格门数：" + statistics.failedCount()
                    + "    当前班级排名：" + statistics.rank());
        } else {
            io.println("平均分：" + UiSupport.formatScore(statistics.average())
                    + "    不及格门数："
                    + (statistics.failedCount() == null ? "数据不足"
                    : statistics.failedCount())
                    + "    当前班级排名：" + statistics.rankNote());
        }

        io.println();
        io.println("课程成绩明细");
        List<String[]> rows = new ArrayList<>();
        for (var detail : statistics.details()) {
            rows.add(new String[]{
                    detail.className(),
                    detail.courseCode(),
                    detail.courseName(),
                    String.valueOf(detail.score()),
                    Dates.formatDateTime(detail.updatedAt())
            });
        }
        if (rows.isEmpty()) {
            io.println("暂无成绩明细。");
        } else {
            tablePrinter.showPaged(rows, DETAIL_HEADERS, io);
        }
    }

    private void showClassRanking() {
        SchoolClass schoolClass = chooseClass();
        if (schoolClass == null) {
            return;
        }
        io.println();
        io.println("班级排名：" + schoolClass.getCode() + " " + schoolClass.getName());
        showRankingRows(statisticsService.classRanking(schoolClass.getId()));
    }

    private void showRankingRows(List<RankEntry> ranking) {
        List<String[]> rows = new ArrayList<>();
        for (RankEntry entry : ranking) {
            rows.add(new String[]{
                    String.valueOf(entry.rank()),
                    entry.studentNo(),
                    entry.name(),
                    UiSupport.formatScore(entry.average()),
                    String.valueOf(entry.failedCount())
            });
        }
        if (rows.isEmpty()) {
            io.println("本班暂无排名数据。");
        } else {
            tablePrinter.showPaged(rows, RANK_HEADERS, io);
        }
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
