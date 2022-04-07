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
import java.time.OffsetTime;

/**
 * @since 3.4.5
 * @author Tomas Rohovsky
 */
public class OffsetTimeTypeHandler extends BaseTypeHandler<OffsetTime> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, OffsetTime parameter, JdbcType jdbcType)
          throws SQLException {
    ps.setObject(i, parameter);
  }

  @Override
  public OffsetTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return rs.getObject(columnName, OffsetTime.class);
  }

  @Override
  public OffsetTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return rs.getObject(columnIndex, OffsetTime.class);
  }

  @Override
  public OffsetTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return cs.getObject(columnIndex, OffsetTime.class);
  }

}
