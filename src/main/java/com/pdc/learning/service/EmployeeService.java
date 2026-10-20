package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.enums.EmployeeStatus;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.Role;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.PasswordHasher;
import com.pdc.learning.util.Validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 员工（教师）管理、启停、角色与密码重置。
 */
public class EmployeeService {

    private final Repository<Employee> employeeRepository;
    private final Repository<com.pdc.learning.model.Department> departmentRepository;
    private final Repository<SchoolClass> classRepository;
    private final Repository<ClassCourse> classCourseRepository;
    private final PasswordHasher passwordHasher;
    private final Session session;

    public EmployeeService(Repository<Employee> employeeRepository,
                           Repository<com.pdc.learning.model.Department> departmentRepository,
                           Repository<SchoolClass> classRepository,
                           Repository<ClassCourse> classCourseRepository,
                           PasswordHasher passwordHasher,
                           Session session) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.classRepository = classRepository;
        this.classCourseRepository = classCourseRepository;
        this.passwordHasher = passwordHasher;
        this.session = session;
    }

    public List<Employee> search(String keyword, Long departmentId, Role role,
                                 EmployeeStatus status) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Employee> result = employeeRepository.findAll(employee ->
                (needle.isEmpty()
                        || employee.getUsername().toLowerCase().contains(needle)
                        || employee.getName().toLowerCase().contains(needle))
                        && (departmentId == null || employee.getDepartmentId() == departmentId)
                        && (role == null || employee.getRole() == role)
                        && (status == null || employee.getStatus() == status));
        result.sort(Comparator.comparing(Employee::getUsername));
        return List.copyOf(result);
    }

    public Employee create(String username,
                           String rawPassword,
                           String name,
                           Gender gender,
                           String phone,
                           String email,
                           long departmentId,
                           Role role,
                           LocalDate hireDate) {
        session.requireAdmin();
        String validUsername = Validators.requireUsername(username);
        String validPassword = Validators.requirePassword(rawPassword);
        String validName = Validators.requireName(name);
        String validPhone = Validators.optionalPhone(phone);
        String validEmail = Validators.optionalEmail(email);
        requireDepartment(departmentId);
        if (role == null) {
            throw new BusinessException("请选择员工角色");
        }
        if (hireDate == null) {
            throw new BusinessException("入职日期不能为空");
        }
        ensureUsernameUnique(validUsername, -1);
        Employee employee = new Employee(validUsername, validName, gender, validPhone,
                validEmail, departmentId, role, hireDate);
        String salt = passwordHasher.generateSalt();
        employee.updatePassword(salt, passwordHasher.hash(validPassword, salt));
        return employeeRepository.save(employee);
    }

    public void update(long id,
                       String username,
                       String name,
                       Gender gender,
                       String phone,
                       String email,
                       long departmentId,
                       Role role) {
        session.requireAdmin();
        Employee employee = findOrThrow(id);
        String validUsername = Validators.requireUsername(username);
        String validName = Validators.requireName(name);
        String validPhone = Validators.optionalPhone(phone);
        String validEmail = Validators.optionalEmail(email);
        requireDepartment(departmentId);
        if (role == null) {
            throw new BusinessException("请选择员工角色");
        }
        ensureUsernameUnique(validUsername, id);
        validateRoleChange(employee, role);
        employee.updateUsername(validUsername);
        employee.updateProfile(validName, gender, validPhone);
        employee.updateEmail(validEmail);
        employee.updateDepartment(departmentId);
        employee.changeRole(role);
        employeeRepository.save(employee);
    }

    public void delete(long id) {
        session.requireAdmin();
        Employee employee = findOrThrow(id);
        if (session.currentUser().isPresent()
                && session.currentUser().get().getId() == id) {
            throw new BusinessException("不能删除当前登录账号");
        }
        List<String> classReferences = headTeacherClassCodes(id);
        if (!classReferences.isEmpty()) {
            throw new BusinessException("该员工仍是班主任（" + String.join("、", classReferences)
                    + "），请先更换班主任后再删除");
        }
        int teachingCount = teachingCourseCount(id);
        if (teachingCount > 0) {
            throw new BusinessException("该员工仍有 " + teachingCount
                    + " 条任课任务，请先改派任课教师后再删除");
        }
        if (employee.isAdmin() && employee.isActive() && activeAdminCount() <= 1) {
            throw new BusinessException("不能删除最后一个启用的管理员");
        }
        employeeRepository.delete(id);
    }

    public void changeStatus(long id, EmployeeStatus status) {
        session.requireAdmin();
        if (status == null) {
            throw new BusinessException("请选择员工状态");
        }
        Employee employee = findOrThrow(id);
        if (status == employee.getStatus()) {
            return;
        }
        if (status == EmployeeStatus.DISABLED) {
            List<String> classReferences = headTeacherClassCodes(id);
            if (!classReferences.isEmpty()) {
                throw new BusinessException("该员工仍是班主任（" + String.join("、", classReferences)
                        + "），请先更换班主任后再禁用");
            }
            int teachingCount = teachingCourseCount(id);
            if (teachingCount > 0) {
                throw new BusinessException("该员工仍有 " + teachingCount
                        + " 条任课任务，请先改派任课教师后再禁用");
            }
            if (employee.isAdmin() && activeAdminCount() <= 1) {
                throw new BusinessException("不能禁用最后一个启用的管理员");
            }
        }
        employee.changeStatus(status);
        employeeRepository.save(employee);
    }

    public void changeRole(long id, Role role) {
        session.requireAdmin();
        if (role == null) {
            throw new BusinessException("请选择员工角色");
        }
        Employee employee = findOrThrow(id);
        if (role == employee.getRole()) {
            return;
        }
        validateRoleChange(employee, role);
        employee.changeRole(role);
        employeeRepository.save(employee);
    }

    public void resetPassword(long id, String newPassword) {
        session.requireAdmin();
        Employee employee = findOrThrow(id);
        String validPassword = Validators.requirePassword(newPassword);
        String salt = passwordHasher.generateSalt();
        employee.updatePassword(salt, passwordHasher.hash(validPassword, salt));
        employeeRepository.save(employee);
    }

    public Employee findById(long id) {
        return findOrThrow(id);
    }

    public List<Employee> listTeachers() {
        List<Employee> teachers = employeeRepository.findAll(
                employee -> employee.getRole() == Role.TEACHER && employee.isActive());
        teachers.sort(Comparator.comparing(Employee::getName));
        return List.copyOf(teachers);
    }

    private void validateRoleChange(Employee employee, Role target) {
        if (target == Role.ADMIN && employee.getRole() != Role.ADMIN) {
            List<String> classReferences = headTeacherClassCodes(employee.getId());
            int teachingCount = teachingCourseCount(employee.getId());
            if (!classReferences.isEmpty() || teachingCount > 0) {
                throw new BusinessException("该员工仍有班主任或任课任务，"
                        + "请先改派后再改为管理员");
            }
        }
        if (employee.getRole() == Role.ADMIN && target == Role.TEACHER
                && employee.isActive() && activeAdminCount() <= 1) {
            throw new BusinessException("不能将最后一个启用的管理员改为教师");
        }
    }

    private Employee findOrThrow(long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
    }

    private void requireDepartment(long departmentId) {
        if (departmentRepository.findById(departmentId).isEmpty()) {
            throw new BusinessException("所选部门不存在");
        }
    }

    private void ensureUsernameUnique(String username, long excludeId) {
        boolean duplicate = employeeRepository.findAll(employee ->
                        employee.getId() != excludeId
                                && employee.getUsername().equalsIgnoreCase(username))
                .size() > 0;
        if (duplicate) {
            throw new BusinessException("用户名已存在，请使用其他用户名");
        }
    }

    private List<String> headTeacherClassCodes(long employeeId) {
        List<String> codes = new ArrayList<>();
        for (SchoolClass schoolClass : classRepository.findAll()) {
            if (Objects.equals(schoolClass.getHeadTeacherId(), employeeId)) {
                codes.add(schoolClass.getCode());
            }
        }
        return codes;
    }

    private int teachingCourseCount(long employeeId) {
        return classCourseRepository.findAll(
                classCourse -> Objects.equals(classCourse.getTeacherId(), employeeId)).size();
    }

    private long activeAdminCount() {
        return employeeRepository.findAll(
                employee -> employee.isAdmin() && employee.isActive()).size();
    }
}
