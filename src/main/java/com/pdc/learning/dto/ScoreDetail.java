package com.pdc.learning.dto;

import java.time.LocalDateTime;

/**
 * 学生统计中的单条成绩明细。
 */
public record ScoreDetail(
        long classId,
        String className,
        long courseId,
        String courseCode,
        String courseName,
        double score,
        LocalDateTime updatedAt) {
}
