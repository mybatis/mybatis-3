package org.apache.ibatis.submitted.parametrizedlist;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

public interface Mapper {

  @Select("select id, name from users")
  List<User<String>> getAListOfUsers();

  @Select("select id, name from users")
  @MapKey("id")
  Map<Integer, User<String>> getAMapOfUsers();

  @Select("select id, name from users where id=1")
  Map<String, Object> getUserAsAMap();

  @Select("select id, name from users")
  List<Map<String, Object>> getAListOfMaps();
  
}
