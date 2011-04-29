package domain.blog.mappers;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.RowBounds;

public interface AuthorMapperWithRowBounds {
  @Select("select id, username, password, email, bio, favourite_section from author where id = #{id}")
  void selectAuthor(int id, RowBounds bounds1, RowBounds bounds2);

}
