/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
