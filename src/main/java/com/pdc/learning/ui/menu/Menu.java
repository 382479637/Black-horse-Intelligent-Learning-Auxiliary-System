package com.pdc.learning.ui.menu;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.exception.DataAccessException;
import com.pdc.learning.ui.ConsoleIO;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单循环。命令提示中 0 返回上一级，Q 安全退出。
 */
public class Menu {

    private final String title;
    private final List<MenuItem> items = new ArrayList<>();
    private String zeroLabel = "返回";

    public Menu(String title) {
        this.title = title;
    }

    public void setZeroLabel(String zeroLabel) {
        this.zeroLabel = zeroLabel;
    }

    public void addItem(String key, String label, MenuAction action) {
        items.add(new MenuItem(key, label, action));
    }

    public void run(ConsoleIO io) {
        while (true) {
            display(io);
            String choice = io.readCommand("请选择：");
            if (choice.isEmpty()) {
                continue;
            }
            if (choice.equals("0")) {
                return;
            }
            MenuItem selected = null;
            for (MenuItem item : items) {
                if (item.matches(choice)) {
                    selected = item;
                    break;
                }
            }
            if (selected == null) {
                io.println("无效选择，请重新输入。");
                continue;
            }
            try {
                selected.execute();
            } catch (BusinessException e) {
                io.println("操作失败：" + e.getMessage());
            } catch (DataAccessException e) {
                io.println("数据访问失败：" + e.getMessage());
            }
        }
    }

    private void display(ConsoleIO io) {
        io.println();
        io.println("========== " + title + " ==========");
        for (MenuItem item : items) {
            io.println(item.getKey() + ". " + item.getLabel());
        }
        io.println("0. " + zeroLabel);
        io.println("Q. 退出系统");
    }
}
