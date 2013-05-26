/*
 *    Copyright 2009-2013 the original author or authors.
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
package org.apache.ibatis.logging.jdbc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.jdbc.ResultSetLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultSetLoggerTest {

  @Mock
  private ResultSet rs;

  @Mock
  private Log log;

  @Mock
  private ResultSetMetaData metaData;

  public void setup(int type) throws SQLException {
    when(rs.next()).thenReturn(true);
    when(rs.getMetaData()).thenReturn(metaData);
    when(metaData.getColumnCount()).thenReturn(1);
    when(metaData.getColumnType(1)).thenReturn(type);
    when(metaData.getColumnLabel(1)).thenReturn("ColumnName");
    when(rs.getString(1)).thenReturn("value");
    when(log.isTraceEnabled()).thenReturn(true);
    ResultSet resultSet = ResultSetLogger.newInstance(rs, log);
    resultSet.next();
  }

  @Test
  public void shouldNotPrintBlobs() throws SQLException {
    setup(Types.LONGNVARCHAR);
    verify(log).trace("<==    Columns: ColumnName");
    verify(log).trace("<==        Row: <<BLOB>>");
  }

  @Test
  public void shouldPrintVarchars() throws SQLException {
    setup(Types.VARCHAR);
    verify(log).trace("<==    Columns: ColumnName");
    verify(log).trace("<==        Row: value");
  }

}
