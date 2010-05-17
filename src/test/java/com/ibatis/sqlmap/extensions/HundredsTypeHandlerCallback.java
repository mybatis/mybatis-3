package com.ibatis.sqlmap.extensions;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

import java.sql.SQLException;

public class HundredsTypeHandlerCallback implements TypeHandlerCallback {

  public Object getResult(ResultGetter getter) throws SQLException {
    int i = getter.getInt();
    if (i == 100) {
      return new Boolean(true);
    } else if (i == 200) {
      return new Boolean(false);
    } else {
      throw new SQLException("Unexpected value " + i + " found where 100 or 200 was expected.");
    }
  }

  public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
    boolean b = ((Boolean) parameter).booleanValue();
    if (b) {
      setter.setInt(100);
    } else {
      setter.setInt(200);
    }
  }

  public Object valueOf(String s) {
    if ("100".equalsIgnoreCase(s)) {
      return new Boolean(true);
    } else if ("200".equalsIgnoreCase(s)) {
      return new Boolean(false);
    } else {
      throw new SqlMapException("Unexpected value " + s + " found where 100 or 200 was expected.");
    }
  }

}
