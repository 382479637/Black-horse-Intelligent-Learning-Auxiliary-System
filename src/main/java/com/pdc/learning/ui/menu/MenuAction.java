package com.pdc.learning.ui.menu;

/**
 * 可执行菜单动作（Command/Strategy）。
 */
@FunctionalInterface
public interface MenuAction {

    void execute();
}
