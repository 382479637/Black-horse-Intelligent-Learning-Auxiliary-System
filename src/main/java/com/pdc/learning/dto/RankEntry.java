package com.pdc.learning.dto;

/**
 * 班级排名中的一行。
 */
public record RankEntry(
        int rank,
        long studentId,
        String studentNo,
        String name,
        double average,
        int failedCount) {
}
