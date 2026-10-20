package com.pdc.learning.persistence.csv;

import com.pdc.learning.model.Student;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.StudentStatus;
import com.pdc.learning.persistence.CsvCodec;
import com.pdc.learning.util.Dates;

/**
 * students.csv 编解码。
 */
public class StudentCsvCodec implements CsvCodec<Student> {

    private static final String[] HEADER = {
            "id", "studentNo", "name", "gender", "phone", "classId", "enrollmentDate", "status"
    };

    @Override
    public String[] header() {
        return HEADER.clone();
    }

    @Override
    public Student parse(String[] fields) {
        long id = parseLong(fields[0], "id");
        String studentNo = require(fields[1], "studentNo");
        String name = require(fields[2], "name");
        Gender gender = parseEnum(Gender.class, fields[3], "gender");
        String phone = emptyToNull(fields[4]);
        long classId = parseLong(fields[5], "classId");
        StudentStatus status = parseEnum(StudentStatus.class, fields[7], "status");
        return new Student(id, studentNo, name, gender, phone, classId,
                Dates.parseDate(fields[6]), status);
    }

    @Override
    public String[] format(Student entity) {
        return new String[]{
                String.valueOf(entity.getId()),
                entity.getStudentNo(),
                entity.getName(),
                entity.getGender().name(),
                nullToEmpty(entity.getPhone()),
                String.valueOf(entity.getClassId()),
                Dates.formatDate(entity.getEnrollmentDate()),
                entity.getStatus().name()
        };
    }

    @Override
    public long idOf(Student entity) {
        return entity.getId();
    }

    @Override
    public Student withId(Student entity, long id) {
        return new Student(id, entity.getStudentNo(), entity.getName(), entity.getGender(),
                entity.getPhone(), entity.getClassId(), entity.getEnrollmentDate(),
                entity.getStatus());
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
