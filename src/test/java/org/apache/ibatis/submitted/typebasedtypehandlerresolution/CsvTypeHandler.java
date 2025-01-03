/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.typebasedtypehandlerresolution;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.assertj.core.util.Arrays;

public class CsvTypeHandler extends BaseTypeHandler<Object> {
  private final Type type;

  public CsvTypeHandler(Type type) {
    super();
    this.type = type;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    // test if the parameter matches 'type'
    if (parameter instanceof List) {
      Class<?> elementClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
      if (String.class.equals(elementClass)) {
        ps.setString(i, String.join(",", (List) parameter));
      } else if (Integer.class.equals(elementClass)) {
        ps.setString(i, (String) ((List) parameter).stream().map(String::valueOf).collect(Collectors.joining(",")));
      }
    }
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String str = rs.getString(columnName);
    if (str == null) {
      return null;
    }
    if (type instanceof ParameterizedType) {
      Type argType = ((ParameterizedType) type).getActualTypeArguments()[0];
      if (argType instanceof Class) {
        if (String.class.equals(argType)) {
          return Arrays.asList(str.split(","));
        } else if (Integer.class.equals(argType)) {
          return Stream.of(str.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        }
      }
    }
    return null;
  }

  @Override
  public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

}
