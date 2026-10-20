package com.pdc.learning.persistence.csv;

import com.pdc.learning.exception.DataAccessException;
import com.pdc.learning.model.Identifiable;
import com.pdc.learning.persistence.CsvCodec;
import com.pdc.learning.persistence.LoadDiagnostics;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.CsvUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 通用 CSV 仓储：内存索引 + 单文件临时文件原子替换。
 *
 * @param <T> 必须实现 {@link Identifiable} 的实体
 */
public class CsvRepository<T extends Identifiable> implements Repository<T> {

    private final Path file;
    private final CsvCodec<T> codec;
    private final Map<Long, T> store = new LinkedHashMap<>();
    private final Map<Long, Integer> lineNumbers = new LinkedHashMap<>();
    private long nextId = 1;

    public CsvRepository(Path file, CsvCodec<T> codec) {
        this.file = file;
        this.codec = codec;
    }

    public Path file() {
        return file;
    }

    public String[] header() {
        return codec.header();
    }

    public int lineOf(long id) {
        return lineNumbers.getOrDefault(id, 0);
    }

    /**
     * 读取 CSV。文件不存在或只有表头时视为空仓储。
     */
    public void load(LoadDiagnostics diagnostics) {
        store.clear();
        lineNumbers.clear();
        nextId = 1;
        if (!Files.exists(file)) {
            diagnostics.recordStats(file.getFileName().toString(), 0, 0);
            return;
        }
        int loaded = 0;
        int skipped = 0;
        String fileName = file.getFileName().toString();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (first) {
                    first = false;
                    if (line.startsWith("﻿")) {
                        line = line.substring(1);
                    }
                    if (Arrays.equals(CsvUtil.split(line), codec.header())) {
                        continue;
                    }
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = CsvUtil.split(line);
                if (fields.length != codec.header().length) {
                    diagnostics.add(fileName, lineNo, "field-count",
                            "字段数量为 " + fields.length + "，应为 " + codec.header().length);
                    skipped++;
                    continue;
                }
                T entity;
                try {
                    entity = codec.parse(fields);
                } catch (RuntimeException e) {
                    diagnostics.add(fileName, lineNo, "parse", safeMessage(e));
                    skipped++;
                    continue;
                }
                long id = codec.idOf(entity);
                if (id <= 0) {
                    diagnostics.add(fileName, lineNo, "id", "ID 必须为正整数");
                    skipped++;
                    continue;
                }
                if (store.containsKey(id)) {
                    diagnostics.add(fileName, lineNo, "duplicate-id", "ID " + id + " 重复，保留首条");
                    skipped++;
                    continue;
                }
                store.put(id, entity);
                lineNumbers.put(id, lineNo);
                if (id >= nextId) {
                    nextId = id + 1;
                }
                loaded++;
            }
        } catch (IOException e) {
            throw new DataAccessException("读取数据文件失败：" + fileName, e);
        }
        diagnostics.recordStats(fileName, loaded, skipped);
    }

    private String safeMessage(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? "无法解析记录" : message;
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<T> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public T save(T entity) {
        T toStore = entity;
        if (entity.getId() == 0) {
            toStore = codec.withId(entity, nextId++);
        } else if (entity.getId() >= nextId) {
            nextId = entity.getId() + 1;
        }
        store.put(toStore.getId(), toStore);
        lineNumbers.putIfAbsent(toStore.getId(), 0);
        writeAtomically();
        return toStore;
    }

    @Override
    public void delete(long id) {
        store.remove(id);
        lineNumbers.remove(id);
        writeAtomically();
    }

    @Override
    public void saveAll() {
        writeAtomically();
    }

    /**
     * 仅供 DataLoader 移除坏行，不写文件。
     */
    public void removeInMemory(long id) {
        store.remove(id);
        lineNumbers.remove(id);
    }

    @Override
    public List<T> findAll(Predicate<T> filter) {
        List<T> result = new ArrayList<>();
        for (T entity : store.values()) {
            if (filter.test(entity)) {
                result.add(entity);
            }
        }
        return result;
    }

    @Override
    public Optional<T> findFirst(Predicate<T> filter) {
        for (T entity : store.values()) {
            if (filter.test(entity)) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    private void writeAtomically() {
        Path parent = file.toAbsolutePath().getParent();
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                writer.write(CsvUtil.join(codec.header()));
                writer.newLine();
                for (T entity : store.values()) {
                    writer.write(CsvUtil.join(codec.format(entity)));
                    writer.newLine();
                }
            }
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // 清理失败不覆盖原始错误
            }
            throw new DataAccessException("写入数据文件失败：" + file.getFileName(), e);
        }
    }
}
