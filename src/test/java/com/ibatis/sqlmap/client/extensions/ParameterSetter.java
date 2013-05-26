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
package com.ibatis.sqlmap.client.extensions;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

/*
 * Allows parameters to be set on the underlying prepared statement.
 * TypeHandlerCallback implementations use this interface to
 * process values before they are set on the prepared statement.
 * Each of these methods has a corresponding method on the
 * PreparedStatement class, the only difference being
 * that there is no need to specify the parameter index with these
 * methods.
 * <p/>
 * <b>NOTE:</b> There is no need to implement this.  The implementation
 * will be passed into the TypeHandlerCallback automatically.
 */
public interface ParameterSetter {

  /*
   * Set an array on the underlying prepared statement
   *
   * @param x - the array to set
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setArray(Array x) throws SQLException;

  /*
   * Set an InputStream on the underlying prepared statement
   *
   * @param x      - the InputStream
   * @param length - the length of the InputStream
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setAsciiStream(InputStream x, int length) throws SQLException;

  /*
   * Set an on the underlying prepared statement
   *
   * @param x
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setBigDecimal(BigDecimal x) throws SQLException;

  /*
   * Set an InputStream on the underlying prepared statement
   *
   * @param x      - the InputStream
   * @param length - the length of the InputStream
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setBinaryStream(InputStream x, int length) throws SQLException;

  /*
   * Set a blob on the underlying prepared statement
   *
   * @param x - the blob
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setBlob(Blob x) throws SQLException;

  /*
   * Set a boolean on the underlying prepared statement
   *
   * @param x - the boolean
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setBoolean(boolean x) throws SQLException;

  /*
   * Set a byte on the underlying prepared statement
   *
   * @param x - the byte
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setByte(byte x) throws SQLException;

  /*
   * Set a byte array on the underlying prepared statement
   *
   * @param x - the byte[]
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setBytes(byte x[]) throws SQLException;

  /*
   * Set a character stream on the underlying prepared statement
   *
   * @param reader - the reader
   * @param length - the length of the reader
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setCharacterStream(Reader reader, int length) throws SQLException;

  /*
   * Set a clob on the underlying prepared statement
   *
   * @param x - the clob
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setClob(Clob x) throws SQLException;

  /*
   * Set a date on the underlying prepared statement
   *
   * @param x - the date
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setDate(Date x) throws SQLException;

  /*
   * Set a date with a calendar on the underlying prepared statement
   *
   * @param x   - the date
   * @param cal - the calendar
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setDate(Date x, Calendar cal) throws SQLException;

  /*
   * Set a double on the underlying prepared statement
   *
   * @param x - the double
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setDouble(double x) throws SQLException;

  /*
   * Set a float on the underlying prepared statement
   *
   * @param x the float
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setFloat(float x) throws SQLException;

  /*
   * Set an integer on the underlying prepared statement
   *
   * @param x - the int
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setInt(int x) throws SQLException;

  /*
   * Set a long on the underlying prepared statement
   *
   * @param x - the long
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setLong(long x) throws SQLException;

  /*
   * Set a null on the underlying prepared statement
   *
   * @param sqlType - the type for the null value
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setNull(int sqlType) throws SQLException;

  /*
   * Set a null on the underlying prepared statement
   *
   * @param sqlType  -  the type for the null value
   * @param typeName -  the name of the type
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setNull(int sqlType, String typeName) throws SQLException;

  /*
   * Set an object on the underlying prepared statement
   *
   * @param x - the object to set
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setObject(Object x) throws SQLException;

  /*
   * Set an object on the underlying prepared statement
   *
   * @param x             - the object to set
   * @param targetSqlType - the sql type of the object
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setObject(Object x, int targetSqlType) throws SQLException;

  /*
   * Set an object on the underlying prepared statement
   *
   * @param x             - the object to set
   * @param targetSqlType - the sql type of the object
   * @param scale         - the scale of the object
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setObject(Object x, int targetSqlType, int scale) throws SQLException;

  /*
   * Set a reference on the underlying prepared statement
   *
   * @param x - the reference to set
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setRef(Ref x) throws SQLException;

  /*
   * Set a short on the underlying prepared statement
   *
   * @param x - the short to set
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setShort(short x) throws SQLException;

  /*
   * Set a string on the underlying prepared statement
   *
   * @param x - the string to set
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setString(String x) throws SQLException;

  /*
   * Set a time on the underlying prepared statement
   *
   * @param x - the time to set
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setTime(Time x) throws SQLException;

  /*
   * Set a time with a calendar on the underlying prepared statement
   *
   * @param x   - the time to set
   * @param cal - the calendar to use
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setTime(Time x, Calendar cal) throws SQLException;

  /*
   * Set a timestamp on the underlying prepared statement
   *
   * @param x - the timestamp to set
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setTimestamp(Timestamp x) throws SQLException;

  /*
   * Set a timestamp on the underlying prepared statement
   *
   * @param x   - the timestamp to set
   * @param cal - the calendar to use
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setTimestamp(Timestamp x, Calendar cal) throws SQLException;

  /*
   * Set a URL on the underlying prepared statement
   *
   * @param x - the url to set
   * @throws java.sql.SQLException - thrown if the underlying prepared statement throws it
   */
  public void setURL(URL x) throws SQLException;

  /*
   * Returns the underlying prepared statement...be careful!
   */
  public PreparedStatement getPreparedStatement();

  /*
   * Returns the index of the parameter being set.
   *
   * @return the parameter index used to set the value in the underlying
   *         PreparedStatement
   */
  public int getParameterIndex();
}
