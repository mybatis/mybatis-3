/*
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.results_id;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface Mapper {

  @Results(id = "userResult", value = {
    @Result(id = true, column = "uid", property = "id"),
    @Result(column = "name", property = "name")
  })
  @Select("select * from users where uid = #{id}")
  User getUserById(Integer id);

  @ResultMap("userResult")
  @Select("select * from users where name = #{name}")
  User getUserByName(String name);

  @Results(id = "userResultConstructor")
  @ConstructorArgs({
    @Arg(id = true, column = "uid", javaType = Integer.class),
    @Arg(column = "name", javaType = String.class)
  })
  @Select("select * from users where uid = #{id}")
  User getUserByIdConstructor(Integer id);

  @ResultMap("userResultConstructor")
  @Select("select * from users where name = #{name}")
  User getUserByNameConstructor(String name);
}
