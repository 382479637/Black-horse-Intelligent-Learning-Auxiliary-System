package com.pdc.learning.util;

import com.pdc.learning.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 日期/时间戳的固定格式解析与格式化。
 */
public final class Dates {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private Dates() {
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            throw new BusinessException("日期不能为空，格式应为 yyyy-MM-dd");
        }
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式不正确，应为 yyyy-MM-dd：" + text.trim());
        }
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            throw new BusinessException("时间不能为空，格式应为 yyyy-MM-dd'T'HH:mm:ss");
        }
        try {
            return LocalDateTime.parse(text.trim(), DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new BusinessException("时间格式不正确，应为 yyyy-MM-dd'T'HH:mm:ss");
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME_FORMAT.format(dateTime.withNano(0));
    }

    public static LocalDateTime now() {
        return LocalDateTime.now().withNano(0);
    }
}
