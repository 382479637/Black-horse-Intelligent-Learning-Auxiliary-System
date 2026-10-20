package com.pdc.learning.persistence.csv;

import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.persistence.CsvCodec;
import com.pdc.learning.util.Dates;

/**
 * class_courses.csv 编解码。
 */
public class ClassCourseCsvCodec implements CsvCodec<ClassCourse> {

    private static final String[] HEADER = {
            "id", "classId", "courseId", "teacherId", "createdAt"
    };

    @Override
    public String[] header() {
        return HEADER.clone();
    }

    @Override
    public ClassCourse parse(String[] fields) {
        long id = parseLong(fields[0], "id");
        long classId = parseLong(fields[1], "classId");
        long courseId = parseLong(fields[2], "courseId");
        Long teacherId = parseOptionalLong(fields[3], "teacherId");
        return new ClassCourse(id, classId, courseId, teacherId,
                Dates.parseDateTime(fields[4]));
    }

    @Override
    public String[] format(ClassCourse entity) {
        return new String[]{
                String.valueOf(entity.getId()),
                String.valueOf(entity.getClassId()),
                String.valueOf(entity.getCourseId()),
                entity.getTeacherId() == null ? "" : String.valueOf(entity.getTeacherId()),
                Dates.formatDateTime(entity.getCreatedAt())
        };
    }

    @Override
    public long idOf(ClassCourse entity) {
        return entity.getId();
    }

    @Override
    public ClassCourse withId(ClassCourse entity, long id) {
        return new ClassCourse(id, entity.getClassId(), entity.getCourseId(),
                entity.getTeacherId(),
                entity.getCreatedAt() == null ? Dates.now() : entity.getCreatedAt());
    }

    private static long parseLong(String text, String field) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 不是有效整数");
        }
    }

    private static Long parseOptionalLong(String text, String field) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return parseLong(text, field);
    }
}
