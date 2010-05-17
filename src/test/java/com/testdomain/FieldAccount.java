package com.testdomain;

import java.io.Serializable;

public class FieldAccount implements Serializable {

  private int id;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private FieldAccount account;

  public int id() {
    return id;
  }

  public void id(int id) {
    this.id = id;
  }

  public String firstName() {
    return firstName;
  }

  public void firstName(String firstName) {
    this.firstName = firstName;
  }

  public String lastName() {
    return lastName;
  }

  public void lastName(String lastName) {
    this.lastName = lastName;
  }

  public String emailAddress() {
    return emailAddress;
  }

  public void emailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public FieldAccount account() {
    return account;
  }

  public void account(FieldAccount account) {
    this.account = account;
  }
}
