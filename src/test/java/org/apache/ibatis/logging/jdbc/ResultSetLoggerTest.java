/*
 *    Copyright 2009-2022 the original author or authors.
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResultSetLoggerTest {

  @Mock
  private ResultSet rs;

  @Mock
  private Log log;

  @Mock
  private ResultSetMetaData metaData;

  private void setup(int type) throws SQLException {
    when(rs.next()).thenReturn(true);
    when(rs.getMetaData()).thenReturn(metaData);
    when(metaData.getColumnCount()).thenReturn(1);
    when(metaData.getColumnType(1)).thenReturn(type);
    when(metaData.getColumnLabel(1)).thenReturn("ColumnName");
    when(log.isTraceEnabled()).thenReturn(true);
    ResultSet resultSet = ResultSetLogger.newInstance(rs, log, 1);
    resultSet.next();
  }

  @Test
  void shouldNotPrintBlobs() throws SQLException {
    setup(Types.LONGNVARCHAR);
    verify(log).trace("<==    Columns: ColumnName");
    verify(log).trace("<==        Row: <<BLOB>>");
  }

  @Test
  void shouldPrintVarchars() throws SQLException {
    when(rs.getString(1)).thenReturn("value");
    setup(Types.VARCHAR);
    verify(log).trace("<==    Columns: ColumnName");
    verify(log).trace("<==        Row: value");
  }

}
