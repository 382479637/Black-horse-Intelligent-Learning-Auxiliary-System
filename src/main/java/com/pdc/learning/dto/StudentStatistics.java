package com.pdc.learning.dto;

import java.util.List;

/**
 * 学生统计。rank 为空时由 rankNote 说明原因。
 */
public record StudentStatistics(
        long studentId,
        String studentNo,
        String name,
        int scoreCount,
        DataStatus dataStatus,
        Double average,
        Integer failedCount,
        Integer rank,
        String rankNote,
        List<ScoreDetail> details) {

    public StudentStatistics {
        details = details == null ? List.of() : List.copyOf(details);
    }
}
