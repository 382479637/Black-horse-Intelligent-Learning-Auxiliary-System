package com.pdc.learning.ui.view;

import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Course;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.service.ClassService;
import com.pdc.learning.service.CourseService;
import com.pdc.learning.service.EmployeeService;
import com.pdc.learning.service.ScoreService;
import com.pdc.learning.service.Session;
import com.pdc.learning.service.TeachingService;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.ui.menu.Menu;
import com.pdc.learning.util.Validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教学任务管理界面。
 */
public class TeachingUI {

    private static final String[] HEADERS = {
            "任务 ID", "班级", "课程编号", "课程名称", "任课教师", "已录成绩"
    };

    private final TeachingService teachingService;
    private final ClassService classService;
    private final CourseService courseService;
    private final EmployeeService employeeService;
    private final ScoreService scoreService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;
    private final Session session;

    public TeachingUI(TeachingService teachingService,
                      ClassService classService,
                      CourseService courseService,
                      EmployeeService employeeService,
                      ScoreService scoreService,
                      ConsoleIO io,
                      TablePrinter tablePrinter,
                      Session session) {
        this.teachingService = teachingService;
        this.classService = classService;
        this.courseService = courseService;
        this.employeeService = employeeService;
        this.scoreService = scoreService;
        this.io = io;
        this.tablePrinter = tablePrinter;
        this.session = session;
    }

    public void show() {
        Menu menu = new Menu(session.isAdmin() ? "教学任务管理" : "我的教学任务");
        menu.addItem("1", "查看班级教学任务", this::listByClass);
        menu.addItem("2", "查看我的教学任务", this::listMine);
        if (session.isAdmin()) {
            menu.addItem("3", "为班级添加课程", this::addCourse);
            menu.addItem("4", "分配/更换任课教师", this::assignTeacher);
            menu.addItem("5", "移除班级课程", this::removeCourse);
        }
        menu.run(io);
    }

    private void listByClass() {
        showClasses();
        long classId = UiSupport.readId(io, "班级 ID（0 返回）：", "班级 ID");
        if (classId == 0) {
            return;
        }
        showClassCourses(teachingService.listByClass(classId));
    }

    private void listMine() {
        long teacherId;
        if (session.isTeacher()) {
            teacherId = session.teacherId();
        } else {
            teacherId = UiSupport.readId(io, "教师 ID（0 返回）：", "教师 ID");
            if (teacherId == 0) {
                return;
            }
        }
        showClassCourses(teachingService.listByTeacher(teacherId));
    }

    private void addCourse() {
        showClasses();
        long classId = UiSupport.readId(io, "班级 ID（0 返回）：", "班级 ID");
        if (classId == 0) {
            return;
        }
        showCourses();
        long courseId = UiSupport.readId(io, "课程 ID（0 返回）：", "课程 ID");
        if (courseId == 0) {
            return;
        }
        ClassCourse created = teachingService.addCourse(classId, courseId);
        io.println("已添加教学任务（ID " + created.getId() + "），当前为待分配教师。");
    }

    private void assignTeacher() {
        Long classId = chooseClassId();
        if (classId == null) {
            return;
        }
        showClassCourses(teachingService.listByClass(classId));
        long classCourseId = UiSupport.readId(io, "教学任务 ID（0 返回）：", "教学任务 ID");
        if (classCourseId == 0) {
            return;
        }
        String raw = io.readLine("任课教师 ID（输入 0 表示待分配）：");
        if (raw.isBlank()) {
            io.println("已取消。");
            return;
        }
        Long teacherId = raw.trim().equals("0") ? null
                : Validators.requireId(raw, "教师 ID");
        teachingService.assignTeacher(classCourseId, teacherId);
        io.println("任课教师已更新。");
    }

    private void removeCourse() {
        Long classId = chooseClassId();
        if (classId == null) {
            return;
        }
        showClassCourses(teachingService.listByClass(classId));
        long classCourseId = UiSupport.readId(io, "要移除的教学任务 ID（0 返回）：", "教学任务 ID");
        if (classCourseId == 0) {
            return;
        }
        if (!io.confirm("确认移除该教学任务？")) {
            io.println("已取消。");
            return;
        }
        teachingService.removeCourse(classCourseId);
        io.println("教学任务已移除。");
    }

    private Long chooseClassId() {
        showClasses();
        long classId = UiSupport.readId(io, "先选择班级 ID（0 返回）：", "班级 ID");
        return classId == 0 ? null : classId;
    }

    private void showClassCourses(List<ClassCourse> classCourses) {
        Map<Long, Course> courses = new HashMap<>();
        for (Course course : courseService.list()) {
            courses.put(course.getId(), course);
        }
        Map<Long, SchoolClass> classes = new HashMap<>();
        for (SchoolClass schoolClass : classService.list()) {
            classes.put(schoolClass.getId(), schoolClass);
        }
        Map<Long, String> teachers = new HashMap<>();
        for (Employee employee : employeeService.search("", null, null, null)) {
            teachers.put(employee.getId(), employee.getName());
        }
        List<String[]> rows = new ArrayList<>();
        for (ClassCourse classCourse : classCourses) {
            Course course = courses.get(classCourse.getCourseId());
            SchoolClass schoolClass = classes.get(classCourse.getClassId());
            int scoreCount = scoreService.listByClassCourse(classCourse.getId()).size();
            rows.add(new String[]{
                    String.valueOf(classCourse.getId()),
                    schoolClass == null ? "-"
                            : schoolClass.getCode() + " " + schoolClass.getName(),
                    course == null ? "-" : course.getCode(),
                    course == null ? "-" : course.getName(),
                    classCourse.getTeacherId() == null ? "待分配"
                            : teachers.getOrDefault(classCourse.getTeacherId(), "-"),
                    String.valueOf(scoreCount)
            });
        }
        tablePrinter.showPaged(rows, HEADERS, io);
    }

    private void showClasses() {
        List<String[]> rows = new ArrayList<>();
        for (SchoolClass schoolClass : classService.list()) {
            rows.add(new String[]{
                    String.valueOf(schoolClass.getId()),
                    schoolClass.getCode(),
                    schoolClass.getName(),
                    schoolClass.getStatus().name()
            });
        }
        tablePrinter.showPaged(rows, new String[]{"ID", "编号", "名称", "状态"}, io);
    }

    private void showCourses() {
        List<String[]> rows = new ArrayList<>();
        for (Course course : courseService.list()) {
            rows.add(new String[]{
                    String.valueOf(course.getId()),
                    course.getCode(),
                    course.getName()
            });
        }
        tablePrinter.showPaged(rows, new String[]{"ID", "课程编号", "课程名称"}, io);
    }
}
