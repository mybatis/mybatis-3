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
package org.apache.ibatis.submitted.mapper_type_parameter;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

public interface BaseMapper<S, T> {

  @SelectProvider(type = StatementProvider.class, method = "provideSelect")
  S select(S param);

  @SelectProvider(type = StatementProvider.class, method = "provideSelect")
  List<S> selectList(S param);

  @SelectProvider(type = StatementProvider.class, method = "provideSelect")
  @MapKey("id")
  Map<T, S> selectMap(S param);

  @InsertProvider(type = StatementProvider.class, method = "provideInsert")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(List<S> param);

  @UpdateProvider(type = StatementProvider.class, method = "provideUpdate")
  int update(S param);

  class StatementProvider {
    public String provideSelect(Object param) {
      StringBuilder sql = new StringBuilder("select * from ");
      if (param == null || param instanceof Person) {
        sql.append(" person ");
        if (param != null && ((Person) param).getId() != null) {
          sql.append(" where id = #{id}");
        }
      } else if (param instanceof Country) {
        sql.append(" country ");
        if (((Country) param).getId() != null) {
          sql.append(" where id = #{id}");
        }
      }
      sql.append(" order by id");
      return sql.toString();
    }

    public String provideInsert(Map<String, Object> map) {
      List<?> params = (List<?>) map.get("list");
      StringBuilder sql = null;
      for (int i = 0; i < params.size(); i++) {
        Object param = params.get(i);
        if (sql == null) {
          sql = new StringBuilder("insert into ");
          sql.append(param instanceof Country ? " country " : " person");
          sql.append(" (id, name) values ");
        } else {
          sql.append(",");
        }
        sql.append(" (#{list[").append(i).append("].id}, #{list[").append(i).append("].name})");
      }
      return sql == null ? "" : sql.toString();
    }

    public String provideUpdate(Object param) {
      StringBuilder sql = new StringBuilder("update ");
      if (param instanceof Person) {
        sql.append(" person set name = #{name} where id = #{id}");
      } else if (param instanceof Country) {
        sql.append(" country set name = #{name} where id = #{id}");
      }
      return sql.toString();
    }
  }
}
