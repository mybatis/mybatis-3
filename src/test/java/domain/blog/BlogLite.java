/*
 * Copyright 2013 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package domain.blog;

import java.util.List;

public class BlogLite {

  private int id;
  private List<PostLite> posts;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<PostLite> getPosts() {
    return posts;
  }

  public void setPosts(List<PostLite> posts) {
    this.posts = posts;
  }
}
