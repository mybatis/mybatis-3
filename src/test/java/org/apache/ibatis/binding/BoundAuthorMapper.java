package org.apache.ibatis.binding;

import domain.blog.Author;
import domain.blog.Post;
import domain.blog.Section;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@CacheNamespace(readWrite = false)
public interface BoundAuthorMapper {

  //======================================================

  List<Post> findPostsInArray(Integer[] ids);

  //======================================================

  List<Post> findPostsInList(List<Integer> ids);

  //======================================================

  int insertAuthor(Author author);

  //======================================================

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
  Author selectAuthor(int id);

  //======================================================

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
      "  BIO as AUTHOR_BIO," +
          "  FAVOURITE_SECTION as AUTHOR_SECTION",
      "FROM AUTHOR WHERE ID = #{id}"})
  Author selectAuthorConstructor(int id);

  //======================================================

  List<Post> findThreeSpecificPosts(@Param("one") int one,
                                    RowBounds rowBounds,
                                    @Param("two") int two,
                                    int three);
}
