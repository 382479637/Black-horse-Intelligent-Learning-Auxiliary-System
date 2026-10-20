package com.pdc.learning.dto;

import java.util.List;

/**
 * 学生预警结果。hitRules 收集全部命中规则，空列表表示没有命中。
 */
public record WarningResult(
        long studentId,
        String studentNo,
        String name,
        WarningLevel level,
        Double average,
        Integer failedCount,
        List<String> hitRules) {

    public WarningResult {
        hitRules = hitRules == null ? List.of() : List.copyOf(hitRules);
    }
}
