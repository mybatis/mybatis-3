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
package org.apache.ibatis.type;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
public class ArrayTypeHandler extends BaseTypeHandler<Object> {

  public ArrayTypeHandler() {
    super();
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    ps.setArray(i, (Array) parameter);
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return extractArray(rs.getArray(columnName));
  }

  @Override
  public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return extractArray(rs.getArray(columnIndex));
  }

  @Override
  public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return extractArray(cs.getArray(columnIndex));
  }

  protected Object extractArray(Array array) throws SQLException {
    if (array == null) {
      return null;
    }
    Object result = array.getArray();
    array.free();
    return result;
  }

}
