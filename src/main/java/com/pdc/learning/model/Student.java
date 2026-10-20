package com.pdc.learning.model;

import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.StudentStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 学生学籍。
 */
public class Student extends Person {

    private String studentNo;
    private long classId;
    private LocalDate enrollmentDate;
    private StudentStatus status;

    public Student(String studentNo,
                   String name,
                   Gender gender,
                   String phone,
                   long classId,
                   LocalDate enrollmentDate) {
        this(0, studentNo, name, gender, phone, classId, enrollmentDate, StudentStatus.ACTIVE);
    }

    public Student(long id,
                   String studentNo,
                   String name,
                   Gender gender,
                   String phone,
                   long classId,
                   LocalDate enrollmentDate,
                   StudentStatus status) {
        super(id, name, gender, phone);
        this.studentNo = studentNo;
        this.classId = classId;
        this.enrollmentDate = enrollmentDate;
        this.status = status == null ? StudentStatus.ACTIVE : status;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public long getClassId() {
        return classId;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == StudentStatus.ACTIVE;
    }

    public void transferTo(long classId) {
        this.classId = classId;
    }

    public void changeStatus(StudentStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }
}
