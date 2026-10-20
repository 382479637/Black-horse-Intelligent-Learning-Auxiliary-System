package com.pdc.learning.model;

import java.util.Objects;

/**
 * 统一 ID、相等性与“新实体”判断。
 *
 * <p>ID 由仓储分配；id == 0 表示尚未保存。相等性按类型 + 非零 ID 判断，
 * 避免两个尚未保存的实体被误认为同一条记录。</p>
 */
public abstract class AbstractEntity implements Identifiable {

    private long id;

    protected AbstractEntity() {
    }

    protected AbstractEntity(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    protected void setId(long id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AbstractEntity that = (AbstractEntity) other;
        return id != 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
