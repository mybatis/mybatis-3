/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.submitted.dirty_select;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.cursor.Cursor;

public interface Mapper {

  @Select("select * from users where id = #{id}")
  User selectById(Integer id);

  @Select("select * from users where id = #{id}")
  Cursor<User> selectCursorById(Integer id);

  @Select(value = "insert into users (name) values (#{name}) returning id, name", affectData = true)
  User insertReturn(String name);

  @Select(value = "insert into users (name) values (#{name}) returning id, name", affectData = true)
  Cursor<User> insertReturnCursor(String name);

  User insertReturnXml(String name);

  @SelectProvider(type = MyProvider.class, method = "getSql", affectData = true)
  User insertReturnProvider(String name);

  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  @Insert("insert into users (name) values (#{name}) returning id, name")
  int insert(User user);

  static final class MyProvider {
    public static String getSql() {
      return "insert into users (name) values (#{name}) returning id, name";
    }

    private MyProvider() {
    }
  }
}
