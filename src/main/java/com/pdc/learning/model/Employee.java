package com.pdc.learning.model;

import com.pdc.learning.model.enums.EmployeeStatus;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.Role;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 员工/教师/登录账号。
 */
public class Employee extends Person {

    private String username;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private long departmentId;
    private Role role;
    private EmployeeStatus status;
    private LocalDate hireDate;

    public Employee(String username,
                    String name,
                    Gender gender,
                    String phone,
                    String email,
                    long departmentId,
                    Role role,
                    LocalDate hireDate) {
        this(0, username, name, gender, phone, email, departmentId, role,
                EmployeeStatus.ACTIVE, hireDate);
    }

    public Employee(long id,
                    String username,
                    String name,
                    Gender gender,
                    String phone,
                    String email,
                    long departmentId,
                    Role role,
                    EmployeeStatus status,
                    LocalDate hireDate) {
        super(id, name, gender, phone);
        this.username = username;
        this.email = email;
        this.departmentId = departmentId;
        this.role = Objects.requireNonNull(role, "role");
        this.status = status == null ? EmployeeStatus.ACTIVE : status;
        this.hireDate = hireDate;
        this.passwordHash = "";
        this.passwordSalt = "";
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public Role getRole() {
        return role;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }

    public boolean canTeach() {
        return role == Role.TEACHER && status == EmployeeStatus.ACTIVE;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateDepartment(long departmentId) {
        this.departmentId = departmentId;
    }

    public void updatePassword(String salt, String hash) {
        this.passwordSalt = salt;
        this.passwordHash = hash;
    }

    public void changeStatus(EmployeeStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public void changeRole(Role role) {
        this.role = Objects.requireNonNull(role, "role");
    }
}
