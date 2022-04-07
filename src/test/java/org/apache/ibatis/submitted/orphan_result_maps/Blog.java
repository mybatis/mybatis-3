/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.orphan_result_maps;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.submitted.parent_reference_3level.Post;

public class Blog {

  private int id;
  private String title;
  private List<Post> posts;

  public Blog(@Param("id") int id, @Param("title") String title) {
    this.id = id;
    this.title = title;
    this.posts = new ArrayList<>();
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public List<Post> getPosts() {
    return posts;
  }
}
