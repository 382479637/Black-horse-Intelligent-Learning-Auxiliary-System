package com.pdc.learning.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * 表格输出与 UI 层分页。每页默认 10 条。
 */
public class TablePrinter {

    public static final int PAGE_SIZE = 10;

    /**
     * 打印表格，按显示宽度对齐（中文按 2 列计算）。
     */
    public void printTable(List<String[]> rows, String[] headers) {
        int columns = headers.length;
        int[] widths = new int[columns];
        for (int i = 0; i < columns; i++) {
            widths[i] = displayWidth(headers[i]);
        }
        for (String[] row : rows) {
            for (int i = 0; i < columns; i++) {
                widths[i] = Math.max(widths[i], displayWidth(cell(row, i)));
            }
        }
        System.out.println(formatRow(headers, widths));
        int totalWidth = 0;
        for (int width : widths) {
            totalWidth += width;
        }
        totalWidth += 2 * (columns - 1);
        System.out.println("-".repeat(Math.max(totalWidth, 1)));
        for (String[] row : rows) {
            System.out.println(formatRow(
                    java.util.Arrays.stream(row).map(value -> value == null ? "" : value)
                            .toArray(String[]::new), widths));
        }
        if (rows.isEmpty()) {
            System.out.println("（无数据）");
        }
    }

    public static <T> List<T> page(List<T> list, int page, int pageSize) {
        if (list.isEmpty()) {
            return List.of();
        }
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(list.size(), from + pageSize);
        if (from >= to) {
            return List.of();
        }
        return new ArrayList<>(list.subList(from, to));
    }

    public static int pageCount(int total, int pageSize) {
        if (total <= 0) {
            return 1;
        }
        return (total + pageSize - 1) / pageSize;
    }

    /**
     * 分页展示：n 下一页、p 上一页、页码跳转、0 返回。
     */
    public void showPaged(List<String[]> rows, String[] headers, ConsoleIO io) {
        if (rows.isEmpty()) {
            io.println("暂无数据。");
            return;
        }
        int pages = pageCount(rows.size(), PAGE_SIZE);
        int page = 1;
        while (true) {
            io.println();
            io.println("第 " + page + "/" + pages + " 页（共 " + rows.size() + " 条）");
            printTable(page(rows, page, PAGE_SIZE), headers);
            String command = io.readCommand(
                    "翻页：n 下一页 / p 上一页 / 页码 / 0 返回：").toLowerCase();
            if (command.equals("0") || command.isEmpty()) {
                return;
            }
            if (command.equals("n")) {
                if (page < pages) {
                    page++;
                } else {
                    io.println("已经是最后一页。");
                }
                continue;
            }
            if (command.equals("p")) {
                if (page > 1) {
                    page--;
                } else {
                    io.println("已经是第一页。");
                }
                continue;
            }
            try {
                int target = Integer.parseInt(command);
                if (target < 1 || target > pages) {
                    io.println("页码范围为 1–" + pages + "。");
                } else {
                    page = target;
                }
            } catch (NumberFormatException e) {
                io.println("请输入 n、p、页码或 0。");
            }
        }
    }

    private String formatRow(String[] cells, int[] widths) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            String value = cell(cells, i);
            builder.append(value);
            builder.append(" ".repeat(Math.max(1, widths[i] - displayWidth(value) + 2)));
        }
        return builder.toString().stripTrailing();
    }

    private String cell(String[] row, int index) {
        if (index >= row.length || row[index] == null) {
            return "";
        }
        return row[index];
    }

    private int displayWidth(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            width += isWide(codePoint) ? 2 : 1;
            i += Character.charCount(codePoint);
        }
        return width;
    }

    private boolean isWide(int codePoint) {
        return (codePoint >= 0x1100 && codePoint <= 0x115F)
                || (codePoint >= 0x2E80 && codePoint <= 0xA4CF)
                || (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
                || (codePoint >= 0xFE30 && codePoint <= 0xFE4F)
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6)
                || (codePoint >= 0x20000 && codePoint <= 0x3FFFD);
    }
}
