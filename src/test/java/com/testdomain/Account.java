package com.testdomain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Account implements Serializable {

  private int id;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private int[] ids;
  private int age;
  private Date dateAdded;
  private Account account;
  private List accountList;
  private boolean bannerOption;
  private boolean cartOption;

  public Account() {
  }

  public Account(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int[] getIds() {
    return ids;
  }

  public void setIds(int[] ids) {
    this.ids = ids;
  }

  /**
   * @return Returns the age.
   */
  public int getAge() {
    return age;
  }

  /**
   * @param age The age to set.
   */
  public void setAge(int age) {
    this.age = age;
  }

  /**
   * @return Returns the dateAdded.
   */
  public Date getDateAdded() {
    return dateAdded;
  }

  /**
   * @param dateAdded The dateAdded to set.
   */
  public void setDateAdded(Date dateAdded) {
    this.dateAdded = dateAdded;
  }

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

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public List getAccountList() {
    return accountList;
  }

  public void setAccountList(List accountList) {
    this.accountList = accountList;
  }

  public boolean isBannerOption() {
    return bannerOption;
  }

  public void setBannerOption(boolean bannerOption) {
    this.bannerOption = bannerOption;
  }


  public boolean isCartOption() {
    return cartOption;
  }

  public void setCartOption(boolean cartOption) {
    this.cartOption = cartOption;
  }
}
