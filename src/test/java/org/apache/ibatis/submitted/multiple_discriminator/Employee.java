package org.apache.ibatis.submitted.multiple_discriminator;

public class Employee extends Person {
    private String jobTitle;
    
    public String getJobTitle() {
        return jobTitle;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}
