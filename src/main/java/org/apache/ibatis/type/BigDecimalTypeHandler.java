package org.apache.ibatis.type;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalTypeHandler extends BaseTypeHandler<BigDecimal> {

  public void setNonNullParameter(PreparedStatement ps, int i, BigDecimal parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setBigDecimal(i, parameter);
  }

  public BigDecimal getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getBigDecimal(columnName);
  }

  public BigDecimal getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getBigDecimal(columnIndex);
  }


}
