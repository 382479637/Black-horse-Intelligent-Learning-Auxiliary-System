package com.pdc.learning.dto;

/**
 * 单个班级课程的成绩统计。
 */
public record CourseStatistics(
        long classCourseId,
        String courseCode,
        String courseName,
        String teacherName,
        int scoreCount,
        DataStatus dataStatus,
        Double average,
        Double passRate,
        Double highest,
        Double lowest) {
}
