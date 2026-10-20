package com.pdc.learning.model;

import java.time.LocalDateTime;

/**
 * 成绩。classCourseId 创建后不可变，班级与课程经 ClassCourse 解析。
 */
public class Score extends AbstractEntity {

    private final long studentId;
    private final long classCourseId;
    private double score;
    private LocalDateTime updatedAt;

    public Score(long studentId, long classCourseId, double score, LocalDateTime updatedAt) {
        this(0, studentId, classCourseId, score, updatedAt);
    }

    public Score(long id,
                 long studentId,
                 long classCourseId,
                 double score,
                 LocalDateTime updatedAt) {
        super(id);
        this.studentId = studentId;
        this.classCourseId = classCourseId;
        this.updatedAt = updatedAt;
        setValidatedScore(score);
    }

    public long getStudentId() {
        return studentId;
    }

    public long getClassCourseId() {
        return classCourseId;
    }

    public double getScore() {
        return score;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isPassed() {
        return score >= 60;
    }

    public void updateScore(double score, LocalDateTime updatedAt) {
        setValidatedScore(score);
        this.updatedAt = updatedAt;
    }

    private void setValidatedScore(double score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("分数必须在 0–100 之间");
        }
        this.score = score;
    }
}
