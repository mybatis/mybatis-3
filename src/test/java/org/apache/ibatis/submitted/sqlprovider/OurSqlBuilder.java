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

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

public class OurSqlBuilder {

  public String buildGetUsersQuery(Map<String, Object> parameter) {
    // MyBatis wraps a single List parameter in a Map with the key="list",
    // so need to pull it out
    @SuppressWarnings("unchecked")
    List<Integer> ids = (List<Integer>) parameter.get("list");
    StringBuilder sb = new StringBuilder();
    sb.append("select * from users where id in (");
    for (int i = 0; i < ids.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("#{list[");
      sb.append(i);
      sb.append("]}");
    }
    sb.append(") order by id");
    return sb.toString();
  }

  public String buildGetUserQuery(Number parameter) {
    // parameter is not a single List or Array,
    // so it is passed as is from the mapper
    return "select * from users where id = #{value}";
  }
  public String buildGetAllUsersQuery() {
    return "select * from users order by id";
  }

  public String buildGetUsersByCriteriaQuery(final User criteria) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (criteria.getId() != null) {
        WHERE("id = #{id}");
      }
      if (criteria.getName() != null) {
        WHERE("name like #{name} || '%'");
      }
    }}.toString();
  }

  public String buildGetUsersByCriteriaMapQuery(final Map<String, Object> criteria) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (criteria.get("id") != null) {
        WHERE("id = #{id}");
      }
      if (criteria.get("name") != null) {
        WHERE("name like #{name} || '%'");
      }
    }}.toString();
  }

  public String buildGetUsersByNameQuery(final String name, final String orderByColumn) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (name != null) {
        WHERE("name like #{param1} || '%'");
      }
      ORDER_BY(orderByColumn);
    }}.toString();
  }

  public String buildGetUsersByNameUsingMap(Map<String, Object> params) {
    final String name = String.class.cast(params.get("param1"));
    final String orderByColumn = String.class.cast(params.get("param2"));
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (name != null) {
        WHERE("name like #{param1} || '%'");
      }
      ORDER_BY(orderByColumn);
    }}.toString();
  }

  public String buildGetUsersByNameWithParamNameAndOrderByQuery(@Param("orderByColumn") final String orderByColumn, @Param("name") final String name) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (name != null) {
        WHERE("name like #{name} || '%'");
      }
      ORDER_BY(orderByColumn);
    }}.toString();
  }

  public String buildGetUsersByNameWithParamNameQuery(@Param("name") final String name) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (name != null) {
        WHERE("name like #{name} || '%'");
      }
      ORDER_BY("id DESC");
    }}.toString();
  }

  public String buildGetUsersByNameWithParamNameQueryUsingMap(Map<String, Object> params) {
    final String name = String.class.cast(params.get("name"));
    final String orderByColumn = String.class.cast(params.get("orderByColumn"));
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (name != null) {
        WHERE("name like #{param1} || '%'");
      }
      ORDER_BY(orderByColumn);
    }}.toString();
  }

  public String buildInsert() {
    return "insert into users (id, name) values (#{id}, #{name})";
  }

  public String buildUpdate() {
    return "update users set name = #{name} where id = #{id}";
  }

  public String buildDelete() {
    return "delete from users where id = #{id}";
  }

  public String buildSelectByIdProviderContextOnly(ProviderContext context) {
    final boolean containsLogicalDelete = context.getMapperMethod().getAnnotation(BaseMapper.ContainsLogicalDelete.class) != null;
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    return new SQL(){{
      SELECT("*");
      FROM(tableName);
      WHERE("id = #{id}");
      if (!containsLogicalDelete){
        WHERE("logical_delete = ${Constants.LOGICAL_DELETE_OFF}");
      }
    }}.toString();
  }

  public String buildSelectByNameOneParamAndProviderContext(ProviderContext context, final String name) {
    final boolean containsLogicalDelete = context.getMapperMethod().getAnnotation(BaseMapper.ContainsLogicalDelete.class) != null;
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    return new SQL(){{
      SELECT("*");
      FROM(tableName);
      if (name != null) {
        WHERE("name like #{name} || '%'");
      }
      if (!containsLogicalDelete){
        WHERE("logical_delete = ${LOGICAL_DELETE_OFF:0}");
      }
    }}.toString();
  }

  public String buildSelectByIdAndNameMultipleParamAndProviderContextWithAtParam(@Param("id") final Integer id, ProviderContext context, @Param("name") final String name) {
    final boolean containsLogicalDelete = context.getMapperMethod().getAnnotation(BaseMapper.ContainsLogicalDelete.class) != null;
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    return new SQL(){{
      SELECT("*");
      FROM(tableName);
      if (id != null) {
        WHERE("id = #{id}");
      }
      if (name != null) {
        WHERE("name like #{name} || '%'");
      }
      if (!containsLogicalDelete){
        WHERE("logical_delete = false");
      }
    }}.toString();
  }

  public String buildSelectByIdAndNameMultipleParamAndProviderContext(final Integer id, final String name, ProviderContext context) {
    final boolean containsLogicalDelete = context.getMapperMethod().getAnnotation(BaseMapper.ContainsLogicalDelete.class) != null;
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    return new SQL(){{
      SELECT("*");
      FROM(tableName);
      if (id != null) {
        WHERE("id = #{param1}");
      }
      if (name != null) {
        WHERE("name like #{param2} || '%'");
      }
      if (!containsLogicalDelete){
        WHERE("logical_delete = false");
      }
    }}.toString();
  }

}
