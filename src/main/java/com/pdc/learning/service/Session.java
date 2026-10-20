package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.enums.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 当前登录用户、角色判断与教师可见范围。
 */
public class Session {

    private Employee currentUser;

    public void login(Employee employee) {
        this.currentUser = employee;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Optional<Employee> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public boolean isTeacher() {
        return currentUser != null && currentUser.getRole() == Role.TEACHER;
    }

    public void requireAdmin() {
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (!isAdmin()) {
            throw new BusinessException("仅管理员可执行此操作");
        }
    }

    public void requireTeacher() {
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (!isTeacher()) {
            throw new BusinessException("仅教师可执行此操作");
        }
    }

    public Long teacherId() {
        return isTeacher() ? currentUser.getId() : null;
    }

    public boolean canAccess(ClassCourse classCourse) {
        if (classCourse == null || currentUser == null) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        return isTeacher()
                && classCourse.getTeacherId() != null
                && classCourse.getTeacherId().equals(currentUser.getId());
    }

    public List<ClassCourse> filterClassCourses(List<ClassCourse> classCourses) {
        List<ClassCourse> visible = new ArrayList<>();
        for (ClassCourse classCourse : classCourses) {
            if (canAccess(classCourse)) {
                visible.add(classCourse);
            }
        }
        return List.copyOf(visible);
    }
}
