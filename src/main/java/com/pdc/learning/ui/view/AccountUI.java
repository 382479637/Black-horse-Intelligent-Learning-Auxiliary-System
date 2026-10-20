package com.pdc.learning.ui.view;

import com.pdc.learning.service.AuthService;
import com.pdc.learning.ui.ConsoleIO;

/**
 * 修改本人密码。主菜单的“修改密码”直接进入本流程（需求 6.2/6.3）。
 */
public class AccountUI {

    private final AuthService authService;
    private final ConsoleIO io;

    public AccountUI(AuthService authService, ConsoleIO io) {
        this.authService = authService;
        this.io = io;
    }

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
}
