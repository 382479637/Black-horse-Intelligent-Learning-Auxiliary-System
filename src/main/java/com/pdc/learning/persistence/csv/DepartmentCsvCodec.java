package com.pdc.learning.persistence.csv;

import com.pdc.learning.model.Department;
import com.pdc.learning.persistence.CsvCodec;

/**
 * departments.csv 编解码。
 */
public class DepartmentCsvCodec implements CsvCodec<Department> {

    private static final String[] HEADER = {"id", "name", "description"};

    @Override
    public String[] header() {
        return HEADER.clone();
    }

    @Override
    public Department parse(String[] fields) {
        long id = parseLong(fields[0], "id");
        String name = require(fields[1], "name");
        return new Department(id, name, emptyToNull(fields[2]));
    }

    @Override
    public String[] format(Department entity) {
        return new String[]{
                String.valueOf(entity.getId()),
                entity.getName(),
                nullToEmpty(entity.getDescription())
        };
    }

    @Override
    public long idOf(Department entity) {
        return entity.getId();
    }

    @Override
    public Department withId(Department entity, long id) {
        return new Department(id, entity.getName(), entity.getDescription());
    }

    private static long parseLong(String text, String field) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 不是有效整数");
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
