package com.pdc.learning.ui;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.enums.Role;
import com.pdc.learning.service.AuthService;
import com.pdc.learning.ui.menu.Menu;

/**
 * 登录循环：登录成功后按角色选择主菜单，菜单返回（0）即退出登录。
 */
public class LoginUI {

    private final AuthService authService;
    private final ConsoleIO io;
    private final MenuFactory adminMenuFactory;
    private final MenuFactory teacherMenuFactory;

    public LoginUI(AuthService authService,
                   ConsoleIO io,
                   MenuFactory adminMenuFactory,
                   MenuFactory teacherMenuFactory) {
        this.authService = authService;
        this.io = io;
        this.adminMenuFactory = adminMenuFactory;
        this.teacherMenuFactory = teacherMenuFactory;
    }

    public void run() {
        printBanner();
        while (true) {
            Employee employee = login();
            if (employee == null) {
                continue;
            }
            io.println();
            io.println("登录成功：" + employee.getName()
                    + "（" + roleText(employee.getRole()) + "）");
            MenuFactory factory = employee.getRole() == Role.ADMIN
                    ? adminMenuFactory : teacherMenuFactory;
            Menu menu = factory.createMenu(authService.session());
            menu.run(io);
            authService.logout();
            io.println();
            io.println("已退出登录。");
        }
    }

    private Employee login() {
        io.println();
        io.println("---------- 登录 ----------");
        String username = io.readCommand("用户名（Q 退出系统）：");
        if (username.isEmpty()) {
            io.println("用户名不能为空。");
            return null;
        }
        String password = io.readPassword("密码：");
        try {
            return authService.login(username, password);
        } catch (BusinessException e) {
            io.println("登录失败：" + e.getMessage());
            return null;
        }
    }

    private String roleText(Role role) {
        return role == Role.ADMIN ? "管理员" : "教师";
    }

    private void printBanner() {
        io.println("==============================================");
        io.println("        黑马智能学习辅助系统（CUI）");
        io.println("        Learning Assistant System");
        io.println("==============================================");
        io.println("提示：菜单输入 0 返回上一级；输入 Q 安全退出。");
        io.println("      数据字段输入空行取消当前操作。");
    }
}
