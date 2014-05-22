/*
 *    Copyright 2014 the original author or authors.
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
package org.apache.ibatis.submitted.prefix_queries_with_statement_id;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.Map;

public interface AnnotatedMapper {
  @Select("select * from users where id = #{id}")
  User getUserUsingParameterMarker(Map<String, Object> parameterMap);

  @Select("select * from users where id = ${id}")
  User getUserUsingStringSubstitution(Map<String, Object> parameterMap);

  @SelectProvider(type = UserSqlProvider.class, method = "getUserUsingDynamicSQL")
  User getUserUsingDynamicSQL(Map<String, Object> parameterMap);

  public static class UserSqlProvider {
    public String getUserUsingDynamicSQL() {
      return "select * from users where id = #{id}";
    }
  }
}
