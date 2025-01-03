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

import java.util.Date;
import java.util.List;

import org.apache.ibatis.domain.blog.Section;

public class ImmutablePost {

  private final int id;
  private final ImmutableAuthor author;
  private final Date createdOn;
  private final Section section;
  private final String subject;
  private final String body;
  private final List<ImmutableComment> comments;
  private final List<ImmutableTag> tags;

  public ImmutablePost(int id, ImmutableAuthor author, Date createdOn, Section section, String subject, String body,
      List<ImmutableComment> comments, List<ImmutableTag> tags) {
    this.id = id;
    this.author = author;
    this.createdOn = createdOn;
    this.section = section;
    this.subject = subject;
    this.body = body;
    this.comments = comments;
    this.tags = tags;
  }

  public ImmutablePost(int id, ImmutableAuthor author, Date createdOn, Section section, String subject, String body) {
    this.id = id;
    this.author = author;
    this.createdOn = createdOn;
    this.section = section;
    this.subject = subject;
    this.body = body;
    this.comments = List.of();
    this.tags = List.of();
  }

  public List<ImmutableTag> getTags() {
    return tags;
  }

  public int getId() {
    return id;
  }

  public ImmutableAuthor getAuthor() {
    return author;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public Section getSection() {
    return section;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }

  public List<ImmutableComment> getComments() {
    return comments;
  }

  @Override
  public String toString() {
    return "ImmutablePost{" + "id=" + id + ", author=" + author + ", createdOn=" + createdOn + ", section=" + section
        + ", subject='" + subject + '\'' + ", body='" + body + '\'' + ", comments=" + comments + ", tags=" + tags + '}';
  }
}
