package com.pdc.learning.ui.view;

import com.pdc.learning.model.Course;
import com.pdc.learning.service.CourseService;
import com.pdc.learning.service.Session;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.ui.menu.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程管理界面。管理员可增删改，教师只读。
 */
public class CourseUI {

    private static final String[] HEADERS = {"ID", "课程编号", "课程名称", "说明"};

    private final CourseService courseService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;
    private final Session session;

    public CourseUI(CourseService courseService,
                    ConsoleIO io,
                    TablePrinter tablePrinter,
                    Session session) {
        this.courseService = courseService;
        this.io = io;
        this.tablePrinter = tablePrinter;
        this.session = session;
    }

    public void show() {
        Menu menu = new Menu(session.isAdmin() ? "课程管理" : "课程目录（只读）");
        menu.addItem("1", "课程列表", this::listAll);
        menu.addItem("2", "按编号/名称搜索", this::search);
        if (session.isAdmin()) {
            menu.addItem("3", "新增课程", this::create);
            menu.addItem("4", "修改课程", this::update);
            menu.addItem("5", "删除课程", this::delete);
        }
        menu.run(io);
    }

    private void listAll() {
        showRows(courseService.list());
    }

    private void search() {
        String keyword = io.readLine("课程编号/名称关键字（直接回车返回全部）：").trim()
                .toLowerCase();
        if (keyword.isEmpty()) {
            showRows(courseService.list());
            return;
        }
        List<Course> result = new ArrayList<>();
        for (Course course : courseService.list()) {
            if (course.getCode().toLowerCase().contains(keyword)
                    || course.getName().toLowerCase().contains(keyword)) {
                result.add(course);
            }
        }
        showRows(result);
    }

    private void create() {
        String code = io.readRequired("课程编号（空行取消）：", "课程编号");
        String name = io.readRequired("课程名称（空行取消）：", "课程名称");
        String description = io.readLine("说明（可空）：");
        Course created = courseService.create(code, name, description);
        io.println("已新增课程：" + created.getCode() + " " + created.getName());
    }

    private void update() {
        showRows(courseService.list());
        long id = UiSupport.readId(io, "要修改的课程 ID（0 返回）：", "课程 ID");
        if (id == 0) {
            return;
        }
        Course current = find(id);
        if (current == null) {
            io.println("课程不存在。");
            return;
        }
        String codeRaw = io.readLine("新编号（直接回车保持不变）：");
        String nameRaw = io.readLine("新名称（直接回车保持不变）：");
        String descriptionRaw = io.readLine(
                "新说明（直接回车保持不变，输入 - 清空）：");
        String code = codeRaw.isBlank() ? current.getCode() : codeRaw;
        String name = nameRaw.isBlank() ? current.getName() : nameRaw;
        String description = current.getDescription();
        if (!descriptionRaw.isBlank()) {
            description = descriptionRaw.trim().equals("-") ? "" : descriptionRaw;
        }
        courseService.update(id, code, name, description);
        io.println("课程已更新。");
    }

    private void delete() {
        showRows(courseService.list());
        long id = UiSupport.readId(io, "要删除的课程 ID（0 返回）：", "课程 ID");
        if (id == 0) {
            return;
        }
        Course course = find(id);
        if (course == null) {
            io.println("课程不存在。");
            return;
        }
        if (!io.confirm("确认删除课程“" + course.getCode() + " " + course.getName() + "”？")) {
            io.println("已取消删除。");
            return;
        }
        courseService.delete(id);
        io.println("课程已删除。");
    }

    private Course find(long id) {
        return courseService.list().stream()
                .filter(course -> course.getId() == id)
                .findFirst().orElse(null);
    }

    private void showRows(List<Course> courses) {
        List<String[]> rows = new ArrayList<>();
        for (Course course : courses) {
            rows.add(new String[]{
                    String.valueOf(course.getId()),
                    course.getCode(),
                    course.getName(),
                    UiSupport.text(course.getDescription())
            });
        }
        tablePrinter.showPaged(rows, HEADERS, io);
    }
}
