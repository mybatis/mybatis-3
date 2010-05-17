package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    if (parameter == null) {
      if (jdbcType == null) {
        try {
          ps.setNull(i, JdbcType.OTHER.TYPE_CODE);
        } catch (SQLException e) {
          throw new TypeException("Error setting null parameter.  Most JDBC drivers require that the JdbcType must be specified for all nullable parameters. Cause: " + e, e);
        }
      } else {
        ps.setNull(i, jdbcType.TYPE_CODE);
      }
    } else {
      setNonNullParameter(ps, i, parameter, jdbcType);
    }
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    Object result = getNullableResult(rs, columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return result;
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Object result = getNullableResult(cs, columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return result;
    }
  }

  public abstract void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException;

  public abstract Object getNullableResult(ResultSet rs, String columnName)
      throws SQLException;

  public abstract Object getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException;

}

