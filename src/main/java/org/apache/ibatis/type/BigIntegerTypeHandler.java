package org.apache.ibatis.type;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigIntegerTypeHandler extends BaseTypeHandler {

  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    BigInteger bigint = (BigInteger) parameter;
    ps.setLong(i, bigint.longValue());
  }

  public Object getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return BigInteger.valueOf(rs.getLong(columnName));
  }

  public Object getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return BigInteger.valueOf(cs.getLong(columnIndex));
  }
}
