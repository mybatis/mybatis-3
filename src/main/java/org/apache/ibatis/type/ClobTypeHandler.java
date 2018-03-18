/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.type;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 * @see ClobReaderTypeHandler
 */
public class ClobTypeHandler extends BaseTypeHandler<String> {

  private final BaseTypeHandler<Reader> clobReaderTypeHandler;

  /**
   * @since 3.5.0
   */
  public ClobTypeHandler() {
    this(new ClobReaderTypeHandler());
  }

  /**
   * @since 3.5.0
   */
  public ClobTypeHandler(BaseTypeHandler<Reader> clobReaderTypeHandler) {
    this.clobReaderTypeHandler = clobReaderTypeHandler;
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
      throws SQLException {
    clobReaderTypeHandler.setNonNullParameter(ps, i, new StringReader(parameter), jdbcType);
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    Reader reader = clobReaderTypeHandler.getNullableResult(rs, columnName);
    return toString(reader);
  }

  @Override
  public String getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Reader reader = clobReaderTypeHandler.getNullableResult(rs, columnIndex);
    return toString(reader);
  }

  @Override
  public String getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Reader reader = clobReaderTypeHandler.getNullableResult(cs, columnIndex);
    return toString(reader);
  }

  private String toString(Reader reader) throws SQLException {
    if (reader == null) {
      return "";
    }
    StringWriter writer = new StringWriter();
    try {
      char[] buffer = new char[4096];
      int readedCharLength;
      while ((readedCharLength = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, readedCharLength);
      }
      return writer.toString();
    }
    catch(IOException e) {
      throw new SQLException(e);
    }
    finally {
      try {
        reader.close();
      }
      catch (IOException ex) {
        // ignore
      }
    }
  }

}
