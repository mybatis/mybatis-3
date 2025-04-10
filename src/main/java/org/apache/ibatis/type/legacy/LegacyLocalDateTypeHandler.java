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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * This type handler converts {@link java.time.LocalDate} to/from {@link java.sql.Date}.<br>
 * It may work with most drivers.
 * 
 * @since 3.5.4
 */
public class LegacyLocalDateTypeHandler extends BaseTypeHandler<LocalDate> {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setDate(i, Date.valueOf(parameter));
  }

  @Override
  public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Date date = rs.getDate(columnName);
    return getLocalDate(date);
  }

  @Override
  public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    Date date = rs.getDate(columnIndex);
    return getLocalDate(date);
  }

  @Override
  public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    Date date = cs.getDate(columnIndex);
    return getLocalDate(date);
  }

  private static LocalDate getLocalDate(Date date) {
    return date == null ? null : date.toLocalDate();
  }
}
