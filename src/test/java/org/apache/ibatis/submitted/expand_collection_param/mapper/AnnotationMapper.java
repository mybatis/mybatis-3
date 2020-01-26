/**
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
package org.apache.ibatis.submitted.expand_collection_param.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.submitted.expand_collection_param.model.User;
import org.apache.ibatis.submitted.expand_collection_param.model.UserRole;
import org.apache.ibatis.submitted.expand_collection_param.model.UserStatus;

import java.util.List;

public interface AnnotationMapper {

  @Select("select * from users WHERE id in (#{ids...}) order by id")
  List<User> getUsersByIds(@Param("ids") List<Integer> ids);

  @Select("select * from users WHERE id in (#{ids...}) order by id")
  List<User> getUsersByArrayIds(@Param("ids") int[] ids);

  @Select("select * from users WHERE first_name in (#{names...}) order by id")
  List<User> getUsersByNames(@Param("names") List<String> names);

  @Select("select * from users WHERE role in (#{roles...}) order by id")
  List<User> getUsersByRoles(@Param("roles") List<UserRole> roles);

  @Select("select * from users WHERE status in (#{status...}) order by id")
  List<User> getUsersByStatus(@Param("status") List<UserStatus> status);

  @Select("select * from users WHERE status in (#{status...}) and role in (#{roles...}) order by id")
  List<User> getUsersByStatusAndRoles(@Param("status") List<UserStatus> status, @Param("roles") List<UserRole> roles);


}
