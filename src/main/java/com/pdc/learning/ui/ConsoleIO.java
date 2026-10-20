package com.pdc.learning.ui;

import com.pdc.learning.exception.BusinessException;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 控制台输入输出。
 *
 * <p>输入字符集优先使用 {@link System#console()} 的字符集（Windows 控制台通常为 GBK），
 * 无控制台时使用 UTF-8。输出使用 {@code System.out} 的终端编码，避免中文乱码。</p>
 */
public class ConsoleIO {

    /**
     * 用户在命令提示中输入 Q，或任意输入处遇到 EOF 时抛出，由组合根安全退出。
     */
    public static class QuitRequestedException extends RuntimeException {
        public QuitRequestedException() {
            super("用户请求退出");
        }
    }

    private final BufferedReader reader;
    private final Console console;

    public ConsoleIO() {
        this.console = System.console();
        Charset charset = console != null ? console.charset() : StandardCharsets.UTF_8;
        this.reader = new BufferedReader(new InputStreamReader(System.in, charset));
    }

    /**
     * 数据字段读取。空行返回空字符串；Q 按普通文本处理；EOF 触发安全退出。
     */
    public String readLine(String prompt) {
        print(prompt);
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new QuitRequestedException();
            }
            return line;
        } catch (IOException e) {
            throw new QuitRequestedException();
        }
    }

    /**
     * 命令/选择提示读取。输入 Q（不区分大小写）或 EOF 触发安全退出。
     */
    public String readCommand(String prompt) {
        String line = readLine(prompt);
        if (line.trim().equalsIgnoreCase("Q")) {
            throw new QuitRequestedException();
        }
        return line.trim();
    }

    public String readPassword(String prompt) {
        if (console != null) {
            char[] password = console.readPassword(prompt);
            if (password == null) {
                throw new QuitRequestedException();
            }
            return new String(password);
        }
        print(prompt);
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new QuitRequestedException();
            }
            return line;
        } catch (IOException e) {
            throw new QuitRequestedException();
        }
    }

    /**
     * 命令型整数输入：Q/EOF 退出，非法输入重新提示。
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            String raw = readCommand(prompt);
            if (raw.isEmpty()) {
                println("请输入 " + min + "–" + max + " 之间的数字。");
                continue;
            }
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    println("请输入 " + min + "–" + max + " 之间的数字。");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                println("请输入有效数字。");
            }
        }
    }

    /**
     * 数据型小数输入：Q 按普通文本处理，非法输入重新提示。
     */
    public double readDouble(String prompt, double min, double max) {
        while (true) {
            String raw = readLine(prompt).trim();
            if (raw.isEmpty()) {
                println("输入不能为空。");
                continue;
            }
            try {
                double value = Double.parseDouble(raw);
                if (Double.isNaN(value) || value < min || value > max) {
                    println("请输入 " + min + "–" + max + " 之间的数字。");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                println("请输入有效数字。");
            }
        }
    }

    /**
     * 确认：y 为是，n/0/空行为否；Q 触发安全退出。
     */
    public boolean confirm(String prompt) {
        while (true) {
            String raw = readCommand(prompt + "（y/n）：").toLowerCase();
            if (raw.equals("y") || raw.equals("yes")) {
                return true;
            }
            if (raw.equals("n") || raw.equals("no") || raw.equals("0") || raw.isEmpty()) {
                return false;
            }
            println("请输入 y 或 n。");
        }
    }

    public void print(String text) {
        System.out.print(text);
        System.out.flush();
    }

    public void println(String text) {
        System.out.println(text);
    }

    public void println() {
        System.out.println();
    }

    /**
     * 读取一个必填数据字段；空行取消当前操作。
     */
    public String readRequired(String prompt, String fieldLabel) {
        String value = readLine(prompt).trim();
        if (value.isEmpty()) {
            throw new BusinessException("已取消：未填写" + fieldLabel);
        }
        return value;
    }
}
