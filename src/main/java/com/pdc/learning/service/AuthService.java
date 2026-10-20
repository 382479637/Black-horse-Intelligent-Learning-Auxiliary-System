package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.Employee;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.PasswordHasher;
import com.pdc.learning.util.Validators;

import java.util.Optional;

/**
 * 登录、登出与本人密码修改。
 */
public class AuthService {

    private final Repository<Employee> employeeRepository;
    private final PasswordHasher passwordHasher;
    private final Session session;

    public AuthService(Repository<Employee> employeeRepository,
                       PasswordHasher passwordHasher,
                       Session session) {
        this.employeeRepository = employeeRepository;
        this.passwordHasher = passwordHasher;
        this.session = session;
    }

    public Employee login(String username, String password) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("用户名或密码错误");
        }
        Optional<Employee> found = employeeRepository.findFirst(
                employee -> employee.getUsername().equalsIgnoreCase(name));
        if (found.isEmpty() || !passwordHasher.verify(password,
                found.get().getPasswordSalt(), found.get().getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        Employee employee = found.get();
        if (!employee.isActive()) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        session.login(employee);
        return employee;
    }

    public void logout() {
        session.logout();
    }

    public void changePassword(String oldPassword, String newPassword) {
        Employee employee = session.currentUser()
                .orElseThrow(() -> new BusinessException("请先登录"));
        if (oldPassword == null || oldPassword.isEmpty()
                || !passwordHasher.verify(oldPassword, employee.getPasswordSalt(),
                employee.getPasswordHash())) {
            throw new BusinessException("旧密码不正确");
        }
        Validators.requirePassword(newPassword);
        if (newPassword.equals(oldPassword)) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        String salt = passwordHasher.generateSalt();
        employee.updatePassword(salt, passwordHasher.hash(newPassword, salt));
        employeeRepository.save(employee);
    }

    public Optional<Employee> currentUser() {
        return session.currentUser();
    }

    public Session session() {
        return session;
    }
}
