/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.submitted.enum_with_method;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class MoodTypeTypeHandler extends BaseTypeHandler<Mood> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Mood parameter, JdbcType jdbcType) throws SQLException {
    ps.setInt(i, parameter.getValue());
  }

  @Override
  public Mood getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return Mood.fromValue(rs.getInt(columnName));
  }

  @Override
  public Mood getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return Mood.fromValue(rs.getInt(columnIndex));
  }

  @Override
  public Mood getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return Mood.fromValue(cs.getInt(columnIndex));
  }

}
