/*
 *    Copyright 2009-2012 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import domain.misc.RichType;

public class TypeHandlerRegistryTest {

  private TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

  @Test
  public void shouldRegisterAndRetrieveTypeHandler() {
    TypeHandler<String> stringTypeHandler = typeHandlerRegistry.getTypeHandler(String.class);
    typeHandlerRegistry.register(String.class, JdbcType.LONGVARCHAR, stringTypeHandler);
    assertEquals(stringTypeHandler, typeHandlerRegistry.getTypeHandler(String.class, JdbcType.LONGVARCHAR));

    assertTrue(typeHandlerRegistry.hasTypeHandler(String.class));
    assertFalse(typeHandlerRegistry.hasTypeHandler(RichType.class));
    assertTrue(typeHandlerRegistry.hasTypeHandler(String.class, JdbcType.LONGVARCHAR));
    assertTrue(typeHandlerRegistry.hasTypeHandler(String.class, JdbcType.INTEGER));
    assertTrue(typeHandlerRegistry.getUnknownTypeHandler() instanceof UnknownTypeHandler);
  }

  @Test
  public void shouldRegisterAndRetrieveComplexTypeHandler() {
    TypeHandler<List<URI>> fakeHandler = new TypeHandler<List<URI>>() {

    public void setParameter( PreparedStatement ps, int i, List<URI> parameter, JdbcType jdbcType )
      throws SQLException {
      // do nothing, fake method
    }

    public List<URI> getResult( CallableStatement cs, int columnIndex )
      throws SQLException {
      // do nothing, fake method
      return null;
    }

    public List<URI> getResult( ResultSet rs, int columnIndex )
      throws SQLException {
      // do nothing, fake method
      return null;
    }

    public List<URI> getResult( ResultSet rs, String columnName )
      throws SQLException {
      // do nothing, fake method
      return null;
    }

    };

    TypeReference<List<URI>> type = new TypeReference<List<URI>>(){};

    typeHandlerRegistry.register(type, fakeHandler);
    assertSame(fakeHandler, typeHandlerRegistry.getTypeHandler(type));
  }

  @Test
  public void shouldAutoRegisterAndRetrieveComplexTypeHandler() {
    TypeHandler<List<URI>> fakeHandler = new BaseTypeHandler<List<URI>>() {

      @Override
      public void setNonNullParameter( PreparedStatement ps, int i, List<URI> parameter, JdbcType jdbcType )
        throws SQLException {
        // do nothing, fake method
      }

      @Override
      public List<URI> getNullableResult( ResultSet rs, String columnName )
        throws SQLException {
        // do nothing, fake method
        return null;
      }

      @Override
      public List<URI> getNullableResult( ResultSet rs, int columnIndex )
        throws SQLException {
        // do nothing, fake method
        return null;
      }

      @Override
      public List<URI> getNullableResult( CallableStatement cs, int columnIndex )
        throws SQLException {
        // do nothing, fake method
        return null;
      }

    };

    typeHandlerRegistry.register(fakeHandler);

    assertSame(fakeHandler, typeHandlerRegistry.getTypeHandler(new TypeReference<List<URI>>(){}));
  }

  @Ignore("see https://github.com/mybatis/mybatis-3/issues/165")
  @Test
  public void shouldBindHandlersToWrapersAndPrimitivesIndividually() {
    typeHandlerRegistry.register(Integer.class, DateTypeHandler.class);
    assertSame(IntegerTypeHandler.class, typeHandlerRegistry.getTypeHandler(int.class).getClass());
    typeHandlerRegistry.register(Integer.class, IntegerTypeHandler.class);
    typeHandlerRegistry.register(int.class, DateTypeHandler.class);
    assertSame(IntegerTypeHandler.class, typeHandlerRegistry.getTypeHandler(Integer.class).getClass());
    typeHandlerRegistry.register(Integer.class, IntegerTypeHandler.class);
  }
  
}
