package org.apache.ibatis.builder.model;

import java.io.Serializable;

public class UserDTO implements Serializable {

    private static final long serialVersionUID = -2533018270773798450L;

    private Integer id;

    private String name;

    private Integer age;

    public UserDTO() {

    }

    public UserDTO(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}