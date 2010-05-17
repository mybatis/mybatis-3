package com.submitted.resultmap;

import java.util.List;

public class Person {
  private Integer id;
  private String name;
  private List phoneNumbers;
  private List addresses;

  public List getAddresses() {
    return addresses;
  }

  public void setAddresses(List addresses) {
    this.addresses = addresses;
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

  public List getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Name: ");
    sb.append(name);

    if (phoneNumbers != null && phoneNumbers.size() > 0) {
      sb.append(" Phone Numbers: <");

      for (int i = 0; i < phoneNumbers.size(); i++) {
        if (i > 0) {
          sb.append(" ");
        }
        sb.append(phoneNumbers.get(i));
      }
      sb.append(">");
    }

    if (addresses != null && addresses.size() > 0) {
      sb.append(" Addresses: <");

      for (int i = 0; i < addresses.size(); i++) {
        if (i > 0) {
          sb.append(" ");
        }
        sb.append(addresses.get(i));
      }
      sb.append(">");
    }

    return sb.toString();
  }

}
