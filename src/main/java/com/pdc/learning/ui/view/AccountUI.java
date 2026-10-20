package com.pdc.learning.ui.view;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.Department;
import com.pdc.learning.model.Employee;
import com.pdc.learning.service.AuthService;
import com.pdc.learning.service.DepartmentService;
import com.pdc.learning.service.EmployeeService;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.util.Dates;

import java.util.List;

/**
 * 本人资料查看（UC-16）与本人密码修改（需求 6.2/6.3）。
 */
public class AccountUI {

    private static final String[] PROFILE_HEADERS = {"字段", "值"};

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final ConsoleIO io;
    private final TablePrinter tablePrinter;

    public AccountUI(AuthService authService,
                     EmployeeService employeeService,
                     DepartmentService departmentService,
                     ConsoleIO io,
                     TablePrinter tablePrinter) {
        this.authService = authService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.io = io;
        this.tablePrinter = tablePrinter;
    }

    /**
     * UC-16 查看本人资料：只读展示当前登录教师本人的资料。
     * 不显示密码哈希/盐，也不提供修改角色或状态的入口。
     */
    public void showProfile() {
        Employee current = authService.currentUser()
                .orElseThrow(() -> new BusinessException("请先登录"));
        Employee profile = employeeService.findById(current.getId());
        List<String[]> rows = List.of(
                new String[]{"用户名", UiSupport.text(profile.getUsername())},
                new String[]{"姓名", UiSupport.text(profile.getName())},
                new String[]{"性别", profile.getGender().name()},
                new String[]{"手机号", UiSupport.text(profile.getPhone())},
                new String[]{"邮箱", UiSupport.text(profile.getEmail())},
                new String[]{"部门", departmentName(profile.getDepartmentId())},
                new String[]{"角色", profile.getRole().name()},
                new String[]{"状态", profile.getStatus().name()},
                new String[]{"入职日期", Dates.formatDate(profile.getHireDate())});
        io.println();
        io.println("我的资料（只读）");
        tablePrinter.printTable(rows, PROFILE_HEADERS);
        io.println("如需修改资料，请联系管理员；如需修改密码，请选择“修改密码”。");
    }

    /**
     * UC-03 修改本人密码：主菜单的“修改密码”直接进入本流程。
     */
    public void show() {
        String oldPassword = io.readPassword("旧密码（空行取消）：");
        if (oldPassword.isEmpty()) {
            io.println("已取消。");
            return;
        }
        String newPassword = io.readPassword("新密码（至少 6 位）：");
        if (newPassword.isEmpty()) {
            io.println("已取消。");
            return;
        }
        String confirmation = io.readPassword("再次输入新密码：");
        if (!newPassword.equals(confirmation)) {
            io.println("两次输入的新密码不一致，已取消。");
            return;
        }
        authService.changePassword(oldPassword, newPassword);
        io.println("密码已修改，请在下次登录时使用新密码。");
    }

    private String departmentName(long departmentId) {
        return departmentService.list().stream()
                .filter(department -> department.getId() == departmentId)
                .map(Department::getName)
                .findFirst()
                .orElse("-");
    }
}