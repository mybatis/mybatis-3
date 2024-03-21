/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.binding;

import java.util.List;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Post;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.RowBounds;

@CacheNamespace(readWrite = false)
public interface BoundAuthorMapper {

  // ======================================================

  List<Post> findPostsInArray(Integer[] ids);

  // ======================================================

  List<Post> findPostsInList(List<Integer> ids);

  // ======================================================

  int insertAuthor(Author author);

  int insertAuthorInvalidSelectKey(Author author);

  int insertAuthorInvalidInsert(Author author);

  int insertAuthorDynamic(Author author);

  // ======================================================

  // @formatter:off
  @ConstructorArgs({
      @Arg(column = "AUTHOR_ID", javaType = int.class)
  })
  @Results({
      @Result(property = "username", column = "AUTHOR_USERNAME"),
      @Result(property = "password", column = "AUTHOR_PASSWORD"),
      @Result(property = "email", column = "AUTHOR_EMAIL"),
      @Result(property = "bio", column = "AUTHOR_BIO")
  })
  @Select({
      "SELECT ",
      "  ID as AUTHOR_ID,",
      "  USERNAME as AUTHOR_USERNAME,",
      "  PASSWORD as AUTHOR_PASSWORD,",
      "  EMAIL as AUTHOR_EMAIL,",
      "  BIO as AUTHOR_BIO",
      "FROM AUTHOR WHERE ID = #{id}"})
  // @formatter:on
  Author selectAuthor(int id);

  // ======================================================

  @Result(property = "id", column = "AUTHOR_ID", id = true)
  @Result(property = "username", column = "AUTHOR_USERNAME")
  @Result(property = "password", column = "AUTHOR_PASSWORD")
  @Result(property = "email", column = "AUTHOR_EMAIL")
  @Result(property = "bio", column = "AUTHOR_BIO")
  // @formatter:off
  @Select({
    "SELECT ",
    "  ID as AUTHOR_ID,",
    "  USERNAME as AUTHOR_USERNAME,",
    "  PASSWORD as AUTHOR_PASSWORD,",
    "  EMAIL as AUTHOR_EMAIL,",
    "  BIO as AUTHOR_BIO",
    "FROM AUTHOR WHERE ID = #{id}"})
  // @formatter:on
  Author selectAuthorMapToPropertiesUsingRepeatable(int id);

  // ======================================================

  // @formatter:off
  @ConstructorArgs({
      @Arg(column = "AUTHOR_ID", javaType = Integer.class),
      @Arg(column = "AUTHOR_USERNAME", javaType = String.class),
      @Arg(column = "AUTHOR_PASSWORD", javaType = String.class),
      @Arg(column = "AUTHOR_EMAIL", javaType = String.class),
      @Arg(column = "AUTHOR_BIO", javaType = String.class),
      @Arg(column = "AUTHOR_SECTION", javaType = Section.class)
  })
  @Select({
      "SELECT ",
      "  ID as AUTHOR_ID,",
      "  USERNAME as AUTHOR_USERNAME,",
      "  PASSWORD as AUTHOR_PASSWORD,",
      "  EMAIL as AUTHOR_EMAIL,",
      "  BIO as AUTHOR_BIO,"
          + "  FAVOURITE_SECTION as AUTHOR_SECTION",
      "FROM AUTHOR WHERE ID = #{id}"})
  // @formatter:on
  Author selectAuthorConstructor(int id);

  // ======================================================

  @Arg(column = "AUTHOR_ID", javaType = Integer.class, id = true)
  @Arg(column = "AUTHOR_USERNAME", javaType = String.class)
  @Arg(column = "AUTHOR_PASSWORD", javaType = String.class)
  @Arg(column = "AUTHOR_EMAIL", javaType = String.class)
  @Arg(column = "AUTHOR_BIO", javaType = String.class)
  @Arg(column = "AUTHOR_SECTION", javaType = Section.class)
  // @formatter:off
  @Select({
    "SELECT ",
    "  ID as AUTHOR_ID,",
    "  USERNAME as AUTHOR_USERNAME,",
    "  PASSWORD as AUTHOR_PASSWORD,",
    "  EMAIL as AUTHOR_EMAIL,",
    "  BIO as AUTHOR_BIO,"
      + "  FAVOURITE_SECTION as AUTHOR_SECTION",
    "FROM AUTHOR WHERE ID = #{id}"})
  // @formatter:on
  Author selectAuthorMapToConstructorUsingRepeatable(int id);

  // ======================================================

  @Arg(column = "AUTHOR_ID", javaType = int.class)
  @Result(property = "username", column = "AUTHOR_USERNAME")
  // @formatter:off
  @Select({
    "SELECT ",
    "  ID as AUTHOR_ID,",
    "  USERNAME as AUTHOR_USERNAME,",
    "  PASSWORD as AUTHOR_PASSWORD,",
    "  EMAIL as AUTHOR_EMAIL,",
    "  BIO as AUTHOR_BIO",
    "FROM AUTHOR WHERE ID = #{id}"})
  // @formatter:on
  Author selectAuthorUsingSingleRepeatable(int id);

  // ======================================================

  // @formatter:off
  @ConstructorArgs({
    @Arg(column = "AUTHOR_ID", javaType = Integer.class),
    @Arg(column = "AUTHOR_USERNAME", javaType = String.class),
    @Arg(column = "AUTHOR_PASSWORD", javaType = String.class),
    @Arg(column = "AUTHOR_EMAIL", javaType = String.class),
    @Arg(column = "AUTHOR_BIO", javaType = String.class)
  })
  @Arg(column = "AUTHOR_SECTION", javaType = Section.class)
  @Select({
    "SELECT ",
    "  ID as AUTHOR_ID,",
    "  USERNAME as AUTHOR_USERNAME,",
    "  PASSWORD as AUTHOR_PASSWORD,",
    "  EMAIL as AUTHOR_EMAIL,",
    "  BIO as AUTHOR_BIO,"
      + "  FAVOURITE_SECTION as AUTHOR_SECTION",
    "FROM AUTHOR WHERE ID = #{id}"})
  // @formatter:on
  Author selectAuthorUsingBothArgAndConstructorArgs(int id);

  // ======================================================

  // @formatter:off
  @Results(
    @Result(property = "id", column = "AUTHOR_ID")
  )
  @Result(property = "username", column = "AUTHOR_USERNAME")
  @Select({
    "SELECT ",
    "  ID as AUTHOR_ID,",
    "  USERNAME as AUTHOR_USERNAME",
    "FROM AUTHOR WHERE ID = #{id}"})
  // @formatter:on
  Author selectAuthorUsingBothResultAndResults(int id);

  // ======================================================

  List<Post> findThreeSpecificPosts(@Param("one") int one, RowBounds rowBounds, @Param("two") int two, int three);

  @Flush
  List<BatchResult> flush();

}
