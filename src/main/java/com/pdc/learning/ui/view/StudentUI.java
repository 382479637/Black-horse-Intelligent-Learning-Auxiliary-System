package com.pdc.learning.ui.view;

import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Student;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.StudentStatus;
import com.pdc.learning.service.ClassService;
import com.pdc.learning.service.Session;
import com.pdc.learning.service.StudentService;
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
 * 学生管理界面。教师只能查看自己任课班级的学生。
 */
public class StudentUI {

    private static final String[] HEADERS = {
            "ID", "学号", "姓名", "性别", "班级", "状态", "入学日期"
    };

    private final StudentService studentService;
    private final ClassService classService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;
    private final Session session;

    public StudentUI(StudentService studentService,
                     ClassService classService,
                     ConsoleIO io,
                     TablePrinter tablePrinter,
                     Session session) {
        this.studentService = studentService;
        this.classService = classService;
        this.io = io;
        this.tablePrinter = tablePrinter;
        this.session = session;
    }

    public void show() {
        Menu menu = new Menu(session.isAdmin() ? "学生管理" : "我的学生");
        menu.addItem("1", "学生列表与筛选", this::list);
        if (session.isAdmin()) {
            menu.addItem("2", "新增学生", this::create);
            menu.addItem("3", "修改学生资料", this::update);
            menu.addItem("4", "删除学生", this::delete);
            menu.addItem("5", "学生转班", this::transfer);
            menu.addItem("6", "修改学生状态", this::changeStatus);
        }
        menu.run(io);
    }

    private void list() {
        String keyword = io.readLine("学号/姓名关键字（直接回车不筛选）：");
        String classRaw = io.readLine("班级 ID（直接回车不筛选）：");
        Long classId = classRaw.isBlank() ? null : Validators.requireId(classRaw, "班级 ID");
        String statusRaw = io.readLine(
                "状态 [1 ACTIVE / 2 SUSPENDED / 3 GRADUATED]（直接回车不筛选）：");
        StudentStatus status = statusRaw.isBlank() ? null
                : UiSupport.parseStudentStatus(statusRaw);
        showRows(studentService.search(keyword, classId, status));
    }

    private void create() {
        String studentNo = io.readRequired("学号（空行取消）：", "学号");
        String name = io.readRequired("姓名（空行取消）：", "姓名");
        String genderRaw = io.readLine("性别 [M/F/O/U]（直接回车为 UNKNOWN）：");
        Gender gender = UiSupport.parseGender(genderRaw, Gender.UNKNOWN);
        String phone = io.readLine("手机号（可空）：");
        showClasses();
        long classId = Validators.requireId(
                io.readRequired("班级 ID（空行取消）：", "班级 ID"), "班级 ID");
        LocalDate enrollment = Dates.parseDate(
                io.readRequired("入学日期 yyyy-MM-dd（空行取消）：", "入学日期"));
        Student created = studentService.create(studentNo, name, gender, phone,
                classId, enrollment);
        io.println("已新增学生：" + created.getStudentNo() + " " + created.getName());
    }

    private void update() {
        showRows(studentService.list());
        long id = UiSupport.readId(io, "要修改的学生 ID（0 返回）：", "学生 ID");
        if (id == 0) {
            return;
        }
        Student current = find(id);
        if (current == null) {
            io.println("学生不存在。");
            return;
        }
        String nameRaw = io.readLine("新姓名（直接回车保持不变）：");
        String genderRaw = io.readLine("新性别 [M/F/O/U]（直接回车保持不变）：");
        String phoneRaw = io.readLine("新手机号（直接回车保持，输入 - 清空）：");
        String name = nameRaw.isBlank() ? current.getName() : nameRaw;
        Gender gender = UiSupport.parseGender(genderRaw, current.getGender());
        String phone = current.getPhone();
        if (!phoneRaw.isBlank()) {
            phone = phoneRaw.trim().equals("-") ? null : phoneRaw;
        }
        studentService.update(id, name, gender, phone);
        io.println("学生资料已更新。");
    }

    private void delete() {
        showRows(studentService.list());
        long id = UiSupport.readId(io, "要删除的学生 ID（0 返回）：", "学生 ID");
        if (id == 0) {
            return;
        }
        Student student = find(id);
        if (student == null) {
            io.println("学生不存在。");
            return;
        }
        if (!io.confirm("确认删除学生“" + student.getStudentNo() + " "
                + student.getName() + "”？其成绩将一并删除。")) {
            io.println("已取消删除。");
            return;
        }
        studentService.delete(id);
        io.println("学生及其成绩已删除。");
    }

    private void transfer() {
        showRows(studentService.list());
        long id = UiSupport.readId(io, "要转班的学生 ID（0 返回）：", "学生 ID");
        if (id == 0) {
            return;
        }
        showClasses();
        long targetClassId = Validators.requireId(
                io.readRequired("目标班级 ID（空行取消）：", "目标班级 ID"), "目标班级 ID");
        if (!io.confirm("确认将该生转入目标班级？已有成绩保留在原班级。")) {
            io.println("已取消。");
            return;
        }
        studentService.transfer(id, targetClassId);
        io.println("学生已转班。");
    }

    private void changeStatus() {
        showRows(studentService.list());
        long id = UiSupport.readId(io, "学生 ID（0 返回）：", "学生 ID");
        if (id == 0) {
            return;
        }
        int choice = io.readInt(
                "目标状态：1 ACTIVE / 2 SUSPENDED / 3 GRADUATED / 0 返回：", 0, 3);
        if (choice == 0) {
            return;
        }
        StudentStatus status = switch (choice) {
            case 1 -> StudentStatus.ACTIVE;
            case 2 -> StudentStatus.SUSPENDED;
            default -> StudentStatus.GRADUATED;
        };
        studentService.changeStatus(id, status);
        io.println("学生状态已更新为 " + status + "。");
    }

    private void showRows(List<Student> students) {
        Map<Long, String> classes = new HashMap<>();
        for (SchoolClass schoolClass : classService.list()) {
            classes.put(schoolClass.getId(),
                    schoolClass.getCode() + " " + schoolClass.getName());
        }
        List<String[]> rows = new ArrayList<>();
        for (Student student : students) {
            rows.add(new String[]{
                    String.valueOf(student.getId()),
                    student.getStudentNo(),
                    student.getName(),
                    student.getGender().name(),
                    classes.getOrDefault(student.getClassId(), "-"),
                    student.getStatus().name(),
                    Dates.formatDate(student.getEnrollmentDate())
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
                    schoolClass.getStatus().name(),
                    String.valueOf(schoolClass.getCapacity())
            });
        }
        tablePrinter.showPaged(rows,
                new String[]{"ID", "编号", "名称", "状态", "容量"}, io);
    }

    private Student find(long id) {
        return studentService.list().stream()
                .filter(student -> student.getId() == id)
                .findFirst().orElse(null);
    }
}
