/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.overload;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.StatementId;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;

public interface BaseMapper {

  @Select("select * from users")
  List<User> select();

  @StatementId("selectById")
  User select(Integer id);

  @StatementId("selectByIdAndName")
  @SelectProvider(type = Provider.class)
  User select(@Param("id") Integer id, @Param("name") String name);

  public class Provider implements ProviderMethodResolver {
    public String selectByIdAndName(Integer id, String name) {
      return "select * from users where id = #{id} and name = #{name}";
    }
  }
}
