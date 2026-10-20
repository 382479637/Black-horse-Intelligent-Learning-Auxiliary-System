package com.pdc.learning.app;

import com.pdc.learning.exception.DataAccessException;
import com.pdc.learning.ui.ConsoleIO;

/**
 * 程序入口。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ApplicationBootstrap bootstrap = new ApplicationBootstrap();
        try {
            bootstrap.start();
        } catch (ConsoleIO.QuitRequestedException e) {
            bootstrap.shutdown();
        } catch (DataAccessException e) {
            System.err.println("启动失败：" + e.getMessage());
            System.exit(1);
        }
    }
}
