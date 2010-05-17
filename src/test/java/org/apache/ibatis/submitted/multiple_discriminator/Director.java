package org.apache.ibatis.submitted.multiple_discriminator;

public class Director extends Employee {
    private String department;
    
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
}
