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
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * This type handler converts {@link java.time.OffsetDateTime} to/from {@link java.sql.Timestamp}.<br>
 * It may work with most drivers, but the offset may be lost.
 * 
 * @since 3.5.4
 */
public class LegacyOffsetDateTimeTypeHandler extends BaseTypeHandler<OffsetDateTime> {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, OffsetDateTime parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTimestamp(i, Timestamp.from(parameter.toInstant()));
  }

  @Override
  public OffsetDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnName);
    return getOffsetDateTime(timestamp);
  }

  @Override
  public OffsetDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnIndex);
    return getOffsetDateTime(timestamp);
  }

  @Override
  public OffsetDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    Timestamp timestamp = cs.getTimestamp(columnIndex);
    return getOffsetDateTime(timestamp);
  }

  private static OffsetDateTime getOffsetDateTime(Timestamp timestamp) {
    return timestamp == null ? null : OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
  }
}
