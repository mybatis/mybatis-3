/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
