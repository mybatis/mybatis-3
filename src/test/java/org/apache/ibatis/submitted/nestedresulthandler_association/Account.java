/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.nestedresulthandler_association;

import java.util.Date;

public class Account {
  private String accountUuid;

  private String accountName;

  private Date birthDate;

  private AccountAddress address;

  public String getAccountUuid() {
    return accountUuid;
  }

  public void setAccountUuid(String accountUuid) {
    this.accountUuid = accountUuid;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }

  public AccountAddress getAddress() {
    return address;
  }

  public void setAddress(AccountAddress address) {
    this.address = address;
  }
}
