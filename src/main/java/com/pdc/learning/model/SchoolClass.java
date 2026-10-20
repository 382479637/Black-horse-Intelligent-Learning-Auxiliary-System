package com.pdc.learning.model;

import com.pdc.learning.model.enums.ClassStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 班级。
 */
public class SchoolClass extends AbstractEntity {

    private String code;
    private String name;
    private Long headTeacherId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int capacity;
    private ClassStatus status;

    public SchoolClass(String code,
                       String name,
                       Long headTeacherId,
                       LocalDate startDate,
                       LocalDate endDate,
                       int capacity,
                       ClassStatus status) {
        this(0, code, name, headTeacherId, startDate, endDate, capacity, status);
    }

    public SchoolClass(long id,
                       String code,
                       String name,
                       Long headTeacherId,
                       LocalDate startDate,
                       LocalDate endDate,
                       int capacity,
                       ClassStatus status) {
        super(id);
        this.code = code;
        this.name = name;
        this.headTeacherId = headTeacherId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.status = status == null ? ClassStatus.PLANNED : status;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Long getHeadTeacherId() {
        return headTeacherId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public ClassStatus getStatus() {
        return status;
    }

    public boolean isFull(int currentCount) {
        return currentCount >= capacity;
    }

    public boolean canAcceptStudent() {
        return status == ClassStatus.PLANNED || status == ClassStatus.ACTIVE;
    }

    public boolean isFinished() {
        return status == ClassStatus.FINISHED;
    }

    public void changeStatus(ClassStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public void assignHeadTeacher(Long headTeacherId) {
        this.headTeacherId = headTeacherId;
    }

    public void updateDetails(String code,
                              String name,
                              LocalDate startDate,
                              LocalDate endDate,
                              int capacity) {
        this.code = code;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
    }
}
