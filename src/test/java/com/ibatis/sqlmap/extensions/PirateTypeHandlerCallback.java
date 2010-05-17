package com.ibatis.sqlmap.extensions;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

import java.sql.SQLException;

public class PirateTypeHandlerCallback implements TypeHandlerCallback {

  public Object getResult(ResultGetter getter) throws SQLException {
    String s = getter.getString();
    if ("Aye".equalsIgnoreCase(s)) {
      return new Boolean(true);
    } else if ("Nay".equalsIgnoreCase(s)) {
      return new Boolean(false);
    } else {
      throw new SQLException("Unexpected value " + s + " found where 'Aye' or 'Nay' was expected.");
    }
  }

  public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
    boolean b = ((Boolean) parameter).booleanValue();
    if (b) {
      setter.setString("Aye");
    } else {
      setter.setString("Nay");
    }
  }

  public Object valueOf(String s) {
    if ("Aye".equalsIgnoreCase(s)) {
      return new Boolean(true);
    } else if ("Nay".equalsIgnoreCase(s)) {
      return new Boolean(false);
    } else {
      throw new SqlMapException("Unexpected value " + s + " found where 'Aye' or 'Nay' was expected.");
    }
  }

}
