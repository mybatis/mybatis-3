/*
 *    Copyright 2009-2026 the original author or authors.
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

package org.apache.ibatis.submitted.null_conversion_type_handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class BloodTypeHandler implements TypeHandler<BloodType> {

  @Override
  public void setParameter(PreparedStatement ps, int i, BloodType parameter, JdbcType jdbcType) throws SQLException {
    if (parameter == null) {
      parameter = BloodType.UNKNOWN;
    }
    ps.setInt(i, parameter.getValue());
  }

  @Override
  public BloodType getResult(ResultSet rs, String columnName) throws SQLException {
    int result = rs.getInt(columnName);
    return result == 0 && rs.wasNull() ? BloodType.UNKNOWN : BloodType.of(result);
  }

  @Override
  public BloodType getResult(ResultSet rs, int columnIndex) throws SQLException {
    int result = rs.getInt(columnIndex);
    return result == 0 && rs.wasNull() ? BloodType.UNKNOWN : BloodType.of(result);
  }

  @Override
  public BloodType getResult(CallableStatement cs, int columnIndex) throws SQLException {
    int result = cs.getInt(columnIndex);
    return result == 0 && cs.wasNull() ? BloodType.UNKNOWN : BloodType.of(result);
  }
}
