/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.ibatis.executor.result.ResultMapException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ArrayTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Object> TYPE_HANDLER = new ArrayTypeHandler();

  @Mock
  Array mockArray;

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, mockArray, null);
    verify(ps).setArray(1, mockArray);
    verify(mockArray, never()).free();
  }

  @Test
  void shouldSetStringArrayParameter() throws Exception {
    Connection connection = mock(Connection.class);
    when(ps.getConnection()).thenReturn(connection);

    Array array = mock(Array.class);
    when(connection.createArrayOf(anyString(), any(String[].class))).thenReturn(array);

    TYPE_HANDLER.setParameter(ps, 1, new String[] { "Hello World" }, JdbcType.ARRAY);
    verify(ps).setArray(1, array);
    verify(array).free();
  }

  @Test
  void shouldSetNullParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.ARRAY);
    verify(ps).setNull(1, Types.ARRAY);
  }

  @Test
  void shouldFailForNonArrayParameter() {
    assertThrows(TypeException.class, () -> TYPE_HANDLER.setParameter(ps, 1, "unsupported parameter type", null));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    when(rs.getArray("column")).thenReturn(mockArray);
    String[] stringArray = { "a", "b" };
    when(mockArray.getArray()).thenReturn(stringArray);
    assertEquals(stringArray, TYPE_HANDLER.getResult(rs, "column"));
    verify(mockArray).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getArray("column")).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getArray(1)).thenReturn(mockArray);
    String[] stringArray = { "a", "b" };
    when(mockArray.getArray()).thenReturn(stringArray);
    assertEquals(stringArray, TYPE_HANDLER.getResult(rs, 1));
    verify(mockArray).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getArray(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getArray(1)).thenReturn(mockArray);
    String[] stringArray = { "a", "b" };
    when(mockArray.getArray()).thenReturn(stringArray);
    assertEquals(stringArray, TYPE_HANDLER.getResult(cs, 1));
    verify(mockArray).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getArray(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
  }

  private Array stubArrayCreatedForBinding() throws SQLException {
    Connection connection = mock(Connection.class);
    when(ps.getConnection()).thenReturn(connection);
    Array array = mock(Array.class);
    when(connection.createArrayOf(anyString(), any(Object[].class))).thenReturn(array);
    return array;
  }

  private void verifyBindingFailureIsPrimary(TypeException e, SQLException bindFailure) {
    assertSame(bindFailure, e.getCause());
    assertTrue(e.getMessage().contains("Error setting non null for parameter #1 with JdbcType ARRAY"), e.getMessage());
  }

  // A locally created array is handler owned: it must be freed even when binding it fails.
  @Test
  void shouldFreeArrayCreatedForBindingWhenSetArrayFails() throws Exception {
    Array array = stubArrayCreatedForBinding();
    SQLException bindFailure = new SQLException("setArray failed");
    doThrow(bindFailure).when(ps).setArray(1, array);

    TypeException e = assertThrows(TypeException.class,
        () -> TYPE_HANDLER.setParameter(ps, 1, new Object[] { "Hello World" }, JdbcType.ARRAY));
    verifyBindingFailureIsPrimary(e, bindFailure);
    verify(array).free();
  }

  // A cleanup failure must not hide the original binding failure.
  @Test
  void shouldPreserveBindingFailureWhenFreeingCreatedArrayFails() throws Exception {
    Array array = stubArrayCreatedForBinding();
    SQLException bindFailure = new SQLException("setArray failed");
    SQLException freeFailure = new SQLException("free failed");
    doThrow(bindFailure).when(ps).setArray(1, array);
    doThrow(freeFailure).when(array).free();

    TypeException e = assertThrows(TypeException.class,
        () -> TYPE_HANDLER.setParameter(ps, 1, new Object[] { "Hello World" }, JdbcType.ARRAY));
    verifyBindingFailureIsPrimary(e, bindFailure);
    assertArrayEquals(new Throwable[] { freeFailure }, bindFailure.getSuppressed());
  }

  // A cleanup failure is still propagated when the binding itself succeeded.
  @Test
  void shouldPropagateFreeFailureWhenBindingSucceeds() throws Exception {
    Array array = stubArrayCreatedForBinding();
    SQLException freeFailure = new SQLException("free failed");
    doThrow(freeFailure).when(array).free();

    TypeException e = assertThrows(TypeException.class,
        () -> TYPE_HANDLER.setParameter(ps, 1, new Object[] { "Hello World" }, JdbcType.ARRAY));
    verify(ps).setArray(1, array);
    assertSame(freeFailure, e.getCause());
  }

  // An unchecked binding failure is treated like a SQLException: it stays primary and the array is freed.
  @Test
  void shouldFreeCreatedArrayWhenSetArrayFailsWithUncheckedException() throws Exception {
    Array array = stubArrayCreatedForBinding();
    IllegalStateException bindFailure = new IllegalStateException("setArray failed");
    doThrow(bindFailure).when(ps).setArray(1, array);

    TypeException e = assertThrows(TypeException.class,
        () -> TYPE_HANDLER.setParameter(ps, 1, new Object[] { "Hello World" }, JdbcType.ARRAY));
    assertSame(bindFailure, e.getCause());
    verify(array).free();
  }

  // A failure must never be added to itself when the driver throws the same SQLException from free().
  @Test
  void shouldNotSelfSuppressWhenFreeFailsWithSameException() throws Exception {
    Array array = stubArrayCreatedForBinding();
    SQLException bindFailure = new SQLException("setArray failed");
    doThrow(bindFailure).when(ps).setArray(1, array);
    doThrow(bindFailure).when(array).free();

    TypeException e = assertThrows(TypeException.class,
        () -> TYPE_HANDLER.setParameter(ps, 1, new Object[] { "Hello World" }, JdbcType.ARRAY));
    verifyBindingFailureIsPrimary(e, bindFailure);
    assertEquals(0, bindFailure.getSuppressed().length);
  }

  // A prebuilt java.sql.Array parameter is caller owned: binding it must never free it, even on failure.
  @Test
  void shouldNotFreeCallerOwnedArrayWhenSetArrayFails() throws Exception {
    SQLException bindFailure = new SQLException("setArray failed");
    doThrow(bindFailure).when(ps).setArray(1, mockArray);

    TypeException e = assertThrows(TypeException.class,
        () -> TYPE_HANDLER.setParameter(ps, 1, mockArray, JdbcType.ARRAY));
    verifyBindingFailureIsPrimary(e, bindFailure);
    verify(mockArray, never()).free();
  }

  @Test
  void shouldFreeResultSetArrayWhenGetArrayFails() throws Exception {
    when(rs.getArray("column")).thenReturn(mockArray);
    SQLException extractFailure = new SQLException("getArray failed");
    when(mockArray.getArray()).thenThrow(extractFailure);

    ResultMapException e = assertThrows(ResultMapException.class, () -> TYPE_HANDLER.getResult(rs, "column"));
    assertSame(extractFailure, e.getCause());
    verify(mockArray).free();
  }

  @Test
  void shouldFreeResultSetArrayByIndexWhenGetArrayFails() throws Exception {
    when(rs.getArray(1)).thenReturn(mockArray);
    SQLException extractFailure = new SQLException("getArray failed");
    when(mockArray.getArray()).thenThrow(extractFailure);

    ResultMapException e = assertThrows(ResultMapException.class, () -> TYPE_HANDLER.getResult(rs, 1));
    assertSame(extractFailure, e.getCause());
    verify(mockArray).free();
  }

  @Test
  void shouldFreeCallableStatementArrayWhenGetArrayFails() throws Exception {
    when(cs.getArray(1)).thenReturn(mockArray);
    SQLException extractFailure = new SQLException("getArray failed");
    when(mockArray.getArray()).thenThrow(extractFailure);

    ResultMapException e = assertThrows(ResultMapException.class, () -> TYPE_HANDLER.getResult(cs, 1));
    assertSame(extractFailure, e.getCause());
    verify(mockArray).free();
  }

  // A cleanup failure must not hide the original extraction failure.
  @Test
  void shouldPreserveExtractionFailureWhenFreeingResultSetArrayFails() throws Exception {
    when(rs.getArray("column")).thenReturn(mockArray);
    SQLException extractFailure = new SQLException("getArray failed");
    SQLException freeFailure = new SQLException("free failed");
    when(mockArray.getArray()).thenThrow(extractFailure);
    doThrow(freeFailure).when(mockArray).free();

    ResultMapException e = assertThrows(ResultMapException.class, () -> TYPE_HANDLER.getResult(rs, "column"));
    assertSame(extractFailure, e.getCause());
    assertArrayEquals(new Throwable[] { freeFailure }, extractFailure.getSuppressed());
  }

  // A cleanup failure is still propagated when the extraction itself succeeded.
  @Test
  void shouldPropagateFreeFailureWhenExtractionSucceeds() throws Exception {
    when(rs.getArray("column")).thenReturn(mockArray);
    when(mockArray.getArray()).thenReturn(new String[] { "a", "b" });
    SQLException freeFailure = new SQLException("free failed");
    doThrow(freeFailure).when(mockArray).free();

    ResultMapException e = assertThrows(ResultMapException.class, () -> TYPE_HANDLER.getResult(rs, "column"));
    assertSame(freeFailure, e.getCause());
  }

  // An Error while binding must still free the created array and must surface unchanged; BaseTypeHandler wraps
  // Exception only, so no TypeException is created here.
  @Test
  void shouldFreeCreatedArrayWhenSetArrayFailsWithError() throws Exception {
    Array array = stubArrayCreatedForBinding();
    NoClassDefFoundError bindFailure = new NoClassDefFoundError("setArray failed");
    doThrow(bindFailure).when(ps).setArray(1, array);

    NoClassDefFoundError e = assertThrows(NoClassDefFoundError.class,
        () -> TYPE_HANDLER.setParameter(ps, 1, new Object[] { "Hello World" }, JdbcType.ARRAY));
    assertSame(bindFailure, e);
    verify(array).free();
  }

  // An Error thrown by the cleanup must not replace the SQLException that triggered it.
  @Test
  void shouldPreserveBindingFailureWhenFreeingCreatedArrayFailsWithError() throws Exception {
    Array array = stubArrayCreatedForBinding();
    SQLException bindFailure = new SQLException("setArray failed");
    NoClassDefFoundError freeFailure = new NoClassDefFoundError("free failed");
    doThrow(bindFailure).when(ps).setArray(1, array);
    doThrow(freeFailure).when(array).free();

    TypeException e = assertThrows(TypeException.class,
        () -> TYPE_HANDLER.setParameter(ps, 1, new Object[] { "Hello World" }, JdbcType.ARRAY));
    verifyBindingFailureIsPrimary(e, bindFailure);
    assertArrayEquals(new Throwable[] { freeFailure }, bindFailure.getSuppressed());
  }

  // An Error while extracting must free the array too; BaseTypeHandler wraps Exception only, so it surfaces as is.
  @Test
  void shouldFreeResultSetArrayWhenGetArrayFailsWithError() throws Exception {
    when(rs.getArray("column")).thenReturn(mockArray);
    NoClassDefFoundError extractFailure = new NoClassDefFoundError("getArray failed");
    when(mockArray.getArray()).thenThrow(extractFailure);

    NoClassDefFoundError e = assertThrows(NoClassDefFoundError.class, () -> TYPE_HANDLER.getResult(rs, "column"));
    assertSame(extractFailure, e);
    verify(mockArray).free();
  }

}
