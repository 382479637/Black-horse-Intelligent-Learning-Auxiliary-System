package com.pdc.learning.model;

/**
 * 部门。
 */
public class Department extends AbstractEntity {

    private String name;
    private String description;

    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Department(long id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
