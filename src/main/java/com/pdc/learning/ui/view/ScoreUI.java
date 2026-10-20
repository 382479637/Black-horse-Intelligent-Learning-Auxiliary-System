package com.pdc.learning.ui.view;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.exception.DataAccessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Course;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.Student;
import com.pdc.learning.service.ClassService;
import com.pdc.learning.service.CourseService;
import com.pdc.learning.service.EmployeeService;
import com.pdc.learning.service.ScoreService;
import com.pdc.learning.service.StudentService;
import com.pdc.learning.service.TeachingService;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.util.Dates;
import com.pdc.learning.util.Validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩管理界面：选择班级 → 选择教学任务 → 查看与维护成绩。
 */
public class ScoreUI {

    private static final String[] SCORE_HEADERS = {
            "成绩 ID", "学号", "姓名", "分数", "更新时间"
    };
    private static final String[] STUDENT_HEADERS = {
            "学号", "姓名", "当前成绩"
    };

    private final ScoreService scoreService;
    private final ClassService classService;
    private final TeachingService teachingService;
    private final StudentService studentService;
    private final CourseService courseService;
    private final EmployeeService employeeService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;

    public ScoreUI(ScoreService scoreService,
                   ClassService classService,
                   TeachingService teachingService,
                   StudentService studentService,
                   CourseService courseService,
                   EmployeeService employeeService,
                   ConsoleIO io,
                   TablePrinter tablePrinter) {
        this.scoreService = scoreService;
        this.classService = classService;
        this.teachingService = teachingService;
        this.studentService = studentService;
        this.courseService = courseService;
        this.employeeService = employeeService;
        this.io = io;
        this.tablePrinter = tablePrinter;
    }

    public void show() {
        while (true) {
            List<SchoolClass> classes = classService.list();
            if (classes.isEmpty()) {
                io.println("暂无可操作的班级。");
                return;
            }
            showClasses(classes);
            long classId = UiSupport.readId(io, "班级 ID（0 返回）：", "班级 ID");
            if (classId == 0) {
                return;
            }
            SchoolClass schoolClass = classes.stream()
                    .filter(item -> item.getId() == classId)
                    .findFirst().orElse(null);
            if (schoolClass == null) {
                io.println("班级不存在或无权访问。");
                continue;
            }
            List<ClassCourse> classCourses = teachingService.listByClass(classId);
            if (classCourses.isEmpty()) {
                io.println("该班级暂无教学任务。");
                continue;
            }
            boolean backToClasses = false;
            while (!backToClasses) {
                showClassCourses(schoolClass, classCourses);
                long classCourseId = UiSupport.readId(
                        io, "教学任务 ID（0 返回班级列表）：", "教学任务 ID");
                if (classCourseId == 0) {
                    break;
                }
                ClassCourse classCourse = classCourses.stream()
                        .filter(item -> item.getId() == classCourseId)
                        .findFirst().orElse(null);
                if (classCourse == null) {
                    io.println("教学任务不存在或无权访问。");
                    continue;
                }
                runCourseActions(schoolClass, classCourse);
            }
        }
    }

    private void runCourseActions(SchoolClass schoolClass, ClassCourse classCourse) {
        while (true) {
            showScores(classCourse);
            int choice = io.readInt(
                    "1 录入/修改（按学号） / 2 修改（按成绩 ID） / 3 删除 / 4 批量录入 / 0 返回：",
                    0, 4);
            if (choice == 0) {
                return;
            }
            try {
                switch (choice) {
                    case 1 -> record(classCourse);
                    case 2 -> updateScore(classCourse);
                    case 3 -> deleteScore(classCourse);
                    default -> batchRecord(schoolClass, classCourse);
                }
            } catch (BusinessException e) {
                io.println("操作失败：" + e.getMessage());
            } catch (DataAccessException e) {
                io.println("数据访问失败：" + e.getMessage());
            }
        }
    }

    private void record(ClassCourse classCourse) {
        String studentNo = io.readLine("学号（空行取消）：").trim();
        if (studentNo.isEmpty()) {
            io.println("已取消。");
            return;
        }
        List<Student> students = studentService.search(studentNo, null, null);
        Student student = students.stream()
                .filter(item -> item.getStudentNo().equalsIgnoreCase(studentNo))
                .findFirst().orElse(null);
        if (student == null) {
            io.println("未找到可见学生：" + studentNo);
            return;
        }
        String scoreRaw = io.readLine("分数 0–100（0 分有效，空行取消）：");
        if (scoreRaw.isBlank()) {
            io.println("已取消。");
            return;
        }
        double value = Validators.requireScore(scoreRaw);
        Score saved = scoreService.record(student.getId(), classCourse.getId(), value);
        io.println("成绩已保存：" + student.getStudentNo() + " " + saved.getScore() + " 分");
    }

    private void updateScore(ClassCourse classCourse) {
        long scoreId = UiSupport.readId(io, "成绩 ID（0 返回）：", "成绩 ID");
        if (scoreId == 0) {
            return;
        }
        String scoreRaw = io.readLine("新分数 0–100（空行取消）：");
        if (scoreRaw.isBlank()) {
            io.println("已取消。");
            return;
        }
        double value = Validators.requireScore(scoreRaw);
        scoreService.update(scoreId, value);
        io.println("成绩已更新。");
    }

    private void deleteScore(ClassCourse classCourse) {
        long scoreId = UiSupport.readId(io, "成绩 ID（0 返回）：", "成绩 ID");
        if (scoreId == 0) {
            return;
        }
        if (!io.confirm("确认删除该成绩？")) {
            io.println("已取消。");
            return;
        }
        scoreService.delete(scoreId);
        io.println("成绩已删除。");
    }

    private void batchRecord(SchoolClass schoolClass, ClassCourse classCourse) {
        List<Score> scores = scoreService.listByClassCourse(classCourse.getId());
        Map<Long, Double> scoreByStudent = new HashMap<>();
        for (Score score : scores) {
            scoreByStudent.put(score.getStudentId(), score.getScore());
        }
        List<Student> students = studentService.listByClass(schoolClass.getId());
        List<String[]> rows = new ArrayList<>();
        for (Student student : students) {
            Double value = scoreByStudent.get(student.getId());
            rows.add(new String[]{
                    student.getStudentNo(),
                    student.getName(),
                    value == null ? "--" : String.valueOf(value)
            });
        }
        tablePrinter.showPaged(rows, STUDENT_HEADERS, io);

        io.println("输入“学号 分数”（如 2026002 76；0 分写作 2026002 0），"
                + "空行结束，单独输入 0 返回：");
        Map<String, Double> entries = new LinkedHashMap<>();
        while (true) {
            String line = io.readLine("> ").trim();
            if (line.isEmpty() || line.equals("0")) {
                break;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                io.println("格式不正确，应为“学号 分数”，请重新输入。");
                continue;
            }
            try {
                entries.put(parts[0], Validators.requireScore(parts[1]));
            } catch (BusinessException e) {
                io.println(parts[0] + "：" + e.getMessage() + "，请重新输入。");
            }
        }
        if (entries.isEmpty()) {
            io.println("没有可保存的分数。");
            return;
        }
        List<String> messages = scoreService.batchRecord(classCourse.getId(), entries);
        for (String message : messages) {
            io.println(message);
        }
    }

    private void showScores(ClassCourse classCourse) {
        Map<Long, Student> students = new HashMap<>();
        for (Student student : studentService.list()) {
            students.put(student.getId(), student);
        }
        List<Score> scores = scoreService.listByClassCourse(classCourse.getId());
        List<String[]> rows = new ArrayList<>();
        for (Score score : scores) {
            Student student = students.get(score.getStudentId());
            rows.add(new String[]{
                    String.valueOf(score.getId()),
                    student == null ? "-" : student.getStudentNo(),
                    student == null ? "-" : student.getName(),
                    String.valueOf(score.getScore()),
                    Dates.formatDateTime(score.getUpdatedAt())
            });
        }
        tablePrinter.showPaged(rows, SCORE_HEADERS, io);
    }

    private void showClasses(List<SchoolClass> classes) {
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
    }

    private void showClassCourses(SchoolClass schoolClass, List<ClassCourse> classCourses) {
        Map<Long, Course> courses = new HashMap<>();
        for (Course course : courseService.list()) {
            courses.put(course.getId(), course);
        }
        Map<Long, String> teachers = new HashMap<>();
        for (Employee employee : employeeService.search("", null, null, null)) {
            teachers.put(employee.getId(), employee.getName());
        }
        List<String[]> rows = new ArrayList<>();
        for (ClassCourse classCourse : classCourses) {
            Course course = courses.get(classCourse.getCourseId());
            rows.add(new String[]{
                    String.valueOf(classCourse.getId()),
                    course == null ? "-" : course.getCode(),
                    course == null ? "-" : course.getName(),
                    classCourse.getTeacherId() == null ? "待分配"
                            : teachers.getOrDefault(classCourse.getTeacherId(), "-")
            });
        }
        io.println();
        io.println("班级：" + schoolClass.getCode() + " " + schoolClass.getName());
        tablePrinter.showPaged(rows,
                new String[]{"任务 ID", "课程编号", "课程名称", "任课教师"}, io);
    }
}
