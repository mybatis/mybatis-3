package org.apache.ibatis.type;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigIntegerTypeHandler extends BaseTypeHandler<BigInteger> {

  public void setNonNullParameter(PreparedStatement ps, int i, BigInteger parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setLong(i, parameter.longValue());
  }

  public BigInteger getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return BigInteger.valueOf(rs.getLong(columnName));
  }

  public BigInteger getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return BigInteger.valueOf(cs.getLong(columnIndex));
  }
}
