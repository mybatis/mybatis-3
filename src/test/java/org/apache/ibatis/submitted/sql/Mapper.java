/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.sql;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

public interface Mapper {

  @SelectProvider(type = SqlProvider.class)
  List<User> findAll(@Param("offset") long offset, @Param("limit") int limit);

  class SqlProvider implements ProviderMethodResolver {
    public String findAll() {
      return new SQL()
        .SELECT("user_id", "name")
        .FROM("${schema}users")
        .ORDER_BY("user_id")
        .OFFSET_ROWS("#{offset}")
        .FETCH_FIRST_ROWS_ONLY("#{limit}")
        .toString();
    }
  }

}
