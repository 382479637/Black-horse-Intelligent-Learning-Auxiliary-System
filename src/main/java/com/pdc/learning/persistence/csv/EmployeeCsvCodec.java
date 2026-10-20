package com.pdc.learning.persistence.csv;

import com.pdc.learning.model.Employee;
import com.pdc.learning.model.enums.EmployeeStatus;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.Role;
import com.pdc.learning.persistence.CsvCodec;
import com.pdc.learning.util.Dates;

/**
 * employees.csv 编解码。
 */
public class EmployeeCsvCodec implements CsvCodec<Employee> {

    private static final String[] HEADER = {
            "id", "username", "passwordHash", "passwordSalt", "name", "gender",
            "phone", "email", "departmentId", "role", "status", "hireDate"
    };

    @Override
    public String[] header() {
        return HEADER.clone();
    }

    @Override
    public Employee parse(String[] fields) {
        long id = parseLong(fields[0], "id");
        String username = require(fields[1], "username");
        String hash = emptyToNull(fields[2]);
        String salt = emptyToNull(fields[3]);
        String name = require(fields[4], "name");
        Gender gender = parseEnum(Gender.class, fields[5], "gender");
        String phone = emptyToNull(fields[6]);
        String email = emptyToNull(fields[7]);
        long departmentId = parseLong(fields[8], "departmentId");
        Role role = parseEnum(Role.class, fields[9], "role");
        EmployeeStatus status = parseEnum(EmployeeStatus.class, fields[10], "status");
        Employee employee = new Employee(id, username, name, gender, phone, email,
                departmentId, role, status, Dates.parseDate(fields[11]));
        employee.updatePassword(salt, hash);
        return employee;
    }

    @Override
    public String[] format(Employee entity) {
        return new String[]{
                String.valueOf(entity.getId()),
                entity.getUsername(),
                nullToEmpty(entity.getPasswordHash()),
                nullToEmpty(entity.getPasswordSalt()),
                entity.getName(),
                entity.getGender().name(),
                nullToEmpty(entity.getPhone()),
                nullToEmpty(entity.getEmail()),
                String.valueOf(entity.getDepartmentId()),
                entity.getRole().name(),
                entity.getStatus().name(),
                Dates.formatDate(entity.getHireDate())
        };
    }

    @Override
    public long idOf(Employee entity) {
        return entity.getId();
    }

    @Override
    public Employee withId(Employee entity, long id) {
        Employee copy = new Employee(id, entity.getUsername(), entity.getName(),
                entity.getGender(), entity.getPhone(), entity.getEmail(),
                entity.getDepartmentId(), entity.getRole(), entity.getStatus(),
                entity.getHireDate());
        copy.updatePassword(entity.getPasswordSalt(), entity.getPasswordHash());
        return copy;
    }

    private static long parseLong(String text, String field) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 不是有效整数");
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String text, String field) {
        try {
            return Enum.valueOf(type, text.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(field + " 枚举值非法");
        }
    }

    private static String require(String text, String field) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(field + " 不能为空");
        }
        return text.trim();
    }

    private static String emptyToNull(String text) {
        return text == null || text.isEmpty() ? null : text;
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }
}
