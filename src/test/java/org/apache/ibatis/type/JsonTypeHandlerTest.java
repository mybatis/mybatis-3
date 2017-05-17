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
package org.apache.ibatis.type;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.executor.result.ResultMapException;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class JsonTypeHandlerTest extends BaseTypeHandlerTest {

  /*
   * JsonEntity is json column
   */
  public static class JsonEntity{
    private String name;
    private Integer age;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getAge() {
      return age;
    }

    public void setAge(Integer age) {
      this.age = age;
    }
  }
  public static class TableEntity{
    Integer id;
    JsonEntity jsonEntity;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public JsonEntity getJsonEntity() {
      return jsonEntity;
    }

    public void setJsonEntity(JsonEntity jsonEntity) {
      this.jsonEntity = jsonEntity;
    }
  }

  private static JsonEntity jsonEntity = new JsonEntity();
  private static String jsonString;
  private static TableEntity tableEntity = new TableEntity();
  private static final JsonTypeHandler JSON_TYPE_HANDLER = spy(new FastJsonTypeHandler());

  static {
    jsonEntity.setAge(10);
    jsonEntity.setName("Jack");
    jsonString = JSON.toJSONString(jsonEntity);
    tableEntity.setId(1);
    tableEntity.setJsonEntity(jsonEntity);
  }
  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    JSON_TYPE_HANDLER.setParameter(ps, 1, jsonEntity, null);
    verify(ps).setString(1, jsonString);
  }

  @Test
  public void shouldGetResultFromResultSetByNamePropertyObject() throws Exception{
      when(rs.getString("column")).thenReturn(jsonString);
      JsonEntity dbData = (JsonEntity) JSON_TYPE_HANDLER.getNullableResult(rs, "column", "jsonEntity", tableEntity);
      assertEquals(jsonEntity.getAge(),dbData.getAge());
      assertEquals(jsonEntity.getName(),dbData.getName());
  }

  @Test
  public void shouldGetResultFromResultSetByNameType() throws Exception{
      when(rs.getString("column")).thenReturn(jsonString);
      JsonEntity dbData = (JsonEntity) JSON_TYPE_HANDLER.getNullableResult(rs, "column", JsonEntity.class);
      assertEquals(jsonEntity.getAge(),dbData.getAge());
      assertEquals(jsonEntity.getName(),dbData.getName());
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
      when(rs.getObject("column")).thenReturn(jsonString);
      when(rs.wasNull()).thenReturn(false);
      assertEquals(jsonString, JSON_TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    // Unnecessary
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getObject(1)).thenReturn(jsonString);
    when(rs.wasNull()).thenReturn(false);
    assertEquals(jsonString, JSON_TYPE_HANDLER.getResult(rs, 1));
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
    assertEquals("Hello", JSON_TYPE_HANDLER.getResult(cs, 1));
  }

  @Override
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    // Unnecessary
  }

  @Test
  public void setParameterWithNullParameter() throws Exception {
    JSON_TYPE_HANDLER.setParameter(ps, 0, null, JdbcType.OTHER);
    verify(ps).setNull(0, JdbcType.OTHER.TYPE_CODE);
  }

  @Test
  public void setParameterWithNullParameterThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when(ps).setNull(1, JdbcType.OTHER.TYPE_CODE);
    try {
      JSON_TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.OTHER);
      Assert.fail("Should have thrown a TypeException");
    } catch (Exception e) {
      Assert.assertTrue("Expected TypedException", e instanceof TypeException);
      Assert.assertTrue("Parameter index is in exception", e.getMessage().contains("parameter #1"));
    }
  }

  @Test
  public void setParameterWithNonNullParameterThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when((FastJsonTypeHandler)JSON_TYPE_HANDLER).setNonNullParameter(ps, 1, jsonEntity, JdbcType.OTHER);
    try {
      JSON_TYPE_HANDLER.setParameter(ps, 1, jsonEntity, JdbcType.OTHER);
      Assert.fail("Should have thrown a TypeException");
    } catch (Exception e) {
      Assert.assertTrue("Expected TypedException", e instanceof TypeException);
      Assert.assertTrue("Parameter index is in exception", e.getMessage().contains("parameter #1"));
    }
  }

  @Test
  public void getResultWithResultSetAndColumnNameThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when((FastJsonTypeHandler)JSON_TYPE_HANDLER).getNullableResult(rs, "foo");
    try {
      JSON_TYPE_HANDLER.getResult(rs, "foo");
      Assert.fail("Should have thrown a ResultMapException");
    } catch (Exception e) {
      Assert.assertTrue("Expected ResultMapException", e instanceof ResultMapException);
      Assert.assertTrue("column name is not in exception", e.getMessage().contains("column 'foo'"));
    }
  }

  @Test
  public void getResultWithResultSetAndColumnIndexThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when((FastJsonTypeHandler)JSON_TYPE_HANDLER).getNullableResult(rs, 1);
    try {
      JSON_TYPE_HANDLER.getResult(rs, 1);
      Assert.fail("Should have thrown a ResultMapException");
    } catch (Exception e) {
      Assert.assertTrue("Expected ResultMapException", e instanceof ResultMapException);
      Assert.assertTrue("column index is not in exception", e.getMessage().contains("column #1"));
    }
  }

  @Test
  public void getResultWithCallableStatementAndColumnIndexThrowsException() throws SQLException {
    doThrow(new SQLException("invalid column")).when((FastJsonTypeHandler)JSON_TYPE_HANDLER).getNullableResult(cs, 1);
    try {
      JSON_TYPE_HANDLER.getResult(cs, 1);
      Assert.fail("Should have thrown a ResultMapException");
    } catch (Exception e) {
      Assert.assertTrue("Expected ResultMapException", e instanceof ResultMapException);
      Assert.assertTrue("column index is not in exception", e.getMessage().contains("column #1"));
    }
  }

}