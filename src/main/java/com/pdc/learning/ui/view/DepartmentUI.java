package com.pdc.learning.ui.view;

import com.pdc.learning.model.Department;
import com.pdc.learning.service.DepartmentService;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.ui.menu.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理界面。
 */
public class DepartmentUI {

    private static final String[] HEADERS = {"ID", "名称", "说明", "员工数"};

    private final DepartmentService departmentService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;

    public DepartmentUI(DepartmentService departmentService,
                        ConsoleIO io,
                        TablePrinter tablePrinter) {
        this.departmentService = departmentService;
        this.io = io;
        this.tablePrinter = tablePrinter;
    }

    public void show() {
        Menu menu = new Menu("部门管理");
        menu.addItem("1", "部门列表", this::listAll);
        menu.addItem("2", "按名称搜索", this::search);
        menu.addItem("3", "新增部门", this::create);
        menu.addItem("4", "修改部门", this::update);
        menu.addItem("5", "删除部门", this::delete);
        menu.run(io);
    }

    private void listAll() {
        showList(departmentService.list());
    }

    private void search() {
        String keyword = io.readLine("名称关键字（直接回车返回全部）：");
        showList(departmentService.search(keyword));
    }

    private void create() {
        String name = io.readRequired("部门名称（空行取消）：", "部门名称");
        String description = io.readLine("说明（可空，直接回车跳过）：");
        Department created = departmentService.create(name, description);
        io.println("已新增部门：" + created.getName() + "（ID " + created.getId() + "）");
    }

    private void update() {
        showList(departmentService.list());
        long id = UiSupport.readId(io, "要修改的部门 ID（0 返回）：", "部门 ID");
        if (id == 0) {
            return;
        }
        Department current = find(id);
        if (current == null) {
            io.println("部门不存在。");
            return;
        }
        String name = io.readLine("新名称（直接回车取消修改）：");
        if (name.isBlank()) {
            io.println("已取消修改。");
            return;
        }
        String description = io.readLine("新说明（直接回车清空；输入 - 保持原说明）：");
        String value = description.trim();
        if (value.equals("-")) {
            value = current.getDescription() == null ? "" : current.getDescription();
        }
        departmentService.update(id, name, value);
        io.println("部门已更新。");
    }

    private void delete() {
        showList(departmentService.list());
        long id = UiSupport.readId(io, "要删除的部门 ID（0 返回）：", "部门 ID");
        if (id == 0) {
            return;
        }
        Department department = find(id);
        if (department == null) {
            io.println("部门不存在。");
            return;
        }
        if (!io.confirm("确认删除部门“" + department.getName() + "”？")) {
            io.println("已取消删除。");
            return;
        }
        departmentService.delete(id);
        io.println("部门已删除。");
    }

    private Department find(long id) {
        return departmentService.list().stream()
                .filter(department -> department.getId() == id)
                .findFirst().orElse(null);
    }

    private void showList(List<Department> departments) {
        List<String[]> rows = new ArrayList<>();
        for (Department department : departments) {
            rows.add(new String[]{
                    String.valueOf(department.getId()),
                    department.getName(),
                    UiSupport.text(department.getDescription()),
                    String.valueOf(departmentService.employeeCount(department.getId()))
            });
        }
        tablePrinter.showPaged(rows, HEADERS, io);
    }
}
