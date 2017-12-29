/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.sqlprovider;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

@BaseMapper.Meta(tableName = "users")
public interface Mapper extends BaseMapper<User> {
  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetUsersQuery")
  List<User> getUsers(List<Integer> allFilterIds);

  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetUserQuery")
  User getUser(Integer userId);
 
  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetAllUsersQuery")
  List<User> getAllUsers();

  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetUsersByCriteriaQuery")
  List<User> getUsersByCriteria(User criteria);

  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetUsersByCriteriaMapQuery")
  List<User> getUsersByCriteriaMap(Map<String, Object> criteria);

  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetUsersByNameQuery")
  List<User> getUsersByName(String name, String orderByColumn);

  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetUsersByNameUsingMap")
  List<User> getUsersByNameUsingMap(String name, String orderByColumn);

  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetUsersByNameWithParamNameAndOrderByQuery")
  List<User> getUsersByNameWithParamNameAndOrderBy(@Param("name") String name, @Param("orderByColumn") String orderByColumn);

  @SelectProvider(type = OurSqlBuilder.class, method = "buildGetUsersByNameWithParamNameQuery")
  List<User> getUsersByNameWithParamName(@Param("name") String name);

  @InsertProvider(type = OurSqlBuilder.class, method = "buildInsert")
  void insert(User user);

  @UpdateProvider(type= OurSqlBuilder.class, method= "buildUpdate")
  void update(User user);

  @DeleteProvider(type= OurSqlBuilder.class, method= "buildDelete")
  void delete(Integer id);

}
