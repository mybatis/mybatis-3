/*
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

@MappedTypes(String.class)
@MappedJdbcTypes(value = { JdbcType.CHAR, JdbcType.VARCHAR }, includeNullJdbcType = true)
public class StringTrimmingTypeHandler implements TypeHandler<String> {

  @Override
  public void setParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, trim(parameter));
  }

  @Override
  public String getResult(ResultSet rs, String columnName) throws SQLException {
    return trim(rs.getString(columnName));
  }

  @Override
  public String getResult(ResultSet rs, int columnIndex) throws SQLException {
    return trim(rs.getString(columnIndex));
  }

  @Override
  public String getResult(CallableStatement cs, int columnIndex) throws SQLException {
    return trim(cs.getString(columnIndex));
  }

  private String trim(String s) {
    if (s == null) {
      return null;
    } else {
      return s.trim();
    }
  }
}
