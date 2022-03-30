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
package org.apache.ibatis.submitted.enum_interface_type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(HasValue.class)
public class HasValueEnumTypeHandler<E extends Enum<E> & HasValue> extends
    BaseTypeHandler<E> {
  private Class<E> type;
  private final E[] enums;

  public HasValueEnumTypeHandler(Class<E> type) {
    if (type == null)
      throw new IllegalArgumentException("Type argument cannot be null");
    this.type = type;
    this.enums = type.getEnumConstants();
    if (!type.isInterface() && this.enums == null)
      throw new IllegalArgumentException(type.getSimpleName()
          + " does not represent an enum type.");
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, E parameter,
      JdbcType jdbcType) throws SQLException {
    ps.setInt(i, parameter.getValue());
  }

  @Override
  public E getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    int value = rs.getInt(columnName);
    if (rs.wasNull()) {
      return null;
    }
    for (E enm : enums) {
      if (value == enm.getValue()) {
        return enm;
      }
    }
    throw new IllegalArgumentException("Cannot convert "
        + value + " to " + type.getSimpleName());
  }

  @Override
  public E getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    int value = rs.getInt(columnIndex);
    if (rs.wasNull()) {
      return null;
    }
    for (E enm : enums) {
      if (value == enm.getValue()) {
        return enm;
      }
    }
    throw new IllegalArgumentException("Cannot convert "
        + value + " to " + type.getSimpleName());
  }

  @Override
  public E getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    int value = cs.getInt(columnIndex);
    if (cs.wasNull()) {
      return null;
    }
    for (E enm : enums) {
      if (value == enm.getValue()) {
        return enm;
      }
    }
    throw new IllegalArgumentException("Cannot convert "
        + value + " to " + type.getSimpleName());
  }
}
