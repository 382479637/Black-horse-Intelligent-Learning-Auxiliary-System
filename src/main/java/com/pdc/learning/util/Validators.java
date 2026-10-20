package com.pdc.learning.util;

import com.pdc.learning.exception.BusinessException;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * 输入格式校验。失败时抛出 {@link BusinessException}，由 UI 显示消息。
 */
public final class Validators {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{4,20}");
    private static final Pattern PHONE = Pattern.compile("[0-9+\\- ]{6,20}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private Validators() {
    }

    public static String requireUsername(String raw) {
        String value = trim(raw);
        if (value.isEmpty() || !USERNAME.matcher(value).matches()) {
            throw new BusinessException("用户名需为 4–20 个字母、数字或下划线");
        }
        return value;
    }

    public static String requirePassword(String raw) {
        if (raw == null || raw.length() < 6) {
            throw new BusinessException("密码长度不能少于 6 个字符");
        }
        return raw;
    }

    public static String requireName(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            throw new BusinessException("名称不能为空");
        }
        if (value.length() > 50) {
            throw new BusinessException("名称长度不能超过 50 个字符");
        }
        return value;
    }

    public static String optionalDescription(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > 200) {
            throw new BusinessException("说明长度不能超过 200 个字符");
        }
        return value;
    }

    public static String optionalPhone(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return null;
        }
        if (!PHONE.matcher(value).matches()) {
            throw new BusinessException("手机号应为 6–20 个字符，只能包含数字、+、- 或空格");
        }
        return value;
    }

    public static String optionalEmail(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return null;
        }
        if (!EMAIL.matcher(value).matches()) {
            throw new BusinessException("邮箱格式不正确，应包含 @ 与域名");
        }
        return value;
    }

    public static String requireCode(String raw, String label) {
        String value = trim(raw);
        if (value.isEmpty()) {
            throw new BusinessException(label + "不能为空");
        }
        if (value.length() > 30) {
            throw new BusinessException(label + "长度不能超过 30 个字符");
        }
        return value;
    }

    public static LocalDate requireDate(String raw) {
        return Dates.parseDate(raw);
    }

    public static double requireScore(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("分数不能为空");
        }
        double score;
        try {
            score = Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("分数必须是数字");
        }
        if (score < 0 || score > 100) {
            throw new BusinessException("分数必须在 0–100 之间");
        }
        return score;
    }

    public static int requireCapacity(String raw) {
        int capacity;
        try {
            capacity = Integer.parseInt(raw == null ? "" : raw.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("容量必须是正整数");
        }
        if (capacity <= 0) {
            throw new BusinessException("容量必须是正整数");
        }
        return capacity;
    }

    public static long requireId(String raw, String label) {
        long id;
        try {
            id = Long.parseLong(raw == null ? "" : raw.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(label + "必须是数字 ID");
        }
        if (id <= 0) {
            throw new BusinessException(label + "必须是正整数 ID");
        }
        return id;
    }

    private static String trim(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
