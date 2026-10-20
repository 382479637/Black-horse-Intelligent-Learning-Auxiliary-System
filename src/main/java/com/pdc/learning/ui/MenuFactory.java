package com.pdc.learning.ui;

import com.pdc.learning.service.Session;
import com.pdc.learning.ui.menu.Menu;

/**
 * 按角色装配主菜单。
 */
public interface MenuFactory {

    Menu createMenu(Session session);
}
