package com.pdc.learning.dto;

import java.util.List;

/**
 * 班级统计聚合。studentCount 为全班学生数，不受教师可见课程范围影响。
 */
public record ClassStatistics(
        long classId,
        String className,
        int studentCount,
        int scoreCount,
        DataStatus dataStatus,
        Double average,
        Double passRate,
        Double highest,
        Double lowest,
        List<CourseStatistics> courses) {

    public ClassStatistics {
        courses = courses == null ? List.of() : List.copyOf(courses);
    }
}
