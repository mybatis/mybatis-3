/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.submitted.constructor_columnprefix;

public class Article {

  private EntityKey id;

  private String name;

  private Author author;

  private Author coauthor;

  public Article(EntityKey id, String name, Author author, Author coauthor) {
    super();
    this.id = id;
    this.name = name;
    this.author = author;
    this.coauthor = coauthor;
  }

  public EntityKey getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Author getAuthor() {
    return author;
  }

  public Author getCoauthor() {
    return coauthor;
  }
}
