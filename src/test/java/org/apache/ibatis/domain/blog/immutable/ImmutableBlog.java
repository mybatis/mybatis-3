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

import java.util.ArrayList;
import java.util.List;

public class ImmutableBlog {

  private final int id;
  private final String title;
  private final ImmutableAuthor author;
  private final List<ImmutablePost> posts;

  public ImmutableBlog(int id, String title, ImmutableAuthor author, List<ImmutablePost> posts) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.posts = posts;
  }

  public ImmutableBlog(int id, String title, ImmutableAuthor author) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.posts = new ArrayList<>();
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public ImmutableAuthor getAuthor() {
    return author;
  }

  public List<ImmutablePost> getPosts() {
    return posts;
  }

  @Override
  public String toString() {
    return "ImmutableBlog{" + "id=" + id + ", title='" + title + '\'' + ", author=" + author + ", posts=" + posts + '}';
  }
}
