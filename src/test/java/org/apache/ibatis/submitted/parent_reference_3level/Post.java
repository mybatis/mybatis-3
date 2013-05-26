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
package org.apache.ibatis.submitted.parent_reference_3level;

import java.util.List;

public class Post {

  private int id;
  private String body;
  private Blog blog;
  private List<Comment> comments;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Blog getBlog() {
    return blog;
  }

  public void setBlog(Blog blog) {
    if (this.blog != null) {
      throw new RuntimeException("Setter called twice");
    }    
    this.blog = blog;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    if (this.body != null) {
      throw new RuntimeException("Setter called twice");
    }    
    this.body = body;
  }

  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    if (this.comments != null) {
      throw new RuntimeException("Setter called twice");
    }    
    this.comments = comments;
  }
}
