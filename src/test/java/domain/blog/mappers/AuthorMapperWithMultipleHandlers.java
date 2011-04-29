package domain.blog.mappers;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.ResultHandler;

public interface AuthorMapperWithMultipleHandlers {
  @Select("select id, username, password, email, bio, favourite_section from author where id = #{id}")
  void selectAuthor(int id, ResultHandler handler1, ResultHandler handler2);

}
