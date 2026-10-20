package com.pdc.learning.persistence.csv;

import com.pdc.learning.model.Score;
import com.pdc.learning.persistence.CsvCodec;
import com.pdc.learning.util.Dates;

/**
 * scores.csv 编解码。
 */
public class ScoreCsvCodec implements CsvCodec<Score> {

    private static final String[] HEADER = {
            "id", "studentId", "classCourseId", "score", "updatedAt"
    };

    @Override
    public String[] header() {
        return HEADER.clone();
    }

    @Override
    public Score parse(String[] fields) {
        long id = parseLong(fields[0], "id");
        long studentId = parseLong(fields[1], "studentId");
        long classCourseId = parseLong(fields[2], "classCourseId");
        double score = parseDouble(fields[3], "score");
        return new Score(id, studentId, classCourseId, score, Dates.parseDateTime(fields[4]));
    }

    @Override
    public String[] format(Score entity) {
        return new String[]{
                String.valueOf(entity.getId()),
                String.valueOf(entity.getStudentId()),
                String.valueOf(entity.getClassCourseId()),
                String.valueOf(entity.getScore()),
                Dates.formatDateTime(entity.getUpdatedAt())
        };
    }

    @Override
    public long idOf(Score entity) {
        return entity.getId();
    }

    @Override
    public Score withId(Score entity, long id) {
        return new Score(id, entity.getStudentId(), entity.getClassCourseId(), entity.getScore(),
                entity.getUpdatedAt() == null ? Dates.now() : entity.getUpdatedAt());
    }

    private static long parseLong(String text, String field) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 不是有效整数");
        }
    }

    private static double parseDouble(String text, String field) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 不是有效数字");
        }
    }
}
