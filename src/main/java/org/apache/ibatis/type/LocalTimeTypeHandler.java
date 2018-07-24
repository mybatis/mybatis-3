/**
 *    Copyright 2009-2018 the original author or authors.
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
import java.time.LocalTime;

/**
 * @since 3.4.5
 * @author Tomas Rohovsky
 */
public class LocalTimeTypeHandler extends BaseTypeHandler<LocalTime> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, LocalTime parameter, JdbcType jdbcType)
          throws SQLException {
    ps.setTime(i, Time.valueOf(parameter));
  }

  @Override
  public LocalTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Time time = rs.getTime(columnName);
    return getLocalTime(time);
  }

  @Override
  public LocalTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    Time time = rs.getTime(columnIndex);
    return getLocalTime(time);
  }

  @Override
  public LocalTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    Time time = cs.getTime(columnIndex);
    return getLocalTime(time);
  }

  private static LocalTime getLocalTime(Time time) {
    if (time != null) {
      return time.toLocalTime();
    }
    return null;
  }
}
