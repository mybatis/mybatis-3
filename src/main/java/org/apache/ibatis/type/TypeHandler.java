package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException;

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException;

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException;

}
