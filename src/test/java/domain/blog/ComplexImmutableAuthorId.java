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
package domain.blog;

public class ComplexImmutableAuthorId {
  protected final int id;
  protected final String email;
  protected final String username;
  protected final String password;

  public ComplexImmutableAuthorId(int aId, String aEmail, String aUsername, String aPassword) {
    id = aId;
    email = aEmail;
    username = aUsername;
    password = aPassword;
  }

  public int getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ComplexImmutableAuthorId that = (ComplexImmutableAuthorId) o;

    if (id != that.id) {
      return false;
    }
    if (email != null ? !email.equals(that.email) : that.email != null) {
      return false;
    }
    if (password != null ? !password.equals(that.password) : that.password != null) {
      return false;
    }
    if (username != null ? !username.equals(that.username) : that.username != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int myresult = id;
    myresult = 31 * myresult + (email != null ? email.hashCode() : 0);
    myresult = 31 * myresult + (username != null ? username.hashCode() : 0);
    myresult = 31 * myresult + (password != null ? password.hashCode() : 0);
    return myresult;
  }
}
