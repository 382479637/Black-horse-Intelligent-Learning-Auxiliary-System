package com.pdc.learning.model;

import java.time.LocalDateTime;

/**
 * 班级-课程-任课教师关联实体。teacherId 为空表示“待分配”。
 */
public class ClassCourse extends AbstractEntity {

    private final long classId;
    private final long courseId;
    private Long teacherId;
    private final LocalDateTime createdAt;

    public ClassCourse(long classId, long courseId, Long teacherId, LocalDateTime createdAt) {
        this(0, classId, courseId, teacherId, createdAt);
    }

    public ClassCourse(long id,
                       long classId,
                       long courseId,
                       Long teacherId,
                       LocalDateTime createdAt) {
        super(id);
        this.classId = classId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.createdAt = createdAt;
    }

    public long getClassId() {
        return classId;
    }

    public long getCourseId() {
        return courseId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isAssigned() {
        return teacherId != null;
    }

    public void assignTeacher(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void unassignTeacher() {
        this.teacherId = null;
    }
}
