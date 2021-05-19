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
package org.apache.ibatis.submitted.complex_property;

public class User {
  private Long id;

  /*
   * user specified user ID
   */
  private String username;

  /*
   * encrypted password
   */
  private EncryptedString password;

  boolean administrator;

  public User() {
    setUsername(new String());
    setPassword(new EncryptedString());
    setAdministrator(false);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String arg) {
    this.username = arg;
  }

  public EncryptedString getPassword() {
    return password;
  }

  public void setPassword(EncryptedString arg) {
    this.password = arg;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long oid) {
    this.id = oid;
  }

  public boolean isAdministrator() {
    return administrator;
  }

  public void setAdministrator(boolean arg) {
    this.administrator = arg;
  }
}
