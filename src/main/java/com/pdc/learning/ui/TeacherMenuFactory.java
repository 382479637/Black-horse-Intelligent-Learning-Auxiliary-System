package com.pdc.learning.ui;

import com.pdc.learning.model.Employee;
import com.pdc.learning.service.Session;
import com.pdc.learning.ui.menu.Menu;
import com.pdc.learning.ui.view.AccountUI;
import com.pdc.learning.ui.view.ClassUI;
import com.pdc.learning.ui.view.CourseUI;
import com.pdc.learning.ui.view.ScoreUI;
import com.pdc.learning.ui.view.StatisticsUI;
import com.pdc.learning.ui.view.StudentUI;
import com.pdc.learning.ui.view.WarningUI;

/**
 * 教师主菜单装配（需求 6.3）。所有可见数据由 Service 按 Session 限定为本人范围。
 */
public class TeacherMenuFactory implements MenuFactory {

    private final ClassUI classUI;
    private final StudentUI studentUI;
    private final CourseUI courseUI;
    private final ScoreUI scoreUI;
    private final StatisticsUI statisticsUI;
    private final WarningUI warningUI;
    private final AccountUI accountUI;
    private final ConsoleIO io;

    public TeacherMenuFactory(ClassUI classUI,
                              StudentUI studentUI,
                              CourseUI courseUI,
                              ScoreUI scoreUI,
                              StatisticsUI statisticsUI,
                              WarningUI warningUI,
                              AccountUI accountUI,
                              ConsoleIO io) {
        this.classUI = classUI;
        this.studentUI = studentUI;
        this.courseUI = courseUI;
        this.scoreUI = scoreUI;
        this.statisticsUI = statisticsUI;
        this.warningUI = warningUI;
        this.accountUI = accountUI;
        this.io = io;
    }

    @Override
    public Menu createMenu(Session session) {
        String name = session.currentUser().map(Employee::getName).orElse("教师");
        Menu menu = new Menu("教师菜单：" + name);
        menu.setZeroLabel("退出登录");
        menu.addItem("1", "我的教学", () -> open(teachingMenu()));
        menu.addItem("2", "成绩管理", scoreUI::show);
        menu.addItem("3", "统计与预警", () -> open(statisticsMenu()));
        menu.addItem("4", "修改密码", accountUI::show);
        return menu;
    }

    private Menu teachingMenu() {
        Menu menu = new Menu("我的教学");
        menu.addItem("1", "我的班级", classUI::show);
        menu.addItem("2", "我的学生", studentUI::show);
        menu.addItem("3", "课程目录", courseUI::show);
        return menu;
    }

    private Menu statisticsMenu() {
        Menu menu = new Menu("统计与预警");
        menu.addItem("1", "班级统计", statisticsUI::showClassStatistics);
        menu.addItem("2", "学生统计", statisticsUI::showStudentStatistics);
        menu.addItem("3", "学习预警", warningUI::show);
        return menu;
    }

    private void open(Menu submenu) {
        submenu.run(io);
    }
}
