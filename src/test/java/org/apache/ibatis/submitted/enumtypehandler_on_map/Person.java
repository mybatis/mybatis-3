package org.apache.ibatis.submitted.enumtypehandler_on_map;

public class Person {
    
    public enum Type {
        PERSON,
        EMPLOYEE
    }
    
    private Long id;
    private String firstName;
    private String lastName;
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
