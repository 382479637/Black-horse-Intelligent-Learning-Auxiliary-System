package com.pdc.learning.ui;

import com.pdc.learning.model.Employee;
import com.pdc.learning.service.Session;
import com.pdc.learning.ui.menu.Menu;
import com.pdc.learning.ui.view.AccountUI;
import com.pdc.learning.ui.view.ClassUI;
import com.pdc.learning.ui.view.CourseUI;
import com.pdc.learning.ui.view.DepartmentUI;
import com.pdc.learning.ui.view.EmployeeUI;
import com.pdc.learning.ui.view.ScoreUI;
import com.pdc.learning.ui.view.StatisticsUI;
import com.pdc.learning.ui.view.StudentUI;
import com.pdc.learning.ui.view.TeachingUI;
import com.pdc.learning.ui.view.WarningUI;

/**
 * 管理员主菜单装配（需求 6.2）。
 */
public class AdminMenuFactory implements MenuFactory {

    private final DepartmentUI departmentUI;
    private final EmployeeUI employeeUI;
    private final CourseUI courseUI;
    private final ClassUI classUI;
    private final TeachingUI teachingUI;
    private final StudentUI studentUI;
    private final ScoreUI scoreUI;
    private final StatisticsUI statisticsUI;
    private final WarningUI warningUI;
    private final AccountUI accountUI;
    private final ConsoleIO io;

    public AdminMenuFactory(DepartmentUI departmentUI,
                            EmployeeUI employeeUI,
                            CourseUI courseUI,
                            ClassUI classUI,
                            TeachingUI teachingUI,
                            StudentUI studentUI,
                            ScoreUI scoreUI,
                            StatisticsUI statisticsUI,
                            WarningUI warningUI,
                            AccountUI accountUI,
                            ConsoleIO io) {
        this.departmentUI = departmentUI;
        this.employeeUI = employeeUI;
        this.courseUI = courseUI;
        this.classUI = classUI;
        this.teachingUI = teachingUI;
        this.studentUI = studentUI;
        this.scoreUI = scoreUI;
        this.statisticsUI = statisticsUI;
        this.warningUI = warningUI;
        this.accountUI = accountUI;
        this.io = io;
    }

    @Override
    public Menu createMenu(Session session) {
        String name = session.currentUser().map(Employee::getName).orElse("管理员");
        Menu menu = new Menu("管理员菜单：" + name);
        menu.setZeroLabel("退出登录");
        menu.addItem("1", "基础数据", () -> open(baseDataMenu()));
        menu.addItem("2", "教学管理", () -> open(teachingMenu()));
        menu.addItem("3", "成绩管理", scoreUI::show);
        menu.addItem("4", "统计与预警", () -> open(statisticsMenu()));
        menu.addItem("5", "修改密码", accountUI::show);
        return menu;
    }

    private Menu baseDataMenu() {
        Menu menu = new Menu("基础数据");
        menu.addItem("1", "部门管理", departmentUI::show);
        menu.addItem("2", "员工（教师）管理", employeeUI::show);
        menu.addItem("3", "课程管理", courseUI::show);
        return menu;
    }

    private Menu teachingMenu() {
        Menu menu = new Menu("教学管理");
        menu.addItem("1", "班级管理", classUI::show);
        menu.addItem("2", "教学任务", teachingUI::show);
        menu.addItem("3", "学生管理", studentUI::show);
        return menu;
    }

    private Menu statisticsMenu() {
        Menu menu = new Menu("统计与预警");
        menu.addItem("1", "班级统计", statisticsUI::showClassStatistics);
        menu.addItem("2", "学生统计", statisticsUI::showStudentStatistics);
        menu.addItem("3", "学习预警", warningUI::show);
        return menu;
    }

    /**
     * 子菜单以“0”返回上一级。
     */
    private void open(Menu submenu) {
        submenu.run(io);
    }
}
