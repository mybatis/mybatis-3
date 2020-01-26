package org.apache.ibatis.submitted.expand_collection_param.model;

public class User {

  public User(Integer id, String firstName, String lastName, UserRole role, UserStatus status) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.role = role;
    this.status = status;
  }

  private Integer id;
  private String firstName;
  private String lastName;
  private UserRole role;
  private UserStatus status;

  public Integer getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public UserStatus getStatus() {
    return status;
  }

  public UserRole getRole() {
    return role;
  }
}

