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

import java.util.Objects;

public class Book2 {
  private Integer id;
  private String name;
  private Author author;

  public Book2() {
    super();
  }

  public Book2(Integer id, String name, Author author) {
    super();
    this.id = id;
    this.name = name;
    this.author = author;
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

  public Author getAuthor() {
    return author;
  }

  public void setAuthor(Author author) {
    this.author = author;
  }

  @Override
  public int hashCode() {
    return Objects.hash(author, id, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Book2)) {
      return false;
    }
    Book2 other = (Book2) obj;
    return Objects.equals(author, other.author) && Objects.equals(id, other.id) && Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return "Book2 [id=" + id + ", name=" + name + ", author=" + author + "]";
  }
}
