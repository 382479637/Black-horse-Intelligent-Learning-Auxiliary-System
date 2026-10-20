package com.pdc.learning.persistence;

import com.pdc.learning.model.Identifiable;

/**
 * 单个实体的 CSV 编解码策略。
 */
public interface CsvCodec<T extends Identifiable> {

    String[] header();

    T parse(String[] fields);

    String[] format(T entity);

    long idOf(T entity);

    T withId(T entity, long id);
}
