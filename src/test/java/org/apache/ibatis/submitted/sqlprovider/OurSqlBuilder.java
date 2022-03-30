/*
 *    Copyright 2009-2022 the original author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

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
    return new SQL() {
      {
        SELECT("*");
        FROM("users");
        if (criteria.getId() != null) {
          WHERE("id = #{id}");
        }
        if (criteria.getName() != null) {
          WHERE("name like #{name} || '%'");
        }
      }
    }.toString();
  }

  public String buildGetUsersByCriteriaMapQuery(final Map<String, Object> criteria) {
    return new SQL() {
      {
        SELECT("*");
        FROM("users");
        if (criteria.get("id") != null) {
          WHERE("id = #{id}");
        }
        if (criteria.get("name") != null) {
          WHERE("name like #{name} || '%'");
        }
      }
    }.toString();
  }

  public String buildGetUsersByCriteriaMapWithParamQuery(@Param("id") Integer id, @Param("name") String name) {
    return new SQL() {
      {
        SELECT("*");
        FROM("users");
        if (id != null) {
          WHERE("id = #{id}");
        }
        if (name != null) {
          WHERE("name like #{name} || '%'");
        }
      }
    }.toString();
  }

  public String buildGetUsersByNameQuery(final String name, final String orderByColumn) {
    return new SQL() {
      {
        SELECT("*");
        FROM("users");
        if (name != null) {
          WHERE("name like #{param1} || '%'");
        }
        ORDER_BY(orderByColumn);
      }
    }.toString();
  }

  public String buildGetUsersByNameUsingMap(Map<String, Object> params) {
    final String name = String.class.cast(params.get("param1"));
    final String orderByColumn = String.class.cast(params.get("param2"));
    return new SQL() {
      {
        SELECT("*");
        FROM("users");
        if (name != null) {
          WHERE("name like #{param1} || '%'");
        }
        ORDER_BY(orderByColumn);
      }
    }.toString();
  }

  public String buildGetUsersByNameWithParamNameAndOrderByQuery(@Param("orderByColumn") final String orderByColumn,
      @Param("name") final String name) {
    return new SQL() {
      {
        SELECT("*");
        FROM("users");
        if (name != null) {
          WHERE("name like #{name} || '%'");
        }
        ORDER_BY(orderByColumn);
      }
    }.toString();
  }

  public String buildGetUsersByNameWithParamNameQuery(@Param("name") final String name) {
    return new SQL() {
      {
        SELECT("*");
        FROM("users");
        if (name != null) {
          WHERE("name like #{name} || '%'");
        }
        ORDER_BY("id DESC");
      }
    }.toString();
  }

  public String buildGetUsersByNameWithParamNameQueryUsingMap(Map<String, Object> params) {
    final String name = String.class.cast(params.get("name"));
    final String orderByColumn = String.class.cast(params.get("orderByColumn"));
    return new SQL() {
      {
        SELECT("*");
        FROM("users");
        if (name != null) {
          WHERE("name like #{param1} || '%'");
        }
        ORDER_BY(orderByColumn);
      }
    }.toString();
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
    final boolean containsLogicalDelete = context.getMapperMethod()
        .getAnnotation(BaseMapper.ContainsLogicalDelete.class) != null;
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    return new SQL() {
      {
        SELECT("*");
        FROM(tableName);
        WHERE("id = #{id}");
        if (!containsLogicalDelete) {
          WHERE("logical_delete = ${Constants.LOGICAL_DELETE_OFF}");
        }
      }
    }.toString();
  }

  public String buildSelectByNameOneParamAndProviderContext(ProviderContext context, final String name) {
    final boolean containsLogicalDelete = context.getMapperMethod()
        .getAnnotation(BaseMapper.ContainsLogicalDelete.class) != null;
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    return new SQL() {
      {
        SELECT("*");
        FROM(tableName);
        if (name != null) {
          WHERE("name like #{name} || '%'");
        }
        if (!containsLogicalDelete) {
          WHERE("logical_delete = ${LOGICAL_DELETE_OFF:0}");
        }
      }
    }.toString();
  }

  public String buildSelectByIdAndNameMultipleParamAndProviderContextWithAtParam(@Param("id") final Integer id,
      ProviderContext context, @Param("name") final String name) {
    final boolean containsLogicalDelete = context.getMapperMethod()
        .getAnnotation(BaseMapper.ContainsLogicalDelete.class) != null;
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    return new SQL() {
      {
        SELECT("*");
        FROM(tableName);
        if (id != null) {
          WHERE("id = #{id}");
        }
        if (name != null) {
          WHERE("name like #{name} || '%'");
        }
        if (!containsLogicalDelete) {
          WHERE("logical_delete = false");
        }
      }
    }.toString();
  }

  public String buildSelectByIdAndNameMultipleParamAndProviderContext(final Integer id, final String name,
      ProviderContext context) {
    final boolean containsLogicalDelete = context.getMapperMethod()
        .getAnnotation(BaseMapper.ContainsLogicalDelete.class) != null;
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    return new SQL() {
      {
        SELECT("*");
        FROM(tableName);
        if (id != null) {
          WHERE("id = #{param1}");
        }
        if (name != null) {
          WHERE("name like #{param2} || '%'");
        }
        if (!containsLogicalDelete) {
          WHERE("logical_delete = false");
        }
      }
    }.toString();
  }

  private Class<?> getEntityClass(ProviderContext providerContext) {
    Method mapperMethod = providerContext.getMapperMethod();
    Class<?> declaringClass = mapperMethod.getDeclaringClass();
    Class<?> mapperClass = providerContext.getMapperType();

    Type[] types = mapperClass.getGenericInterfaces();
    for (Type type : types) {
      if (type instanceof ParameterizedType) {
        ParameterizedType t = (ParameterizedType) type;
        if (t.getRawType() == declaringClass || mapperClass.isAssignableFrom((Class<?>) t.getRawType())) {
          Class<?> returnType = (Class<?>) t.getActualTypeArguments()[0];
          return returnType;
        }
      }
    }
    throw new RuntimeException("The interface [" + mapperClass.getCanonicalName() + "] must specify a generic type.");
  }

  private Map<String, String> getColumnMap(ProviderContext context) {
    Class<?> entityClass = getEntityClass(context);
    Field[] fields = entityClass.getDeclaredFields();
    Map<String, String> columnMap = new LinkedHashMap<String, String>();
    for (Field field : fields) {
      BaseMapper.Column column = field.getAnnotation(BaseMapper.Column.class);
      if (column != null) {
        String columnName = column.value();
        if (columnName == null || columnName.length() == 0) {
          columnName = field.getName();
        }
        columnMap.put(columnName, field.getName());
      }
    }
    if (columnMap.size() == 0) {
      throw new RuntimeException("There is no field in the class [" + entityClass.getCanonicalName()
          + "] that specifies the @BaseMapper.Column annotation.");
    }
    return columnMap;
  }

  public String buildInsertSelective(ProviderContext context) {
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    Map<String, String> columnMap = getColumnMap(context);
    StringBuilder sqlBuffer = new StringBuilder();
    sqlBuffer.append("<script>");
    sqlBuffer.append("insert into ");
    sqlBuffer.append(tableName);
    sqlBuffer.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
    for (Map.Entry<String, String> entry : columnMap.entrySet()) {
      sqlBuffer.append("<if test=\"").append(entry.getValue()).append(" != null\">");
      sqlBuffer.append(entry.getKey()).append(",");
      sqlBuffer.append("</if>");
    }
    sqlBuffer.append("</trim>");
    sqlBuffer.append("<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
    for (String field : columnMap.values()) {
      sqlBuffer.append("<if test=\"").append(field).append(" != null\">");
      sqlBuffer.append("#{").append(field).append("} ,");
      sqlBuffer.append("</if>");
    }
    sqlBuffer.append("</trim>");
    sqlBuffer.append("</script>");
    return sqlBuffer.toString();
  }

  public String buildUpdateSelective(ProviderContext context) {
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    Map<String, String> columnMap = getColumnMap(context);
    StringBuilder sqlBuffer = new StringBuilder();
    sqlBuffer.append("<script>");
    sqlBuffer.append("update ");
    sqlBuffer.append(tableName);
    sqlBuffer.append("<set>");
    for (Map.Entry<String, String> entry : columnMap.entrySet()) {
      sqlBuffer.append("<if test=\"").append(entry.getValue()).append(" != null\">");
      sqlBuffer.append(entry.getKey()).append(" = #{").append(entry.getValue()).append("} ,");
      sqlBuffer.append("</if>");
    }
    sqlBuffer.append("</set>");
    // For simplicity, there is no @Id annotation here, using default id directly
    sqlBuffer.append("where id = #{id}");
    sqlBuffer.append("</script>");
    return sqlBuffer.toString();
  }

  public String buildGetByEntityQuery(ProviderContext context) {
    final String tableName = context.getMapperType().getAnnotation(BaseMapper.Meta.class).tableName();
    Map<String, String> columnMap = getColumnMap(context);
    StringBuilder sqlBuffer = new StringBuilder();
    sqlBuffer.append("<script>");
    sqlBuffer.append("select * from ");
    sqlBuffer.append(tableName);
    sqlBuffer.append("<where>");
    for (Map.Entry<String, String> entry : columnMap.entrySet()) {
      sqlBuffer.append("<if test=\"").append(entry.getValue()).append(" != null\">");
      sqlBuffer.append("and ").append(entry.getKey()).append(" = #{").append(entry.getValue()).append("}");
      sqlBuffer.append("</if>");
    }
    sqlBuffer.append("</where>");
    sqlBuffer.append("</script>");
    return sqlBuffer.toString();
  }

}
