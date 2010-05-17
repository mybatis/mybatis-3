package org.apache.ibatis.type;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalTypeHandler extends BaseTypeHandler {

  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setBigDecimal(i, ((BigDecimal) parameter));
  }

  public Object getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getBigDecimal(columnName);
  }

  public Object getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getBigDecimal(columnIndex);
  }


}
