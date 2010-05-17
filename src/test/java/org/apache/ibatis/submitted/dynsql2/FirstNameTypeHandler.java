package org.apache.ibatis.submitted.dynsql2;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;

public class FirstNameTypeHandler implements TypeHandler {

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getString(columnIndex);
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getString(columnName);
  }

  public void setParameter(PreparedStatement ps, int i, Object parameter,
                           JdbcType jdbcType) throws SQLException {
    if (parameter == null) {
      ps.setNull(i, Types.VARCHAR);
    } else {
      Name name = (Name) parameter;
      ps.setString(i, name.getFirstName());
    }
  }

}
