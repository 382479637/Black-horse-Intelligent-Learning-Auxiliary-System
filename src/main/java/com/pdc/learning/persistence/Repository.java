package com.pdc.learning.persistence;

import com.pdc.learning.model.Identifiable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 泛型仓储契约。service 只依赖该接口，不依赖具体 CSV 实现。
 */
public interface Repository<T extends Identifiable> {

    List<T> findAll();

    Optional<T> findById(long id);

    /**
     * 保存实体；id == 0 时分配新 ID。保存后立即写文件。
     *
     * @return 带 ID 的已保存实体（新实体返回副本）
     */
    T save(T entity);

    void delete(long id);

    void saveAll();

    List<T> findAll(Predicate<T> filter);

    Optional<T> findFirst(Predicate<T> filter);
}
