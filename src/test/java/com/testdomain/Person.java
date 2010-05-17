package com.testdomain;

public class Person {
  private Integer id;

  private String firstName;

  private String lastName;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("id: ");
    sb.append(id);
    sb.append(", firstName: ");
    sb.append(firstName);
    sb.append(", lastName: ");
    sb.append(lastName);

    return sb.toString();
  }
}
