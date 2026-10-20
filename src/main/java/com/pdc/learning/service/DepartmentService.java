package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.Department;
import com.pdc.learning.model.Employee;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.Validators;

import java.util.List;

/**
 * 部门 CRUD 与员工数统计。
 */
public class DepartmentService {

    private final Repository<Department> departmentRepository;
    private final Repository<Employee> employeeRepository;
    private final Session session;

    public DepartmentService(Repository<Department> departmentRepository,
                             Repository<Employee> employeeRepository,
                             Session session) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.session = session;
    }

    public List<Department> list() {
        return departmentRepository.findAll();
    }

    public List<Department> search(String keyword) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        if (needle.isEmpty()) {
            return list();
        }
        return departmentRepository.findAll(
                department -> department.getName().toLowerCase().contains(needle));
    }

    public Department create(String name, String description) {
        session.requireAdmin();
        String validName = Validators.requireName(name);
        String validDescription = Validators.optionalDescription(description);
        ensureNameUnique(validName, -1);
        return departmentRepository.save(new Department(validName, validDescription));
    }

    public void update(long id, String name, String description) {
        session.requireAdmin();
        Department department = findOrThrow(id);
        String validName = Validators.requireName(name);
        String validDescription = Validators.optionalDescription(description);
        ensureNameUnique(validName, id);
        department.rename(validName);
        department.updateDescription(validDescription);
        departmentRepository.save(department);
    }

    public void delete(long id) {
        session.requireAdmin();
        Department department = findOrThrow(id);
        long count = employeeCount(id);
        if (count > 0) {
            throw new BusinessException("该部门下仍有 " + count
                    + " 名员工，请先调整员工所属部门后再删除");
        }
        departmentRepository.delete(department.getId());
    }

    public long employeeCount(long departmentId) {
        return employeeRepository.findAll(
                employee -> employee.getDepartmentId() == departmentId).size();
    }

    private Department findOrThrow(long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));
    }

    private void ensureNameUnique(String name, long excludeId) {
        boolean duplicate = departmentRepository.findAll(department ->
                        department.getId() != excludeId
                                && department.getName().trim().equalsIgnoreCase(name.trim()))
                .size() > 0;
        if (duplicate) {
            throw new BusinessException("部门名称已存在，请使用其他名称");
        }
    }
}
