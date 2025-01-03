/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.submitted.typebasedtypehandlerresolution;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

public interface GloballyRegisteredHandlerMapper {

  @Result(column = "id", property = "id", id = true)
  @Result(column = "strvalue", property = "strvalue")
  @Result(column = "intvalue", property = "intvalue")
  @Result(column = "strings", property = "strings")
  @Result(column = "integers", property = "integers")
  @Select("select id, strvalue, intvalue, strings, integers from users where id = #{id}")
  User getUser(Integer id);

  @Insert({ "insert into users (id, strvalue, intvalue, strings, integers)",
      "values (#{id}, #{strvalue}, #{intvalue}, #{strings}, #{integers})" })
  void insertUser(User user);

  @Insert({ "insert into users (id, strvalue, intvalue, strings, integers)",
      "values (#{user.id}, #{user.strvalue}, #{user.intvalue}, #{user.strings}, #{user.integers})" })
  void insertUserMultiParam(User user, String foo);

  @Select("select strvalue from users where id = #{id}")
  FuzzyBean<String> selectFuzzyString(Integer id);
}