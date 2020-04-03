package org.apache.ibatis.type;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenericTypeJsonTypeHandler implements TypeHandler{
  private Type type;
  private static final Gson gson = new Gson();

  public GenericTypeJsonTypeHandler(Type type){
    this.type = type;
  }
  @Override
  public void setParameter(PreparedStatement ps, int i, Object parameter,
    JdbcType jdbcType) throws SQLException {
    ps.setString(i, gson.toJson(parameter));
  }

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    String json = rs.getString(columnName);
    return gson.fromJson(json, type);
  }

  @Override
  public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
    String json = rs.getString(columnIndex);
    return gson.fromJson(json, type);
  }

  @Override
  public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
    String json = cs.getString(columnIndex);
    return gson.fromJson(json, type);
  }
}
