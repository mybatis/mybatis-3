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
package org.apache.ibatis.submitted.repeatable;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

public interface BothSelectContainerAndSelectProviderContainerMapper {

  @Select(value = "SELECT * FROM users WHERE id = #{id}", databaseId = "hsql")
  @Select("SELECT * FROM users WHERE id = #{id}")
  @SelectProvider(type = SqlProvider.class, method = "getUser", databaseId = "hsql")
  @SelectProvider(type = SqlProvider.class, method = "getUser")
  User getUser(Integer id);

  class SqlProvider {
    public static String getUser() {
      return "SELECT * FROM users WHERE id = #{id}";
    }
  }

}
