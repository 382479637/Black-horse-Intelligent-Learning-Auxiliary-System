package com.pdc.learning.dto;

/**
 * 预警计算所需的学生指标。无可见成绩时 scoreCount 为 0，其余为 null。
 */
public record StudentMetrics(
        long studentId,
        int scoreCount,
        Double average,
        Integer failedCount) {
}
