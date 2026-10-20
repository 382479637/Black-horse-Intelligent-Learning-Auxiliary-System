package com.pdc.learning.model;

import com.pdc.learning.model.enums.Gender;

/**
 * 员工与学生的公共人员资料。
 */
public abstract class Person extends AbstractEntity {

    private String name;
    private Gender gender;
    private String phone;

    protected Person(String name, Gender gender, String phone) {
        this.name = name;
        this.gender = gender == null ? Gender.UNKNOWN : gender;
        this.phone = phone;
    }

    protected Person(long id, String name, Gender gender, String phone) {
        super(id);
        this.name = name;
        this.gender = gender == null ? Gender.UNKNOWN : gender;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public void updateProfile(String name, Gender gender, String phone) {
        this.name = name;
        this.gender = gender == null ? Gender.UNKNOWN : gender;
        this.phone = phone;
    }
}
