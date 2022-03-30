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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

/**
 * @author Clinton Begin
 */
public class SqlTimeTypeHandler extends BaseTypeHandler<Time> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Time parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTime(i, parameter);
  }

  @Override
  public Time getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getTime(columnName);
  }

  @Override
  public Time getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return rs.getTime(columnIndex);
  }

  @Override
  public Time getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getTime(columnIndex);
  }
}
