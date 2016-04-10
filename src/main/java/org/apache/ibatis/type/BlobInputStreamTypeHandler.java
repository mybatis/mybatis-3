/**
 *    Copyright 2009-2016 the original author or authors.
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

import java.io.InputStream;
import java.sql.*;

/**
 * The {@link TypeHandler} for {@link Blob}/{@link InputStream} using method supported at JDBC 4.0.
 * @since 3.4.0
 * @author Kazuki Shimizu
 */
public class BlobInputStreamTypeHandler extends BaseTypeHandler<InputStream> {

  /**
   * Set an {@link InputStream} into {@link PreparedStatement}.
   * @see PreparedStatement#setBlob(int, InputStream)
   */
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, InputStream parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setBlob(i, parameter);
  }

  /**
   * Get an {@link InputStream} that corresponds to a specified column name from {@link ResultSet}.
   * @see ResultSet#getBlob(String)
   */
  @Override
  public InputStream getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return toInputStream(rs.getBlob(columnName));
  }

  /**
   * Get an {@link InputStream} that corresponds to a specified column index from {@link ResultSet}.
   * @see ResultSet#getBlob(int)
   */
  @Override
  public InputStream getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return toInputStream(rs.getBlob(columnIndex));
  }

  /**
   * Get an {@link InputStream} that corresponds to a specified column index from {@link CallableStatement}.
   * @see CallableStatement#getBlob(int)
   */
  @Override
  public InputStream getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return toInputStream(cs.getBlob(columnIndex));
  }

  private InputStream toInputStream(Blob blob) throws SQLException {
    if (blob == null) {
      return null;
    } else {
      return blob.getBinaryStream();
    }
  }

}
