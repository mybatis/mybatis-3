/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.submitted.global_variables;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

public interface BindParameterMapper {

  List<User> getUsers();

  List<User> getUsersUsingDyna(String name);

  @Select("select * from users where status = #{userStatus.valid}")
  List<User> getUsersFromAnnotation();

  @Select("<script>select * from users <where> <if test=\"value != null\">name = #{value}</if>AND status = #{userStatus-invalid}</where></script>")
  List<User> getUsersFromAnnotationUsingDyna(String name);

  @SelectProvider(type = SqlProvider.class, method = "getUsersFromAnnotationUsingSqlProvider")
  List<User> getUsersFromAnnotationUsingSqlProvider(String name);

  class SqlProvider {
    public String getUsersFromAnnotationUsingSqlProvider() {
      return "select * from users where status = #{userStatus.valid}";
    }
  }

}
