package org.apache.ibatis.type;
/**
 *    Copyright 2009-2015 the original author or authors.
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
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * @author gaohang on 16/3/14.
 */
public class InterfaceOrSuperClassBasedTypeHandlerTest {

  @Test
  public void testGetTypeHandlerWhenSuperClassHasTypeHandler() {
    TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    typeHandlerRegistry.register(Base.class, BaseBeanTypeHandler.class);

    TypeHandler<Actual> typeHandler = typeHandlerRegistry.getTypeHandler(Actual.class);
    assertEquals(BaseBeanTypeHandler.class, typeHandler.getClass());
  }

  @Test
  public void testGetTypeHandlerWhenAncestorClassHasTypeHandler() {
    TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    typeHandlerRegistry.register(Base.class, BaseBeanTypeHandler.class);

    TypeHandler<Actual2> typeHandler = typeHandlerRegistry.getTypeHandler(Actual2.class);
    assertEquals(BaseBeanTypeHandler.class, typeHandler.getClass());
  }

  @Test
  public void testGetTypeHandlerWhenInterfaceClassHasTypeHandler() {
    TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    typeHandlerRegistry.register(In2.class, InTypeHandler2.class);

    TypeHandler<Actual2> typeHandler = typeHandlerRegistry.getTypeHandler(Actual2.class);
    assertEquals(InTypeHandler2.class, typeHandler.getClass());
  }

  @Test
  public void testGetTypeHandlerWhenAncestorInterfaceClassHasTypeHandler() {
    TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    typeHandlerRegistry.register(In.class, InTypeHandler.class);

    TypeHandler<Actual2> typeHandler = typeHandlerRegistry.getTypeHandler(Actual2.class);
    assertEquals(InTypeHandler.class, typeHandler.getClass());
  }

  @Test
  public void testGetTypeHandlerWhenAncestorAndDirectInterfaceClassHasTypeHandler() {
    TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    typeHandlerRegistry.register(In.class, InTypeHandler.class);
    typeHandlerRegistry.register(In2.class, InTypeHandler2.class);

    TypeHandler<Actual2> typeHandler = typeHandlerRegistry.getTypeHandler(Actual2.class);
    assertEquals(InTypeHandler2.class, typeHandler.getClass());
  }

  @Test
  public void testGetTypeHandlerWhenAncestorAndDirectInterfaceClassAndSuperClassHasTypeHandler() {
    TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    typeHandlerRegistry.register(In.class, InTypeHandler.class);
    typeHandlerRegistry.register(In2.class, InTypeHandler2.class);
    typeHandlerRegistry.register(Base.class, BaseBeanTypeHandler.class);

    TypeHandler<Actual2> typeHandler = typeHandlerRegistry.getTypeHandler(Actual2.class);
    assertEquals(BaseBeanTypeHandler.class, typeHandler.getClass());
  }

  @Test
  public void testGetTypeHandlerWhenAllSuperClassAndInterfaceHasTypeHandler() {
    TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    typeHandlerRegistry.register(In.class, InTypeHandler.class);
    typeHandlerRegistry.register(In2.class, InTypeHandler2.class);
    typeHandlerRegistry.register(Base.class, BaseBeanTypeHandler.class);
    typeHandlerRegistry.register(Actual.class, ActualTypeHandler.class);

    TypeHandler<Actual2> typeHandler = typeHandlerRegistry.getTypeHandler(Actual2.class);
    assertEquals(ActualTypeHandler.class, typeHandler.getClass());
  }

  public interface In {
  }
  public interface In2 {
  }

  public static class Base {
  }

  public static class Actual extends Base implements In {
  }

  public static class Actual2 extends Actual implements In2 {
  }

  public static class BaseBeanTypeHandler extends BaseTypeHandler<Base> {
    @Override public void setNonNullParameter(PreparedStatement ps, int i, Base parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override public Base getNullableResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override public Base getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public Base getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }
  public static class ActualTypeHandler extends BaseTypeHandler<Actual> {
    @Override public void setNonNullParameter(PreparedStatement ps, int i, Actual parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override public Actual getNullableResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override public Actual getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public Actual getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }
  public static class InTypeHandler extends BaseTypeHandler<In> {
    @Override public void setNonNullParameter(PreparedStatement ps, int i, In parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override public In getNullableResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override public In getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public In getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }
  public static class InTypeHandler2 extends BaseTypeHandler<In2> {
    @Override public void setNonNullParameter(PreparedStatement ps, int i, In2 parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override public In2 getNullableResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override public In2 getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public In2 getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }
}
