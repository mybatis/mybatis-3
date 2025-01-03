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

public class ImmutableComment {

  private final int id;
  private final String name;
  private final String comment;

  public ImmutableComment(int id, String name, String comment) {
    this.id = id;
    this.name = name;
    this.comment = comment;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  @Override
  public String toString() {
    return "ImmutableComment{" + "id=" + id + ", name='" + name + '\'' + ", comment='" + comment + '\'' + '}';
  }
}
