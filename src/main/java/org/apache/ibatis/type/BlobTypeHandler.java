package org.apache.ibatis.type;

import java.io.ByteArrayInputStream;
import java.sql.*;

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