/*
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
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
public class CharacterTypeHandler extends BaseTypeHandler<Character> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Character parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter.toString());
  }

  @Override
  public Character getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String columnValue = rs.getString(columnName);
    if (columnValue != null) {
      return columnValue.charAt(0);
    } else {
      return null;
    }
  }

  @Override
  public Character getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String columnValue = rs.getString(columnIndex);
    if (columnValue != null) {
      return columnValue.charAt(0);
    } else {
      return null;
    }
  }

  @Override
  public Character getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String columnValue = cs.getString(columnIndex);
    if (columnValue != null) {
      return columnValue.charAt(0);
    } else {
      return null;
    }
  }
}
