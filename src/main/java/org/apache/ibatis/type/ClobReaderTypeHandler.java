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

import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * The {@link TypeHandler} for {@link Clob}/{@link Reader} using method supported at JDBC 4.0.
 * @since 3.4.0
 * @author Kazuki Shimizu
 */
public class ClobReaderTypeHandler extends BaseTypeHandler<Reader> {

  /**
   * Set a {@link Reader} into {@link PreparedStatement}.
   * @see PreparedStatement#setClob(int, Reader)
   */
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Reader parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setClob(i, parameter);
  }

  /**
   * Get a {@link Reader} that corresponds to a specified column name from {@link ResultSet}.
   * @see ResultSet#getClob(String)
   */
  @Override
  public Reader getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return toReader(rs.getClob(columnName));
  }

  /**
   * Get a {@link Reader} that corresponds to a specified column index from {@link ResultSet}.
   * @see ResultSet#getClob(int)
   */
  @Override
  public Reader getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return toReader(rs.getClob(columnIndex));
  }

  /**
   * Get a {@link Reader} that corresponds to a specified column index from {@link CallableStatement}.
   * @see CallableStatement#getClob(int)
   */
  @Override
  public Reader getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return toReader(cs.getClob(columnIndex));
  }

  private Reader toReader(Clob clob) throws SQLException {
    if (clob == null) {
      return null;
    } else {
      return clob.getCharacterStream();
    }
  }

}
