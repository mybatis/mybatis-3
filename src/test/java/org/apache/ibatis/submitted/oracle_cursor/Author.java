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

public class Author {
  private Integer id;
  private String name;
  private List<Book> books;

  public Author() {
    super();
  }

  public Author(Integer id, String name) {
    super();
    this.id = id;
    this.name = name;
  }

  public Author(Integer id, String name, List<Book> books) {
    super();
    this.id = id;
    this.name = name;
    this.books = books;
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

  public List<Book> getBooks() {
    return books;
  }

  public void setBooks(List<Book> books) {
    this.books = books;
  }

  @Override
  public int hashCode() {
    return Objects.hash(books, id, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Author)) {
      return false;
    }
    Author other = (Author) obj;
    return Objects.equals(books, other.books) && Objects.equals(id, other.id) && Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return "Author [id=" + id + ", name=" + name + ", books=" + books + "]";
  }
}
