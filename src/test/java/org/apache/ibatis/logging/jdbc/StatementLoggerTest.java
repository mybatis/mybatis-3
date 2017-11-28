/**
 *    Copyright 2009-2017 the original author or authors.
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatementLoggerTest {

  @Mock
  Statement statement;

  @Mock
  Log log;

  Statement st;

  @Before
  public void setUp() throws SQLException {
    when(log.isDebugEnabled()).thenReturn(true);
    when(statement.execute(anyString())).thenReturn(true);
    st = StatementLogger.newInstance(statement, log, 1);
  }

  @Test
  public void shouldPrintLog() throws SQLException {
    st.executeQuery("select 1");

    verify(log).debug(contains("Executing: select 1"));
  }

  @Test
  public void shouldPrintLogForUpdate() throws SQLException {
    String sql = "update name = '' from test";
    boolean execute = st.execute(sql);

    verify(log).debug(contains(sql));
    Assert.assertTrue(execute);
  }

  @Test
  public void shouldNotPrintLog() throws SQLException {
    st.getResultSet();
    st.close();
    verify(log, times(0)).debug(anyString());
  }
}
