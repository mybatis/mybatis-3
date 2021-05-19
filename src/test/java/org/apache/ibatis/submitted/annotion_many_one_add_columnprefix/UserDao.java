/*
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.annotion_many_one_add_columnprefix;

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
  @Select({ "select",
    "     u.id, u.username, r.id role_id, r.name role_name",
    "    from user u",
    "    left join user_role ur on u.id = ur.user_id",
    "    left join role r on ur.role_id = r.id" })
  @Results({
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(property = "roles", many = @Many(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.RoleDao.roleMap1", columnPrefix = "role_"))
  })
  List<User> findAll();

  @Select({ "select",
    "     u.id, u.username, r.id role_id, r.name role_name",
    "    from user u",
    "    left join user_role ur on u.id = ur.user_id",
    "    left join role r on ur.role_id = r.id" })
  @Results({
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(property = "roles", many = @Many(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.RoleDao.roleMap2", columnPrefix = "role_"))
  })
  List<User> findAll2();

  @Select({ "select",
    "     u.id, u.username, r.id role_id, r.name role_name",
    "    from user u",
    "    left join user_role ur on u.id = ur.user_id",
    "    left join role r on ur.role_id = r.id where u.id in (2, 3)" })
  @Results({
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(property = "role", one = @One(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.RoleDao.roleMap2", columnPrefix = "role_"))
  })
  List<User> findAll3();

  @Select("select id teacher_id, username teacher_name from user")
  @Results(id = "userMap", value = {
    @Result(id = true, column = "teacher_id", property = "id"),
    @Result(column = "teacher_name", property = "username")
  })
  List<User> justUseResult();

  @Select({ "select",
    "     u.id, u.username, r.id role_id, r.name role_name,",
    "     f.id friend_id, f.username, fr.id friend_role_id, fr.name friend_role_name",
    "    from user u",
    "    left join user_role ur on u.id = ur.user_id",
    "    left join role r on ur.role_id = r.id" ,
    "    left join user f on u.friend_id = f.id",
    "    left join user_role fur on f.id = fur.user_id",
    "    left join role fr on fur.role_id = fr.id" ,
    "    where u.id = #{userId} order by r.id, fr.id"
    })
  @Results(id = "userWithFriendMap", value = {
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(property = "roles", many = @Many(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.RoleDao.roleMap1", columnPrefix = "role_")),
    @Result(property = "friend", one = @One(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.UserDao.userWithFriendMap", columnPrefix = "friend_"))
  })
  User findUserWithFriend(Integer userId);
}
