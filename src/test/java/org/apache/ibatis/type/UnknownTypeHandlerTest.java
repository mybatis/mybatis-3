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
package org.apache.ibatis.type;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import org.apache.ibatis.executor.result.ResultMapException;
import org.junit.Assert;
import org.junit.Test;

public class UnknownTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Object> TYPE_HANDLER = spy(new UnknownTypeHandler(new TypeHandlerRegistry()));

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
    verify(ps).setString(1, "Hello");
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    when(rs.getMetaData()).thenReturn(rsmd);
    when(rsmd.getColumnCount()).thenReturn(1);
    when(rsmd.getColumnName(1)).thenReturn("column");
    when(rsmd.getColumnClassName(1)).thenReturn(String.class.getName());
    when(rsmd.getColumnType(1)).thenReturn(JdbcType.VARCHAR.TYPE_CODE);
    when(rs.getString("column")).thenReturn("Hello");
    when(rs.wasNull()).thenReturn(false);
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    // Unnecessary
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getMetaData()).thenReturn(rsmd);
    when(rsmd.getColumnClassName(1)).thenReturn(String.class.getName());
    when(rsmd.getColumnType(1)).thenReturn(JdbcType.VARCHAR.TYPE_CODE);
    when(rs.getString(1)).thenReturn("Hello");
    when(rs.wasNull()).thenReturn(false);
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    // Unnecessary
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getObject(1)).thenReturn("Hello");
    when(cs.wasNull()).thenReturn(false);
    assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
  }

  @Override
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    // Unnecessary
  }

  @Test
  public void setParameterWithNullParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 0, null, JdbcType.INTEGER);
    verify(ps).setNull(0, JdbcType.INTEGER.TYPE_CODE);
  }

  @Test
  public void setParameterWithNullParameterThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when(ps).setNull(1, JdbcType.INTEGER.TYPE_CODE);
    try {
      TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.INTEGER);
      Assert.fail("Should have thrown a TypeException");
    } catch (Exception e) {
      Assert.assertTrue("Expected TypedException", e instanceof TypeException);
      Assert.assertTrue("Parameter index is in exception", e.getMessage().contains("parameter #1"));
    }
  }

  @Test
  public void setParameterWithNonNullParameterThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when((UnknownTypeHandler)TYPE_HANDLER).setNonNullParameter(ps, 1, 99, JdbcType.INTEGER);
    try {
      TYPE_HANDLER.setParameter(ps, 1, 99, JdbcType.INTEGER);
      Assert.fail("Should have thrown a TypeException");
    } catch (Exception e) {
      Assert.assertTrue("Expected TypedException", e instanceof TypeException);
      Assert.assertTrue("Parameter index is in exception", e.getMessage().contains("parameter #1"));
    }
  }

  @Test
  public void getResultWithResultSetAndColumnNameThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when((UnknownTypeHandler)TYPE_HANDLER).getNullableResult(rs, "foo");
    try {
      TYPE_HANDLER.getResult(rs, "foo");
      Assert.fail("Should have thrown a ResultMapException");
    } catch (Exception e) {
      Assert.assertTrue("Expected ResultMapException", e instanceof ResultMapException);
      Assert.assertTrue("column name is not in exception", e.getMessage().contains("column 'foo'"));
    }
  }

  @Test
  public void getResultWithResultSetAndColumnIndexThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when((UnknownTypeHandler)TYPE_HANDLER).getNullableResult(rs, 1);
    try {
      TYPE_HANDLER.getResult(rs, 1);
      Assert.fail("Should have thrown a ResultMapException");
    } catch (Exception e) {
      Assert.assertTrue("Expected ResultMapException", e instanceof ResultMapException);
      Assert.assertTrue("column index is not in exception", e.getMessage().contains("column #1"));
    }
  }

  @Test
  public void getResultWithCallableStatementAndColumnIndexThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when((UnknownTypeHandler)TYPE_HANDLER).getNullableResult(cs, 1);
    try {
      TYPE_HANDLER.getResult(cs, 1);
      Assert.fail("Should have thrown a ResultMapException");
    } catch (Exception e) {
      Assert.assertTrue("Expected ResultMapException", e instanceof ResultMapException);
      Assert.assertTrue("column index is not in exception", e.getMessage().contains("column #1"));
    }
  }

}