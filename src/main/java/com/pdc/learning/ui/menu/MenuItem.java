package com.pdc.learning.ui.menu;

import java.util.Objects;

/**
 * 菜单项：键、标签与动作。
 */
public class MenuItem {

    private final String key;
    private final String label;
    private final MenuAction action;

    public MenuItem(String key, String label, MenuAction action) {
        this.key = Objects.requireNonNull(key);
        this.label = Objects.requireNonNull(label);
        this.action = Objects.requireNonNull(action);
    }

    public boolean matches(String input) {
        return key.equalsIgnoreCase(input);
    }

    public void execute() {
        action.execute();
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }
}
