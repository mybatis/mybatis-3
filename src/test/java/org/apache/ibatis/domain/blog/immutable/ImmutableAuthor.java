/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.domain.blog.immutable;

import org.apache.ibatis.domain.blog.Section;

public class ImmutableAuthor {

  private final int id;
  private final String username;
  private final String password;
  private final String email;
  private final String bio;
  private final Section favouriteSection;

  public ImmutableAuthor(int id, String username, String password, String email, String bio, Section section) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.bio = bio;
    this.favouriteSection = section;
  }

  public int getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

  public String getBio() {
    return bio;
  }

  public Section getFavouriteSection() {
    return favouriteSection;
  }

  @Override
  public String toString() {
    return "ImmutableAuthor{" + "id=" + id + ", username='" + username + '\'' + ", password='" + password + '\''
        + ", email='" + email + '\'' + ", bio='" + bio + '\'' + ", favouriteSection=" + favouriteSection + '}';
  }
}
