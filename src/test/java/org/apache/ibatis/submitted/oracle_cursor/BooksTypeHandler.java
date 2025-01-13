/*
 *    Copyright 2009-2025 the original author or authors.
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

package org.apache.ibatis.submitted.oracle_cursor;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class BooksTypeHandler extends BaseTypeHandler<List<Book>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, List<Book> parameter, JdbcType jdbcType)
      throws SQLException {
    // n/a
  }

  @Override
  public List<Book> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    List<Book> list = new ArrayList<>();
    try (ResultSet nestedCursor = rs.getObject(columnName, ResultSet.class)) {
      while (nestedCursor.next()) {
        Integer id = nestedCursor.getInt("id");
        String name = nestedCursor.getString("name");
        list.add(new Book(id, name));
      }
    }
    return list;
  }

  @Override
  public List<Book> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    // n/a
    return null;
  }

  @Override
  public List<Book> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    // n/a
    return null;
  }

}
