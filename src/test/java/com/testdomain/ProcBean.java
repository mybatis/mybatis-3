package com.testdomain;

import java.io.Serializable;

public class ProcBean implements Serializable {

  private String email1;
  private String email2;
  private String status;

  public String getEmail1() {
    return email1;
  }

  public void setEmail1(String email1) {
    this.email1 = email1;
  }

  public String getEmail2() {
    return email2;
  }

  public void setEmail2(String email2) {
    this.email2 = email2;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
