package org.apache.ibatis.submitted.camelcase;

public class Camel {
  
  private String id;
  private String firstName;
  private String LAST_NAME;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  } 
  public String getLAST_NAME() {
    return LAST_NAME;
  }
  public void setLAST_NAME(String last_name) {
    LAST_NAME = last_name;
  }

}
