package org.apache.ibatis.submitted.parametrizedlist;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

public interface Mapper {

  @Select("select id, name from users")
  List<User<String>> getUsersList();

  @Select("select id, name from users")
  @MapKey("id")
  Map<Integer, User<String>> getUsersMap();

  @Select("select id, name from users where id=1")
  Map<Integer, Object> getUsersMap2();

}
