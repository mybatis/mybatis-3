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

package org.apache.ibatis.submitted.collection_in_constructor;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.assertj.core.util.Arrays;

public class CsvToListTypeHandler extends BaseTypeHandler<List<?>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, List<?> parameter, JdbcType jdbcType)
      throws SQLException {
    // not relevant for this test
  }

  @Override
  public List<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return stringToList(rs.getString(columnName));
  }

  @Override
  public List<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return stringToList(rs.getString(columnIndex));
  }

  @Override
  public List<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return stringToList(cs.getString(columnIndex));
  }

  private List<?> stringToList(String s) {
    if (s == null) {
      return new ArrayList<>();
    }
    return Arrays.asList(s.split(","));
  }
}
