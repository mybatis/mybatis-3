/*
 *    Copyright 2009-2025 the original author or authors.
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

package org.apache.ibatis.submitted.oracle_cursor;

import java.util.List;
import java.util.Objects;

public class Author2 {
  private Integer id;
  private String name;
  private List<String> bookNames;

  public Author2() {
    super();
  }

  public Author2(Integer id, String name, List<String> bookNames) {
    super();
    this.id = id;
    this.name = name;
    this.bookNames = bookNames;
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

  public List<String> getBookNames() {
    return bookNames;
  }

  public void setBookNames(List<String> bookNames) {
    this.bookNames = bookNames;
  }

  @Override
  public int hashCode() {
    return Objects.hash(bookNames, id, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Author2)) {
      return false;
    }
    Author2 other = (Author2) obj;
    return Objects.equals(bookNames, other.bookNames) && Objects.equals(id, other.id)
        && Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return "Author2 [id=" + id + ", name=" + name + ", bookNames=" + bookNames + "]";
  }
}
