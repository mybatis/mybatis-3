package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteTypeHandler extends BaseTypeHandler<Byte> {

  public void setNonNullParameter(PreparedStatement ps, int i, Byte parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setByte(i, parameter);
  }

  public Byte getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getByte(columnName);
  }

  public Byte getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getByte(columnIndex);
  }


}
