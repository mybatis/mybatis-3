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
package org.apache.ibatis.builder.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

@MappedTypes(Integer.class)
public class CustomIntegerTypeHandler implements TypeHandler<Integer> {

  @Override
  public void setParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {
    ps.setInt(i, parameter);
  }

  @Override
  public Integer getResult(ResultSet rs, String columnName) throws SQLException {
    return rs.getInt(columnName);
  }

  @Override
  public Integer getResult(ResultSet rs, int columnIndex) throws SQLException {
    return rs.getInt(columnIndex);
  }

  @Override
  public Integer getResult(CallableStatement cs, int columnIndex) throws SQLException {
    return cs.getInt(columnIndex);
  }

}
