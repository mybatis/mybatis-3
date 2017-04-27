/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.io.Resources;

/**
 * @author Clinton Begin
 */
public class UnknownTypeHandler extends BaseTypeHandler<Object> {

  private static final ObjectTypeHandler OBJECT_TYPE_HANDLER = new ObjectTypeHandler();

  private TypeHandlerRegistry typeHandlerRegistry;

  public UnknownTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
    this.typeHandlerRegistry = typeHandlerRegistry;
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    TypeHandler handler = resolveTypeHandler(parameter, jdbcType);
    handler.setParameter(ps, i, parameter, jdbcType);
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    TypeHandler<?> handler = resolveTypeHandler(rs, columnName);
    return handler.getResult(rs, columnName);
  }

  @Override
  public Object getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    TypeHandler<?> handler = resolveTypeHandler(rs.getMetaData(), columnIndex);
    if (handler == null || handler instanceof UnknownTypeHandler) {
      handler = OBJECT_TYPE_HANDLER;
    }
    return handler.getResult(rs, columnIndex);
  }

  @Override
  public Object getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getObject(columnIndex);
  }

  private TypeHandler<? extends Object> resolveTypeHandler(Object parameter, JdbcType jdbcType) {
    TypeHandler<? extends Object> handler;
    if (parameter == null) {
      handler = OBJECT_TYPE_HANDLER;
    } else {
      handler = typeHandlerRegistry.getTypeHandler(parameter.getClass(), jdbcType);
      // check if handler is null (issue #270)
      if (handler == null || handler instanceof UnknownTypeHandler) {
        handler = OBJECT_TYPE_HANDLER;
      }
    }
    return handler;
  }

  private TypeHandler<?> resolveTypeHandler(ResultSet rs, String column) {
    try {
      Map<String,Integer> columnIndexLookup;
      columnIndexLookup = new HashMap<String,Integer>();
      ResultSetMetaData rsmd = rs.getMetaData();
      int count = rsmd.getColumnCount();
      for (int i=1; i <= count; i++) {
        String name = rsmd.getColumnName(i);
        columnIndexLookup.put(name,i);
      }
      Integer columnIndex = columnIndexLookup.get(column);
      TypeHandler<?> handler = null;
      if (columnIndex != null) {
        handler = resolveTypeHandler(rsmd, columnIndex);
      }
      if (handler == null || handler instanceof UnknownTypeHandler) {
        handler = OBJECT_TYPE_HANDLER;
      }
      return handler;
    } catch (SQLException e) {
      throw new TypeException("Error determining JDBC type for column " + column + ".  Cause: " + e, e);
    }
  }

  private TypeHandler<?> resolveTypeHandler(ResultSetMetaData rsmd, Integer columnIndex) throws SQLException {
    TypeHandler<?> handler = null;
    JdbcType jdbcType = safeGetJdbcTypeForColumn(rsmd, columnIndex);
    Class<?> javaType = safeGetClassForColumn(rsmd, columnIndex);
    if (javaType != null && jdbcType != null) {
      handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
    } else if (javaType != null) {
      handler = typeHandlerRegistry.getTypeHandler(javaType);
    } else if (jdbcType != null) {
      handler = typeHandlerRegistry.getTypeHandler(jdbcType);
    }
    return handler;
  }

  private JdbcType safeGetJdbcTypeForColumn(ResultSetMetaData rsmd, Integer columnIndex) {
    try {
      return JdbcType.forCode(rsmd.getColumnType(columnIndex));
    } catch (Exception e) {
      return null;
    }
  }

  private Class<?> safeGetClassForColumn(ResultSetMetaData rsmd, Integer columnIndex) {
    try {
      return Resources.classForName(rsmd.getColumnClassName(columnIndex));
    } catch (Exception e) {
      return null;
    }
  }
}
