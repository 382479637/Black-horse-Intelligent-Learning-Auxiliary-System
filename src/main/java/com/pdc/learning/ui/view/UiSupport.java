package com.pdc.learning.ui.view;

import com.pdc.learning.dto.ClassWarningLevel;
import com.pdc.learning.dto.WarningLevel;
import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.enums.ClassStatus;
import com.pdc.learning.model.enums.EmployeeStatus;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.Role;
import com.pdc.learning.model.enums.StudentStatus;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.util.Validators;

import java.util.Locale;

/**
 * UI 层的枚举解析与显示格式化辅助。
 */
public final class UiSupport {

    private UiSupport() {
    }

    public static Gender parseGender(String raw, Gender fallback) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if (value.isEmpty()) {
            return fallback;
        }
        return switch (value) {
            case "M", "MALE", "男" -> Gender.MALE;
            case "F", "FEMALE", "女" -> Gender.FEMALE;
            case "O", "OTHER", "其他" -> Gender.OTHER;
            case "U", "UNKNOWN", "未知" -> Gender.UNKNOWN;
            default -> throw new BusinessException("性别请输入 M/F/O/U 或直接回车");
        };
    }

    public static Role parseRole(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "1", "ADMIN", "管理员" -> Role.ADMIN;
            case "2", "TEACHER", "教师" -> Role.TEACHER;
            default -> throw new BusinessException("角色请输入 1（ADMIN）或 2（TEACHER）");
        };
    }

    public static EmployeeStatus parseEmployeeStatus(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "1", "ACTIVE", "启用" -> EmployeeStatus.ACTIVE;
            case "2", "DISABLED", "禁用" -> EmployeeStatus.DISABLED;
            default -> throw new BusinessException("状态请输入 1（ACTIVE）或 2（DISABLED）");
        };
    }

    public static StudentStatus parseStudentStatus(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "1", "ACTIVE" -> StudentStatus.ACTIVE;
            case "2", "SUSPENDED" -> StudentStatus.SUSPENDED;
            case "3", "GRADUATED" -> StudentStatus.GRADUATED;
            default -> throw new BusinessException(
                    "状态请输入 1（ACTIVE）、2（SUSPENDED）或 3（GRADUATED）");
        };
    }

    public static ClassStatus parseClassStatus(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "1", "PLANNED" -> ClassStatus.PLANNED;
            case "2", "ACTIVE" -> ClassStatus.ACTIVE;
            case "3", "FINISHED" -> ClassStatus.FINISHED;
            default -> throw new BusinessException(
                    "状态请输入 1（PLANNED）、2（ACTIVE）或 3（FINISHED）");
        };
    }

    /**
     * 列表选择用 ID：空行或 0 返回 0（表示返回上一级）。
     */
    public static long readId(ConsoleIO io, String prompt, String label) {
        while (true) {
            String raw = io.readLine(prompt);
            String value = raw == null ? "" : raw.trim();
            if (value.isEmpty() || value.equals("0")) {
                return 0;
            }
            try {
                return Validators.requireId(value, label);
            } catch (BusinessException e) {
                io.println("输入无效：" + e.getMessage() + "，请重新输入（0 返回）。");
            }
        }
    }

    public static String text(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }

    public static String formatScore(Double value) {
        return value == null ? "数据不足" : String.format(Locale.ROOT, "%.1f", value);
    }

    public static String formatRate(Double value) {
        return value == null ? "数据不足" : String.format(Locale.ROOT, "%.1f%%", value);
    }

    public static String levelText(WarningLevel level) {
        return switch (level) {
            case SEVERE -> "重度";
            case MODERATE -> "中度";
            case MILD -> "轻度";
            case NORMAL -> "正常";
            case INSUFFICIENT_DATA -> "数据不足";
        };
    }

    public static String classLevelText(ClassWarningLevel level) {
        return switch (level) {
            case NEEDS_ATTENTION -> "需关注";
            case NORMAL -> "正常";
            case INSUFFICIENT_DATA -> "数据不足";
        };
    }

    /**
     * 预警等级筛选：空或 0 返回 null（全部）。
     */
    public static WarningLevel parseLevelFilter(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "", "0", "ALL", "全部" -> null;
            case "1", "SEVERE", "重度" -> WarningLevel.SEVERE;
            case "2", "MODERATE", "中度" -> WarningLevel.MODERATE;
            case "3", "MILD", "轻度" -> WarningLevel.MILD;
            case "4", "NORMAL", "正常" -> WarningLevel.NORMAL;
            case "5", "INSUFFICIENT", "INSUFFICIENT_DATA", "数据不足" ->
                    WarningLevel.INSUFFICIENT_DATA;
            default -> throw new BusinessException("等级请输入 1–5 或 0（全部）");
        };
    }
}
