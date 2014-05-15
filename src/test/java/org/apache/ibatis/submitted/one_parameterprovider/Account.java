package org.apache.ibatis.submitted.one_parameterprovider;

public class Account {
  private Long id;
  private String name;
  private Accountant accountant;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Accountant getAccountant() {
    return accountant;
  }

  public void setAccountant(Accountant accountant) {
    this.accountant = accountant;
  }
}
