package com.pdc.learning.persistence.csv;

import com.pdc.learning.model.Course;
import com.pdc.learning.persistence.CsvCodec;

/**
 * courses.csv 编解码。
 */
public class CourseCsvCodec implements CsvCodec<Course> {

    private static final String[] HEADER = {"id", "code", "name", "description"};

    @Override
    public String[] header() {
        return HEADER.clone();
    }

    @Override
    public Course parse(String[] fields) {
        long id = parseLong(fields[0], "id");
        String code = require(fields[1], "code");
        String name = require(fields[2], "name");
        return new Course(id, code, name, emptyToNull(fields[3]));
    }

    @Override
    public String[] format(Course entity) {
        return new String[]{
                String.valueOf(entity.getId()),
                entity.getCode(),
                entity.getName(),
                nullToEmpty(entity.getDescription())
        };
    }

    @Override
    public long idOf(Course entity) {
        return entity.getId();
    }

    @Override
    public Course withId(Course entity, long id) {
        return new Course(id, entity.getCode(), entity.getName(), entity.getDescription());
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
