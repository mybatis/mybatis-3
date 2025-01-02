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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;

/**
 * @author Clinton Begin
 */
public class TimeOnlyTypeHandler extends BaseTypeHandler<Date> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
    ps.setTime(i, new Time(parameter.getTime()));
  }

  @Override
  public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return toDate(rs.getTime(columnName));
  }

  @Override
  public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return toDate(rs.getTime(columnIndex));
  }

  @Override
  public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return toDate(cs.getTime(columnIndex));
  }

  private Date toDate(Time time) {
    return time == null ? null : new Date(time.getTime());
  }

}
