package com.pdc.learning.dto;

/**
 * 班级预警结果。
 */
public record ClassWarning(
        long classId,
        String className,
        ClassWarningLevel level,
        Double passRate,
        Double average,
        int scoreCount) {
}
