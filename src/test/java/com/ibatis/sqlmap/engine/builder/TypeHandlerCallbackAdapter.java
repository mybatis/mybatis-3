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
package com.ibatis.sqlmap.engine.builder;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TypeHandlerCallbackAdapter implements TypeHandler {

  private TypeHandlerCallback callback;

  public TypeHandlerCallbackAdapter(TypeHandlerCallback callback) {
    this.callback = callback;
  }

  public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    callback.setParameter(new ParameterSetterImpl(ps, i), parameter);
  }

  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    return callback.getResult(new ResultGetterImpl(rs, columnName));
  }

  public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
    return callback.getResult(new ResultGetterImpl(rs, columnIndex));
  }

  public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
    return callback.getResult(new ResultGetterImpl(CallableStatementResultSet.newProxy(cs), columnIndex));
  }

  public static class ParameterSetterImpl implements ParameterSetter {

    private PreparedStatement ps;
    private int index;

    /*
     * Creates an instance for a PreparedStatement and column index
     *
     * @param statement   - the PreparedStatement
     * @param columnIndex - the column index
     */
    public ParameterSetterImpl(PreparedStatement statement, int columnIndex) {
      this.ps = statement;
      this.index = columnIndex;
    }

    public void setArray(Array x) throws SQLException {
      ps.setArray(index, x);
    }

    public void setAsciiStream(InputStream x, int length) throws SQLException {
      ps.setAsciiStream(index, x, length);
    }

    public void setBigDecimal(BigDecimal x) throws SQLException {
      ps.setBigDecimal(index, x);
    }

    public void setBinaryStream(InputStream x, int length) throws SQLException {
      ps.setBinaryStream(index, x, length);
    }

    public void setBlob(Blob x) throws SQLException {
      ps.setBlob(index, x);
    }

    public void setBoolean(boolean x) throws SQLException {
      ps.setBoolean(index, x);
    }

    public void setByte(byte x) throws SQLException {
      ps.setByte(index, x);
    }

    public void setBytes(byte x[]) throws SQLException {
      ps.setBytes(index, x);
    }

    public void setCharacterStream(Reader reader, int length) throws SQLException {
      ps.setCharacterStream(index, reader, length);
    }

    public void setClob(Clob x) throws SQLException {
      ps.setClob(index, x);
    }

    public void setDate(Date x) throws SQLException {
      ps.setDate(index, x);
    }

    public void setDate(Date x, Calendar cal) throws SQLException {
      ps.setDate(index, x, cal);
    }

    public void setDouble(double x) throws SQLException {
      ps.setDouble(index, x);
    }

    public void setFloat(float x) throws SQLException {
      ps.setFloat(index, x);
    }

    public void setInt(int x) throws SQLException {
      ps.setInt(index, x);
    }

    public void setLong(long x) throws SQLException {
      ps.setLong(index, x);
    }

    public void setNull(int sqlType) throws SQLException {
      ps.setNull(index, sqlType);
    }

    public void setNull(int sqlType, String typeName) throws SQLException {
      ps.setNull(index, sqlType, typeName);
    }

    public void setObject(Object x) throws SQLException {
      ps.setObject(index, x);
    }

    public void setObject(Object x, int targetSqlType) throws SQLException {
      ps.setObject(index, x, targetSqlType);
    }

    public void setObject(Object x, int targetSqlType, int scale) throws SQLException {
      ps.setObject(index, x, scale);
    }

    public void setRef(Ref x) throws SQLException {
      ps.setRef(index, x);
    }

    public void setShort(short x) throws SQLException {
      ps.setShort(index, x);
    }

    public void setString(String x) throws SQLException {
      ps.setString(index, x);
    }

    public void setTime(Time x) throws SQLException {
      ps.setTime(index, x);
    }

    public void setTime(Time x, Calendar cal) throws SQLException {
      ps.setTime(index, x, cal);
    }

    public void setTimestamp(Timestamp x) throws SQLException {
      ps.setTimestamp(index, x);
    }

    public void setTimestamp(Timestamp x, Calendar cal) throws SQLException {
      ps.setTimestamp(index, x, cal);
    }

    public void setURL(URL x) throws SQLException {
      ps.setURL(index, x);
    }

    public PreparedStatement getPreparedStatement() {
      return ps;
    }

    public int getParameterIndex() {
      return index;
    }
  }

  public static class ResultGetterImpl implements ResultGetter {

    private ResultSet rs;
    private String name;
    private int index;

    /*
     * Creates an instance for a PreparedStatement and column index
     *
     * @param resultSet   - the result set
     * @param columnIndex - the column index
     */
    public ResultGetterImpl(ResultSet resultSet, int columnIndex) {
      this.rs = resultSet;
      this.index = columnIndex;
    }

    /*
     * Creates an instance for a PreparedStatement and column name
     *
     * @param resultSet  - the result set
     * @param columnName - the column index
     */
    public ResultGetterImpl(ResultSet resultSet, String columnName) {
      this.rs = resultSet;
      this.name = columnName;
    }


    public Array getArray() throws SQLException {
      if (name != null) {
        return rs.getArray(name);
      } else {
        return rs.getArray(index);
      }
    }

    public BigDecimal getBigDecimal() throws SQLException {
      if (name != null) {
        return rs.getBigDecimal(name);
      } else {
        return rs.getBigDecimal(index);
      }
    }

    public Blob getBlob() throws SQLException {
      if (name != null) {
        return rs.getBlob(name);
      } else {
        return rs.getBlob(index);
      }
    }

    public boolean getBoolean() throws SQLException {
      if (name != null) {
        return rs.getBoolean(name);
      } else {
        return rs.getBoolean(index);
      }
    }

    public byte getByte() throws SQLException {
      if (name != null) {
        return rs.getByte(name);
      } else {
        return rs.getByte(index);
      }
    }

    public byte[] getBytes() throws SQLException {
      if (name != null) {
        return rs.getBytes(name);
      } else {
        return rs.getBytes(index);
      }
    }

    public Clob getClob() throws SQLException {
      if (name != null) {
        return rs.getClob(name);
      } else {
        return rs.getClob(index);
      }
    }

    public Date getDate() throws SQLException {
      if (name != null) {
        return rs.getDate(name);
      } else {
        return rs.getDate(index);
      }
    }

    public Date getDate(Calendar cal) throws SQLException {
      if (name != null) {
        return rs.getDate(name, cal);
      } else {
        return rs.getDate(index, cal);
      }
    }

    public double getDouble() throws SQLException {
      if (name != null) {
        return rs.getDouble(name);
      } else {
        return rs.getDouble(index);
      }
    }

    public float getFloat() throws SQLException {
      if (name != null) {
        return rs.getFloat(name);
      } else {
        return rs.getFloat(index);
      }
    }

    public int getInt() throws SQLException {
      if (name != null) {
        return rs.getInt(name);
      } else {
        return rs.getInt(index);
      }
    }

    public long getLong() throws SQLException {
      if (name != null) {
        return rs.getLong(name);
      } else {
        return rs.getLong(index);
      }
    }

    public Object getObject() throws SQLException {
      if (name != null) {
        return rs.getObject(name);
      } else {
        return rs.getObject(index);
      }
    }

    public Object getObject(Map map) throws SQLException {
      if (name != null) {
        return rs.getObject(name, map);
      } else {
        return rs.getObject(index, map);
      }
    }

    public Ref getRef() throws SQLException {
      if (name != null) {
        return rs.getRef(name);
      } else {
        return rs.getRef(index);
      }
    }

    public short getShort() throws SQLException {
      if (name != null) {
        return rs.getShort(name);
      } else {
        return rs.getShort(index);
      }
    }

    public String getString() throws SQLException {
      if (name != null) {
        return rs.getString(name);
      } else {
        return rs.getString(index);
      }
    }

    public Time getTime() throws SQLException {
      if (name != null) {
        return rs.getTime(name);
      } else {
        return rs.getTime(index);
      }
    }

    public Time getTime(Calendar cal) throws SQLException {
      if (name != null) {
        return rs.getTime(name);
      } else {
        return rs.getTime(index);
      }
    }

    public Timestamp getTimestamp() throws SQLException {
      if (name != null) {
        return rs.getTimestamp(name);
      } else {
        return rs.getTimestamp(index);
      }
    }

    public Timestamp getTimestamp(Calendar cal) throws SQLException {
      if (name != null) {
        return rs.getTimestamp(name, cal);
      } else {
        return rs.getTimestamp(index, cal);
      }
    }

    public URL getURL() throws SQLException {
      if (name != null) {
        return rs.getURL(name);
      } else {
        return rs.getURL(index);
      }
    }

    public boolean wasNull() throws SQLException {
      return rs.wasNull();
    }

    public ResultSet getResultSet() {
      return rs;
    }

    public int getColumnIndex() {
      return index;
    }

    public String getColumnName() {
      return name;
    }
  }

  public static class CallableStatementResultSet implements InvocationHandler {

    private CallableStatement cs;

    private CallableStatementResultSet(CallableStatement cs) {
      this.cs = cs;
    }

    public static ResultSet newProxy(CallableStatement cs) {
      return (ResultSet) Proxy.newProxyInstance(cs.getClass().getClassLoader(),new Class[]{ResultSet.class}, new CallableStatementResultSet(cs));
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Method csMethod = cs.getClass().getMethod(method.getName(),method.getParameterTypes());
      return csMethod.invoke(cs,args);
    }

  }


}
