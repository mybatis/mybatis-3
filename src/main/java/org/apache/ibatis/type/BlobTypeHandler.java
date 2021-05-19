/*
 *    Copyright 2009-2015 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
public class BlobTypeHandler extends BaseTypeHandler<byte[]> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, JdbcType jdbcType)
      throws SQLException {
    ByteArrayInputStream bis = new ByteArrayInputStream(parameter);
    ps.setBinaryStream(i, bis, parameter.length);
  }

  @Override
  public byte[] getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    Blob blob = rs.getBlob(columnName);
    byte[] returnValue = null;
    if (null != blob) {
      returnValue = blob.getBytes(1, (int) blob.length());
    }
    return returnValue;
  }

  @Override
  public byte[] getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Blob blob = rs.getBlob(columnIndex);
    byte[] returnValue = null;
    if (null != blob) {
      returnValue = blob.getBytes(1, (int) blob.length());
    }
    return returnValue;
  }

  @Override
  public byte[] getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Blob blob = cs.getBlob(columnIndex);
    byte[] returnValue = null;
    if (null != blob) {
      returnValue = blob.getBytes(1, (int) blob.length());
    }
    return returnValue;
  }
}