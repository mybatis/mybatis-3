/*
 *    Copyright 2009-2021 the original author or authors.
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PreparedStatementLoggerTest {

  @Mock
  Log log;

  @Mock
  PreparedStatement preparedStatement;

  @Mock
  ResultSet resultSet;

  private PreparedStatement ps;

  @BeforeEach
  void setUp() throws SQLException {
    ps = PreparedStatementLogger.newInstance(this.preparedStatement, log, 1);
  }

  @Test
  void shouldPrintParameters() throws SQLException {
    when(log.isDebugEnabled()).thenReturn(true);
    when(preparedStatement.executeQuery(anyString())).thenReturn(resultSet);

    ps.setInt(1, 10);
    ResultSet rs = ps.executeQuery("select 1 limit ?");

    verify(log).debug(contains("Parameters: 10(Integer)"));
    Assertions.assertNotNull(rs);
    Assertions.assertNotSame(resultSet, rs);
  }

  @Test
  void shouldPrintNullParameters() throws SQLException {
    when(log.isDebugEnabled()).thenReturn(true);
    when(preparedStatement.execute(anyString())).thenReturn(true);

    ps.setNull(1, JdbcType.VARCHAR.TYPE_CODE);
    boolean result = ps.execute("update name = ? from test");

    verify(log).debug(contains("Parameters: null"));
    Assertions.assertTrue(result);
  }

  @Test
  void shouldNotPrintLog() throws SQLException {
    ps.getResultSet();
    ps.getParameterMetaData();

    verify(log, times(0)).debug(anyString());
  }

  @Test
  void shouldPrintUpdateCount() throws SQLException {
    when(log.isDebugEnabled()).thenReturn(true);
    when(preparedStatement.getUpdateCount()).thenReturn(1);

    ps.getUpdateCount();

    verify(log).debug(contains("Updates: 1"));
  }
}
