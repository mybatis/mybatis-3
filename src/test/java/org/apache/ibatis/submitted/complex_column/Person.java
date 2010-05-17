package org.apache.ibatis.submitted.complex_column;

public class Person {
    private Long id;
    private String firstName;
    private String lastName;
    private Person parent;
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
    public Person getParent() {
        return parent;
    }
    public void setParent(Person parent) {
        this.parent = parent;
    }
}
