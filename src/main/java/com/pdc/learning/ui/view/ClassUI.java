package com.pdc.learning.ui.view;

import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Course;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Student;
import com.pdc.learning.model.enums.ClassStatus;
import com.pdc.learning.service.ClassService;
import com.pdc.learning.service.CourseService;
import com.pdc.learning.service.EmployeeService;
import com.pdc.learning.service.Session;
import com.pdc.learning.service.StudentService;
import com.pdc.learning.service.TeachingService;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.ui.menu.Menu;
import com.pdc.learning.util.Dates;
import com.pdc.learning.util.Validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 班级管理界面。教师只能查看自己任课的班级。
 */
public class ClassUI {

    private static final String[] HEADERS = {
            "ID", "编号", "名称", "班主任", "开始", "结束", "容量", "状态"
    };
    private static final String[] COURSE_HEADERS = {
            "任务 ID", "课程编号", "课程名称", "任课教师"
    };

    private final ClassService classService;
    private final StudentService studentService;
    private final TeachingService teachingService;
    private final CourseService courseService;
    private final EmployeeService employeeService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;
    private final Session session;

    public ClassUI(ClassService classService,
                   StudentService studentService,
                   TeachingService teachingService,
                   CourseService courseService,
                   EmployeeService employeeService,
                   ConsoleIO io,
                   TablePrinter tablePrinter,
                   Session session) {
        this.classService = classService;
        this.studentService = studentService;
        this.teachingService = teachingService;
        this.courseService = courseService;
        this.employeeService = employeeService;
        this.io = io;
        this.tablePrinter = tablePrinter;
        this.session = session;
    }

    public void show() {
        Menu menu = new Menu(session.isAdmin() ? "班级管理" : "我的班级");
        menu.addItem("1", "班级列表与筛选", this::list);
        menu.addItem("2", "班级详情", this::detail);
        if (session.isAdmin()) {
            menu.addItem("3", "新增班级", this::create);
            menu.addItem("4", "修改班级", this::update);
            menu.addItem("5", "删除班级", this::delete);
            menu.addItem("6", "转换班级状态", this::changeStatus);
            menu.addItem("7", "指定/更换班主任", this::assignHeadTeacher);
        }
        menu.run(io);
    }

    private void list() {
        String keyword = io.readLine("名称/编号关键字（直接回车不筛选）：");
        String statusRaw = io.readLine(
                "状态 [1 PLANNED / 2 ACTIVE / 3 FINISHED]（直接回车不筛选）：");
        ClassStatus status = statusRaw.isBlank() ? null
                : UiSupport.parseClassStatus(statusRaw);
        String headRaw = io.readLine("班主任 ID（直接回车不筛选）：");
        Long headTeacherId = headRaw.isBlank()
                ? null : Validators.requireId(headRaw, "班主任 ID");
        showRows(classService.filter(keyword, status, headTeacherId));
    }

    private void detail() {
        showRows(classService.list());
        long id = UiSupport.readId(io, "班级 ID（0 返回）：", "班级 ID");
        if (id == 0) {
            return;
        }
        SchoolClass schoolClass = find(id);
        if (schoolClass == null) {
            io.println("班级不存在。");
            return;
        }
        Map<Long, String> teachers = teacherNames();
        List<Student> students = studentService.listByClass(id);
        io.println();
        io.println("班级：" + schoolClass.getCode() + " " + schoolClass.getName());
        io.println("班主任：" + (schoolClass.getHeadTeacherId() == null ? "无"
                : teachers.getOrDefault(schoolClass.getHeadTeacherId(), "-")));
        io.println("状态：" + schoolClass.getStatus()
                + "    起止：" + Dates.formatDate(schoolClass.getStartDate())
                + " ~ " + Dates.formatDate(schoolClass.getEndDate()));
        io.println("在班人数：" + students.size() + " / 容量 " + schoolClass.getCapacity());

        List<ClassCourse> classCourses = teachingService.listByClass(id);
        Map<Long, Course> courses = new HashMap<>();
        for (Course course : courseService.list()) {
            courses.put(course.getId(), course);
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
        io.println("所配课程与任课教师：");
        tablePrinter.showPaged(rows, COURSE_HEADERS, io);
    }

    private void create() {
        String code = io.readRequired("班级编号（空行取消）：", "班级编号");
        String name = io.readRequired("班级名称（空行取消）：", "班级名称");
        String headRaw = io.readLine("班主任 ID（可空，直接回车表示无）：");
        Long headTeacherId = headRaw.isBlank() ? null
                : Validators.requireId(headRaw, "班主任 ID");
        LocalDate start = Dates.parseDate(
                io.readRequired("开始日期 yyyy-MM-dd（空行取消）：", "开始日期"));
        LocalDate end = Dates.parseDate(
                io.readRequired("结束日期 yyyy-MM-dd（空行取消）：", "结束日期"));
        int capacity = Validators.requireCapacity(
                io.readRequired("容量（空行取消）：", "容量"));
        SchoolClass created = classService.create(code, name, headTeacherId,
                start, end, capacity);
        io.println("已新增班级：" + created.getCode() + " " + created.getName()
                + "（初始状态 PLANNED）");
    }

    private void update() {
        showRows(classService.list());
        long id = UiSupport.readId(io, "要修改的班级 ID（0 返回）：", "班级 ID");
        if (id == 0) {
            return;
        }
        SchoolClass current = find(id);
        if (current == null) {
            io.println("班级不存在。");
            return;
        }
        String codeRaw = io.readLine("新编号（直接回车保持不变）：");
        String nameRaw = io.readLine("新名称（直接回车保持不变）：");
        String headRaw = io.readLine(
                "新班主任 ID（直接回车保持，输入 0 表示无班主任）：");
        String startRaw = io.readLine("新开始日期 yyyy-MM-dd（直接回车保持不变）：");
        String endRaw = io.readLine("新结束日期 yyyy-MM-dd（直接回车保持不变）：");
        String capacityRaw = io.readLine("新容量（直接回车保持不变）：");

        String code = codeRaw.isBlank() ? current.getCode() : codeRaw;
        String name = nameRaw.isBlank() ? current.getName() : nameRaw;
        Long headTeacherId = current.getHeadTeacherId();
        if (!headRaw.isBlank()) {
            headTeacherId = headRaw.trim().equals("0") ? null
                    : Validators.requireId(headRaw, "班主任 ID");
        }
        LocalDate start = startRaw.isBlank()
                ? current.getStartDate() : Dates.parseDate(startRaw);
        LocalDate end = endRaw.isBlank()
                ? current.getEndDate() : Dates.parseDate(endRaw);
        int capacity = capacityRaw.isBlank()
                ? current.getCapacity() : Validators.requireCapacity(capacityRaw);
        classService.update(id, code, name, headTeacherId, start, end, capacity);
        io.println("班级已更新。");
    }

    private void delete() {
        showRows(classService.list());
        long id = UiSupport.readId(io, "要删除的班级 ID（0 返回）：", "班级 ID");
        if (id == 0) {
            return;
        }
        SchoolClass schoolClass = find(id);
        if (schoolClass == null) {
            io.println("班级不存在。");
            return;
        }
        if (!io.confirm("确认删除班级“" + schoolClass.getCode() + " "
                + schoolClass.getName() + "”？")) {
            io.println("已取消删除。");
            return;
        }
        classService.delete(id);
        io.println("班级已删除。");
    }

    private void changeStatus() {
        showRows(classService.list());
        long id = UiSupport.readId(io, "班级 ID（0 返回）：", "班级 ID");
        if (id == 0) {
            return;
        }
        int choice = io.readInt("目标状态：1 ACTIVE / 2 FINISHED / 0 返回：", 0, 2);
        if (choice == 0) {
            return;
        }
        ClassStatus target = choice == 1 ? ClassStatus.ACTIVE : ClassStatus.FINISHED;
        classService.changeStatus(id, target);
        io.println("班级状态已更新为 " + target + "。");
    }

    private void assignHeadTeacher() {
        showRows(classService.list());
        long id = UiSupport.readId(io, "班级 ID（0 返回）：", "班级 ID");
        if (id == 0) {
            return;
        }
        String raw = io.readLine("班主任教师 ID（输入 0 清除班主任）：");
        if (raw.isBlank()) {
            io.println("已取消。");
            return;
        }
        Long teacherId = raw.trim().equals("0") ? null
                : Validators.requireId(raw, "教师 ID");
        classService.assignHeadTeacher(id, teacherId);
        io.println("班主任已更新。");
    }

    private void showRows(List<SchoolClass> classes) {
        Map<Long, String> teachers = teacherNames();
        List<String[]> rows = new ArrayList<>();
        for (SchoolClass schoolClass : classes) {
            rows.add(new String[]{
                    String.valueOf(schoolClass.getId()),
                    schoolClass.getCode(),
                    schoolClass.getName(),
                    schoolClass.getHeadTeacherId() == null ? "无"
                            : teachers.getOrDefault(schoolClass.getHeadTeacherId(), "-"),
                    Dates.formatDate(schoolClass.getStartDate()),
                    Dates.formatDate(schoolClass.getEndDate()),
                    String.valueOf(schoolClass.getCapacity()),
                    schoolClass.getStatus().name()
            });
        }
        tablePrinter.showPaged(rows, HEADERS, io);
    }

    private Map<Long, String> teacherNames() {
        Map<Long, String> names = new HashMap<>();
        for (Employee employee : employeeService.search("", null, null, null)) {
            names.put(employee.getId(), employee.getName());
        }
        return names;
    }

    private SchoolClass find(long id) {
        return classService.list().stream()
                .filter(schoolClass -> schoolClass.getId() == id)
                .findFirst().orElse(null);
    }
}
