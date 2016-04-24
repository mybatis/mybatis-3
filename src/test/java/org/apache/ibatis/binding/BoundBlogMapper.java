/**
 *    Copyright 2009-2016 the original author or authors.
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

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Blog;
import org.apache.ibatis.domain.blog.DraftPost;
import org.apache.ibatis.domain.blog.Post;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

@CacheNamespace(readWrite = false)
public interface BoundBlogMapper {

  //======================================================

  Blog selectBlogWithPostsUsingSubSelect(int id);

  //======================================================

  int selectRandom();

  //======================================================

  @Select({ "SELECT * FROM blog"})
  @MapKey("id")
  Map<Integer,Blog> selectBlogsAsMapById();

  @Select({ "SELECT * FROM blog ORDER BY id"})
  @MapKey("id")
  Map<Integer,Blog> selectRangeBlogsAsMapById(RowBounds rowBounds);

  //======================================================

  @Select({
      "SELECT *",
      "FROM blog"
  })
  List<Blog> selectBlogs();

  @Select({
          "SELECT *",
          "FROM blog",
          "ORDER BY id"
  })
  @ResultType(Blog.class)
  void collectRangeBlogs(ResultHandler<Object> blog, RowBounds rowBounds);


  @Select({
          "SELECT *",
          "FROM blog",
          "ORDER BY id"
  })
  Cursor<Blog> openRangeBlogs(RowBounds rowBounds);

  //======================================================

  List<Blog> selectBlogsFromXML();

  //======================================================

  @Select({
      "SELECT *",
      "FROM blog"
  })
  List<Map<String,Object>> selectBlogsAsMaps();

  //======================================================

  @SelectProvider(type = BoundBlogSql.class, method = "selectBlogsSql")
  List<Blog> selectBlogsUsingProvider();

  //======================================================

  @Select("SELECT * FROM post ORDER BY id")
  @TypeDiscriminator(
      column = "draft",
      javaType = String.class,
      cases = {@Case(value = "1", type = DraftPost.class)}
  )
  List<Post> selectPosts();

  //======================================================

  @Select("SELECT * FROM post ORDER BY id")
  @Results({
      @Result(id = true, property = "id", column = "id")
  })
  @TypeDiscriminator(
      column = "draft",
      javaType = int.class,
      cases = {@Case(value = "1", type = DraftPost.class,
          results = {@Result(id = true, property = "id", column = "id")})}
  )
  List<Post> selectPostsWithResultMap();

  //======================================================

  @Select("SELECT * FROM " +
      "blog WHERE id = #{id}")
  Blog selectBlog(int id);

  //======================================================

  @Select("SELECT * FROM " +
      "blog WHERE id = #{id}")
  @ConstructorArgs({
      @Arg(column = "id", javaType = int.class, id = true),
      @Arg(column = "title", javaType = String.class),
      @Arg(column = "author_id", javaType = Author.class, select = "org.apache.ibatis.binding.BoundAuthorMapper.selectAuthor"),
      @Arg(column = "id", javaType = List.class, select = "selectPostsForBlog")
  })
  Blog selectBlogUsingConstructor(int id);

  Blog selectBlogUsingConstructorWithResultMap(int i);

  Blog selectBlogUsingConstructorWithResultMapAndProperties(int i);
  
  Blog selectBlogUsingConstructorWithResultMapCollection(int i);
  
  Blog selectBlogByIdUsingConstructor(int id);

  //======================================================

  @Select("SELECT * FROM " +
      "blog WHERE id = #{id}")
  Map<String,Object> selectBlogAsMap(Map<String,Object> params);

  //======================================================

  @Select("SELECT * FROM " +
    "post WHERE subject like #{query}")
  List<Post> selectPostsLike(RowBounds bounds, String query);

  //======================================================

  @Select("SELECT * FROM " +
    "post WHERE subject like #{subjectQuery} and body like #{bodyQuery}")
  List<Post> selectPostsLikeSubjectAndBody(RowBounds bounds,
                             @Param("subjectQuery") String subjectQuery,
                             @Param("bodyQuery") String bodyQuery);

  //======================================================

  @Select("SELECT * FROM " +
    "post WHERE id = #{id}")
  List<Post> selectPostsById(int id);

  //======================================================

  @Select("SELECT * FROM blog " +
          "WHERE id = #{id} AND title = #{nonExistentParam,jdbcType=VARCHAR}")
  Blog selectBlogByNonExistentParam(@Param("id") int id);

  @Select("SELECT * FROM blog " +
          "WHERE id = #{id} AND title = #{params.nonExistentParam,jdbcType=VARCHAR}")
  Blog selectBlogByNonExistentNestedParam(@Param("id") int id, @Param("params") Map<String, Object> params);

  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlogByNullParam(Integer id);

  //======================================================

  @Select("SELECT * FROM blog " +
      "WHERE id = #{0} AND title = #{1}")
  Blog selectBlogByDefault30ParamNames(int id, String title);

  @Select("SELECT * FROM blog " +
      "WHERE id = #{param1} AND title = #{param2}")
  Blog selectBlogByDefault31ParamNames(int id, String title);

  //======================================================

  @Select("SELECT * FROM blog " +
      "WHERE ${column} = #{id} AND title = #{value}")
  Blog selectBlogWithAParamNamedValue(@Param("column") String column, @Param("id") int id, @Param("value") String title);

  //======================================================
  
  @Select({
      "SELECT *",
      "FROM blog"
  })
  @Results({ 
      @Result(property = "author", column = "author_id", one = @One(select = "org.apache.ibatis.binding.BoundAuthorMapper.selectAuthor")), 
      @Result(property = "posts", column = "id", many = @Many(select = "selectPostsById"))
  })
  List<Blog> selectBlogsWithAutorAndPosts();

  @Select({
      "SELECT *",
      "FROM blog"
  })
  @Results({ 
      @Result(property = "author", column = "author_id", one = @One(select = "org.apache.ibatis.binding.BoundAuthorMapper.selectAuthor", fetchType=FetchType.EAGER)), 
      @Result(property = "posts", column = "id", many = @Many(select = "selectPostsById", fetchType=FetchType.EAGER))
  })
  List<Blog> selectBlogsWithAutorAndPostsEagerly();
 
}
