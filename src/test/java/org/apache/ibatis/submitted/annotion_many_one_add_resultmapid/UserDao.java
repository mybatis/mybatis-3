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
package org.apache.ibatis.submitted.annotion_many_one_add_resultmapid;

import java.util.List;

import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * @author lvyang
 */
public interface UserDao {
  // @formatter:off
  @Select({"select",
      "     u.id, u.username, r.id role_id, r.role_name",
      "    from user u",
      "    left join user_role ur on u.id = ur.user_id",
      "    left join role r on ur.role_id = r.id"})
  @Results({
      @Result(id = true, column = "id", property = "id"),
      @Result(column = "username", property = "username"),
      @Result(property = "roles", many = @Many(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_resultmapid.RoleDao.roleMap1"))
    })
  // @formatter:on
  List<User> findAll();

  // @formatter:off
  @Select({"select",
      "     u.id, u.username, r.id role_id, r.role_name",
      "    from user u",
      "    left join user_role ur on u.id = ur.user_id",
      "    left join role r on ur.role_id = r.id"})
  @Results({
      @Result(id = true, column = "id", property = "id"),
      @Result(column = "username", property = "username"),
      @Result(property = "roles", many = @Many(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_resultmapid.RoleDao.roleMap2"))
    })
  // @formatter:on
  List<User> findAll2();

  // @formatter:off
  @Select({"select",
      "     u.id, u.username, r.id role_id, r.role_name",
      "    from user u",
      "    left join user_role ur on u.id = ur.user_id",
      "    left join role r on ur.role_id = r.id where u.id in (2, 3)"})
  @Results({
      @Result(id = true, column = "id", property = "id"),
      @Result(column = "username", property = "username"),
      @Result(property = "role", one = @One(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_resultmapid.RoleDao.roleMap2"))
    })
  // @formatter:on
  List<User> findAll3();

  @Select("select id teacher_id, username teacher_name from user")
  // @formatter:off
  @Results(id = "userMap", value = {
      @Result(id = true, column = "teacher_id", property = "id"),
      @Result(column = "teacher_name", property = "username")
    })
  // @formatter:on
  List<User> justUseResult();

  // @formatter:off
  @Select({"select",
      "u.id, u.username, r.id role_id, r.role_name, ut.id teacher_id, ut.username teacher_name",
      "from user u",
      "left join user_role ur on u.id = ur.user_id",
      "left join role r on ur.role_id = r.id",
      "left join user ut on ut.id != u.id",
      "where role_id = 3"})
  @Results({
      @Result(id = true, column = "id", property = "id"),
      @Result(column = "username", property = "username"),
      @Result(property = "role", one = @One(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_resultmapid.RoleDao.roleMap2")),
      @Result(property = "teachers", many = @Many(resultMap = "userMap"))
    })
  // @formatter:on
  User findHeadmaster();
}
