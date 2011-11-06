package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteArrayTypeHandler extends BaseTypeHandler<byte[]> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setBytes(i, parameter);
  }

  @Override
  public byte[] getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getBytes(columnName);
  }

  @Override
  public byte[] getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return rs.getBytes(columnIndex);
  }

  @Override
  public byte[] getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getBytes(columnIndex);
  }
}
