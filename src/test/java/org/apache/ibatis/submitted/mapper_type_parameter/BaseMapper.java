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
package org.apache.ibatis.submitted.mapper_type_parameter;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.SelectProvider;

public interface BaseMapper<R> {

  @SelectProvider(type = StatementProvider.class, method = "provideSelect")
  R select(Integer id);

  @SelectProvider(type = StatementProvider.class, method = "provideSelect")
  List<R> selectList(Integer id);

  @SelectProvider(type = StatementProvider.class, method = "provideSelect")
  @MapKey("id")
  Map<Integer, R> selectMap(Integer id);

  public class StatementProvider {
    public String provideSelect(Integer id) {
      StringBuilder query = new StringBuilder("select * from person");
      if (id != null) {
        query.append(" where id = #{id}");
      }
      query.append(" order by id");
      return query.toString();
    }
  }

}
