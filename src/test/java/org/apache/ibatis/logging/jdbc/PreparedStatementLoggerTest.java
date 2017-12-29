/**
 *    Copyright 2009-2016 the original author or authors.
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

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PreparedStatementLoggerTest {

  @Mock
  Log log;

  @Mock
  PreparedStatement preparedStatement;

  @Mock
  ResultSet resultSet;

  PreparedStatement ps;
  @Before
  public void setUp() throws SQLException {
    when(log.isDebugEnabled()).thenReturn(true);

    when(preparedStatement.executeQuery(anyString())).thenReturn(resultSet);
    when(preparedStatement.execute(anyString())).thenReturn(true);
    ps = PreparedStatementLogger.newInstance(this.preparedStatement, log, 1);
  }

  @Test
  public void shouldPrintParameters() throws SQLException {
    ps.setInt(1, 10);
    ResultSet rs = ps.executeQuery("select 1 limit ?");

    verify(log).debug(contains("Parameters: 10(Integer)"));
    Assert.assertNotNull(rs);
    Assert.assertNotSame(resultSet, rs);
  }

  @Test
  public void shouldPrintNullParameters() throws SQLException {
    ps.setNull(1, JdbcType.VARCHAR.TYPE_CODE);
    boolean result = ps.execute("update name = ? from test");

    verify(log).debug(contains("Parameters: null"));
    Assert.assertTrue(result);
  }

  @Test
  public void shouldNotPrintLog() throws SQLException {
    ps.getResultSet();
    ps.getParameterMetaData();

    verify(log, times(0)).debug(anyString());
  }

  @Test
  public void shouldPrintUpdateCount() throws SQLException {
    when(preparedStatement.getUpdateCount()).thenReturn(1);
    ps.getUpdateCount();

    verify(log).debug(contains("Updates: 1"));
  }
}
