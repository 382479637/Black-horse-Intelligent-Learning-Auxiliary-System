package com.pdc.learning.ui.view;

import com.pdc.learning.model.Department;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.enums.EmployeeStatus;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.Role;
import com.pdc.learning.service.DepartmentService;
import com.pdc.learning.service.EmployeeService;
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
 * 员工（教师）管理界面。
 */
public class EmployeeUI {

    private static final String[] HEADERS = {
            "ID", "用户名", "姓名", "部门", "角色", "状态", "入职日期"
    };

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;

    public EmployeeUI(EmployeeService employeeService,
                      DepartmentService departmentService,
                      ConsoleIO io,
                      TablePrinter tablePrinter) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.io = io;
        this.tablePrinter = tablePrinter;
    }

    public void show() {
        Menu menu = new Menu("员工（教师）管理");
        menu.addItem("1", "员工列表与筛选", this::list);
        menu.addItem("2", "新增员工", this::create);
        menu.addItem("3", "修改员工资料", this::update);
        menu.addItem("4", "删除员工", this::delete);
        menu.addItem("5", "启用/禁用员工", this::changeStatus);
        menu.addItem("6", "修改员工角色", this::changeRole);
        menu.addItem("7", "重置员工密码", this::resetPassword);
        menu.run(io);
    }

    private void list() {
        String keyword = io.readLine("用户名/姓名关键字（直接回车不筛选）：");
        String deptRaw = io.readLine("部门 ID（直接回车不筛选）：");
        Long departmentId = deptRaw.isBlank() ? null : Validators.requireId(deptRaw, "部门 ID");
        String roleRaw = io.readLine("角色 [1 ADMIN / 2 TEACHER]（直接回车不筛选）：");
        Role role = roleRaw.isBlank() ? null : UiSupport.parseRole(roleRaw);
        String statusRaw = io.readLine("状态 [1 ACTIVE / 2 DISABLED]（直接回车不筛选）：");
        EmployeeStatus status = statusRaw.isBlank()
                ? null : UiSupport.parseEmployeeStatus(statusRaw);
        showRows(employeeService.search(keyword, departmentId, role, status));
    }

    private void create() {
        String username = io.readRequired("用户名（4–20 位字母/数字/下划线，空行取消）：", "用户名");
        String password = io.readPassword("初始密码（至少 6 位，空行取消）：");
        if (password.isEmpty()) {
            io.println("已取消。");
            return;
        }
        String name = io.readRequired("姓名（空行取消）：", "姓名");
        String genderRaw = io.readLine("性别 [M/F/O/U]（直接回车为 UNKNOWN）：");
        Gender gender = UiSupport.parseGender(genderRaw, Gender.UNKNOWN);
        String phone = io.readLine("手机号（可空）：");
        String email = io.readLine("邮箱（可空）：");
        long departmentId = Validators.requireId(
                io.readRequired("部门 ID（空行取消）：", "部门 ID"), "部门 ID");
        Role role = UiSupport.parseRole(io.readLine("角色 [1 ADMIN / 2 TEACHER]："));
        String hireRaw = io.readLine("入职日期 yyyy-MM-dd（直接回车为今天）：");
        LocalDate hireDate = hireRaw.isBlank() ? LocalDate.now()
                : Dates.parseDate(hireRaw);
        Employee created = employeeService.create(username, password, name, gender,
                phone, email, departmentId, role, hireDate);
        io.println("已新增员工：" + created.getUsername() + "（" + created.getName() + "）");
    }

    private void update() {
        showRows(employeeService.search("", null, null, null));
        long id = UiSupport.readId(io, "要修改的员工 ID（0 返回）：", "员工 ID");
        if (id == 0) {
            return;
        }
        Employee current = find(id);
        if (current == null) {
            io.println("员工不存在。");
            return;
        }
        io.println("当前用户名：" + current.getUsername() + "，姓名：" + current.getName());
        String usernameRaw = io.readLine("新用户名（直接回车保持不变）：");
        String nameRaw = io.readLine("新姓名（直接回车保持不变）：");
        String genderRaw = io.readLine("新性别 [M/F/O/U]（直接回车保持不变）：");
        String phoneRaw = io.readLine("新手机号（直接回车保持，输入 - 清空）：");
        String emailRaw = io.readLine("新邮箱（直接回车保持，输入 - 清空）：");
        String deptRaw = io.readLine("新部门 ID（直接回车保持不变）：");
        String roleRaw = io.readLine("新角色 [1 ADMIN / 2 TEACHER]（直接回车保持不变）：");

        String username = usernameRaw.isBlank() ? current.getUsername() : usernameRaw;
        String name = nameRaw.isBlank() ? current.getName() : nameRaw;
        Gender gender = UiSupport.parseGender(genderRaw, current.getGender());
        String phone = resolveOptional(phoneRaw, current.getPhone());
        String email = resolveOptional(emailRaw, current.getEmail());
        long departmentId = deptRaw.isBlank()
                ? current.getDepartmentId() : Validators.requireId(deptRaw, "部门 ID");
        Role role = roleRaw.isBlank() ? current.getRole() : UiSupport.parseRole(roleRaw);
        employeeService.update(id, username, name, gender, phone, email, departmentId, role);
        io.println("员工资料已更新。");
    }

    private void delete() {
        showRows(employeeService.search("", null, null, null));
        long id = UiSupport.readId(io, "要删除的员工 ID（0 返回）：", "员工 ID");
        if (id == 0) {
            return;
        }
        Employee employee = find(id);
        if (employee == null) {
            io.println("员工不存在。");
            return;
        }
        if (!io.confirm("确认删除员工“" + employee.getUsername() + "（"
                + employee.getName() + "）”？")) {
            io.println("已取消删除。");
            return;
        }
        employeeService.delete(id);
        io.println("员工已删除。");
    }

    private void changeStatus() {
        showRows(employeeService.search("", null, null, null));
        long id = UiSupport.readId(io, "员工 ID（0 返回）：", "员工 ID");
        if (id == 0) {
            return;
        }
        int choice = io.readInt("目标状态：1 启用 / 2 禁用 / 0 返回：", 0, 2);
        if (choice == 0) {
            return;
        }
        EmployeeStatus status = choice == 1 ? EmployeeStatus.ACTIVE : EmployeeStatus.DISABLED;
        employeeService.changeStatus(id, status);
        io.println("员工状态已更新为 " + status + "。");
    }

    private void changeRole() {
        showRows(employeeService.search("", null, null, null));
        long id = UiSupport.readId(io, "员工 ID（0 返回）：", "员工 ID");
        if (id == 0) {
            return;
        }
        int choice = io.readInt("目标角色：1 ADMIN / 2 TEACHER / 0 返回：", 0, 2);
        if (choice == 0) {
            return;
        }
        Role role = choice == 1 ? Role.ADMIN : Role.TEACHER;
        employeeService.changeRole(id, role);
        io.println("员工角色已更新为 " + role + "。");
    }

    private void resetPassword() {
        showRows(employeeService.search("", null, null, null));
        long id = UiSupport.readId(io, "员工 ID（0 返回）：", "员工 ID");
        if (id == 0) {
            return;
        }
        String password = io.readPassword("新密码（至少 6 位）：");
        if (password.isEmpty()) {
            io.println("已取消。");
            return;
        }
        String confirmation = io.readPassword("再次输入新密码：");
        if (!password.equals(confirmation)) {
            io.println("两次输入的密码不一致，已取消。");
            return;
        }
        employeeService.resetPassword(id, password);
        io.println("密码已重置，请转告该员工尽快修改密码。");
    }

    private String resolveOptional(String raw, String current) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return current;
        }
        if (value.equals("-")) {
            return null;
        }
        return value;
    }

    private void showRows(List<Employee> employees) {
        Map<Long, String> departments = new HashMap<>();
        for (Department department : departmentService.list()) {
            departments.put(department.getId(), department.getName());
        }
        List<String[]> rows = new ArrayList<>();
        for (Employee employee : employees) {
            rows.add(new String[]{
                    String.valueOf(employee.getId()),
                    employee.getUsername(),
                    employee.getName(),
                    departments.getOrDefault(employee.getDepartmentId(), "-"),
                    employee.getRole().name(),
                    employee.getStatus().name(),
                    Dates.formatDate(employee.getHireDate())
            });
        }
        tablePrinter.showPaged(rows, HEADERS, io);
    }

    private Employee find(long id) {
        return employeeService.search("", null, null, null).stream()
                .filter(employee -> employee.getId() == id)
                .findFirst().orElse(null);
    }
}
