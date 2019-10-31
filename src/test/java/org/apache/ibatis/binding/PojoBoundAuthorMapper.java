/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.binding;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Alexander Pozdnyakov
 */
public interface PojoBoundAuthorMapper {

  @Select({
    "SELECT ",
    "  ID as AUTHOR_ID,",
    "  USERNAME as AUTHOR_USERNAME,",
    "  PASSWORD as AUTHOR_PASSWORD,",
    "  EMAIL as AUTHOR_EMAIL,",
    "  BIO as AUTHOR_BIO",
    "FROM AUTHOR WHERE ID = #{id}"})
  AnnotatedAuthor selectAuthor(long id);

  @Select({
    "SELECT ",
    "  ID as AUTHOR_ID,",
    "  USERNAME as AUTHOR_USERNAME,",
    "  PASSWORD as AUTHOR_PASSWORD,",
    "  EMAIL as AUTHOR_EMAIL,",
    "  BIO as AUTHOR_BIO",
    "FROM AUTHOR"})
  List<AnnotatedAuthor> selectAllAuthors();

  @Select("SELECT * FROM " +
          "post WHERE blog_id = #{blogId}")
  List<AnnotatedPost> selectPostsByBlogId(long blogId);

  @Select({
    "SELECT *",
    "FROM blog"
  })
  List<AnnotatedBlog> selectBlogsWithAuthorAndPosts();

  class AnnotatedAuthor {
    private Long id;
    @Result(column = "AUTHOR_USERNAME")
    private String username;
    @Result(property = "password", column = "AUTHOR_PASSWORD")
    private String password;
    private String email;
    @Result(property = "bio", column = "AUTHOR_BIO")
    private String bio;

    @ConstructorArgs({
      @Arg(column = "AUTHOR_ID", id = true, javaType = Long.class),
      @Arg(column = "AUTHOR_EMAIL", javaType = String.class)
    })
    public AnnotatedAuthor(Long id, String email) {
      this.id = id;
      this.email = email;
    }

    public Long getId() {
      return id;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

    public String getEmail() {
      return email;
    }

    public String getBio() {
      return bio;
    }
  }

  class AnnotatedBlog {
    @Result(column = "id")
    private Long id;
    @Result(column = "title")
    private String title;
    @Result(column = "author_id", one = @One(select = "selectAuthor"))
    private AnnotatedAuthor author;
    @Result(column = "id", many = @Many(select = "selectPostsByBlogId"))
    private List<AnnotatedPost> posts;

    public AnnotatedAuthor getAuthor() {
      return author;
    }

    public List<AnnotatedPost> getPosts() {
      return posts;
    }
  }

  class AnnotatedPost {
    @Result(column = "id")
    private Long id;
    @Result(column = "section")
    private String section;
  }
}
