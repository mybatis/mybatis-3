package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler<T> {

  public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
      throws SQLException;

  public T getResult(ResultSet rs, String columnName)
      throws SQLException;

  public T getResult(CallableStatement cs, int columnIndex)
      throws SQLException;

}
