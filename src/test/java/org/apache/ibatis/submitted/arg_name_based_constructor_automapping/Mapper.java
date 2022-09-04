/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.arg_name_based_constructor_automapping;

import org.apache.ibatis.annotations.Select;

public interface Mapper {

  @Select("select name, id from users where id = #{id}")
  User selectNameAndId(Integer id);

  @Select("select name, id bar from users where id = #{id}")
  User selectNameAndIdWithBogusLabel(Integer id);

  @Select("select name, team, id from users where id = #{id}")
  User selectNameTeamAndId(Integer id);

  @Select("select name userName, id userId from users where id = #{id}")
  User2 selectUserIdAndUserName(Integer id);

  @Select("select name user_name, id user_id from users where id = #{id}")
  User2 selectUserIdAndUserNameUnderscore(Integer id);

  Task selectTask(Integer id);
}
