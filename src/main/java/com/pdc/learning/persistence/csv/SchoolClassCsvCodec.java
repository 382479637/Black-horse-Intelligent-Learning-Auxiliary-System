package com.pdc.learning.persistence.csv;

import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.enums.ClassStatus;
import com.pdc.learning.persistence.CsvCodec;
import com.pdc.learning.util.Dates;

/**
 * classes.csv 编解码。
 */
public class SchoolClassCsvCodec implements CsvCodec<SchoolClass> {

    private static final String[] HEADER = {
            "id", "code", "name", "headTeacherId", "startDate", "endDate", "capacity", "status"
    };

    @Override
    public String[] header() {
        return HEADER.clone();
    }

    @Override
    public SchoolClass parse(String[] fields) {
        long id = parseLong(fields[0], "id");
        String code = require(fields[1], "code");
        String name = require(fields[2], "name");
        Long headTeacherId = parseOptionalLong(fields[3], "headTeacherId");
        int capacity = parseInt(fields[6], "capacity");
        ClassStatus status = parseEnum(ClassStatus.class, fields[7], "status");
        return new SchoolClass(id, code, name, headTeacherId,
                Dates.parseDate(fields[4]), Dates.parseDate(fields[5]), capacity, status);
    }

    @Override
    public String[] format(SchoolClass entity) {
        return new String[]{
                String.valueOf(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getHeadTeacherId() == null ? "" : String.valueOf(entity.getHeadTeacherId()),
                Dates.formatDate(entity.getStartDate()),
                Dates.formatDate(entity.getEndDate()),
                String.valueOf(entity.getCapacity()),
                entity.getStatus().name()
        };
    }

    @Override
    public long idOf(SchoolClass entity) {
        return entity.getId();
    }

    @Override
    public SchoolClass withId(SchoolClass entity, long id) {
        return new SchoolClass(id, entity.getCode(), entity.getName(), entity.getHeadTeacherId(),
                entity.getStartDate(), entity.getEndDate(), entity.getCapacity(),
                entity.getStatus());
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

    private static int parseInt(String text, String field) {
        try {
            return Integer.parseInt(text.trim());
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
}
