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

/**
 * @author Clinton Begin
 */
public class ShortTypeHandler extends BaseTypeHandler<Short> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Short parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setShort(i, parameter);
  }

  @Override
  public Short getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    short result = rs.getShort(columnName);
    return result == 0 && rs.wasNull() ? null : result;
  }

  @Override
  public Short getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    short result = rs.getShort(columnIndex);
    return result == 0 && rs.wasNull() ? null : result;
  }

  @Override
  public Short getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    short result = cs.getShort(columnIndex);
    return result == 0 && cs.wasNull() ? null : result;
  }
}
