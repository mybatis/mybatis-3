/**
 *    Copyright 2009-2019 the original author or authors.
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
import java.time.Year;

/**
 * @since 3.4.5
 * @author Björn Raupach
 */
public class YearTypeHandler extends BaseTypeHandler<Year> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Year year, JdbcType type) throws SQLException {
    ps.setInt(i, year.getValue());
  }

  @Override
  public Year getNullableResult(ResultSet rs, String columnName) throws SQLException {
    int year = rs.getInt(columnName);
    return year == 0 && rs.wasNull() ? null : Year.of(year);
  }

  @Override
  public Year getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    int year = rs.getInt(columnIndex);
    return year == 0 && rs.wasNull() ? null : Year.of(year);
  }

  @Override
  public Year getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    int year = cs.getInt(columnIndex);
    return year == 0 && cs.wasNull() ? null : Year.of(year);
  }

}
