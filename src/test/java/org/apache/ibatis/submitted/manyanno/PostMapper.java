package org.apache.ibatis.submitted.manyanno;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface PostMapper {


    @Select("select * from post where author_id = #{id} order by id")
    @Results(value = {
                      @Result(property="id", column="id"),
                      @Result(property="subject", column="subject"),
                      @Result(property="body", column="body"),
                      @Result(property="tags", javaType=List.class, column="id", many=@Many(select="getTagsForPost"))
                      })
    List<AnnoPost> getPosts(int authorId);

    @Select("select t.id, t.name from tag t inner join post_tag pt on pt.tag_id = t.id where pt.post_id = #{postId} order by id")
    @ConstructorArgs(value = {
                  @Arg(column="id",javaType=int.class),
                  @Arg(column="name",javaType=String.class)
                  })
    List<AnnoPostTag> getTagsForPost(int postId);

}
