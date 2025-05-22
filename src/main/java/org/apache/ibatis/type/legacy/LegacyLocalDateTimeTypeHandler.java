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
package org.apache.ibatis.type.legacy;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * This type handler converts {@link java.time.LocalDateTime} to/from {@link java.sql.Timestamp}.<br>
 * It may work with most drivers, but non-existent date time (e.g. 2019-03-10 02:30 at America/Los_Angeles) may get shifted implicitly.
 * 
 * @since 3.5.4
 */
public class LegacyLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTimestamp(i, Timestamp.valueOf(parameter));
  }

  @Override
  public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnName);
    return getLocalDateTime(timestamp);
  }

  @Override
  public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnIndex);
    return getLocalDateTime(timestamp);
  }

  @Override
  public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    Timestamp timestamp = cs.getTimestamp(columnIndex);
    return getLocalDateTime(timestamp);
  }

  private static LocalDateTime getLocalDateTime(Timestamp date) {
    return date == null ? null : date.toLocalDateTime();
  }
}
