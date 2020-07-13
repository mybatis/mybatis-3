package org.apache.ibatis.adebug;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {
  
  @Select("select name, phone, password from user")
  List<User> selectAll();
}
