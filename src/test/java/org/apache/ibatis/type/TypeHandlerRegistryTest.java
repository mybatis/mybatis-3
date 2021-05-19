/*
 *    Copyright 2009-2020 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.ibatis.domain.misc.RichType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeHandlerRegistryTest {

  private TypeHandlerRegistry typeHandlerRegistry;

  @BeforeEach
  void setup() {
    typeHandlerRegistry = new TypeHandlerRegistry();
  }

  @Test
  void shouldRegisterAndRetrieveTypeHandler() {
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
  void shouldRegisterAndRetrieveComplexTypeHandler() {
    TypeHandler<List<URI>> fakeHandler = new TypeHandler<List<URI>>() {

      @Override
      public void setParameter(PreparedStatement ps, int i, List<URI> parameter, JdbcType jdbcType) {
        // do nothing, fake method
      }

      @Override
      public List<URI> getResult(CallableStatement cs, int columnIndex) {
        // do nothing, fake method
        return null;
      }

      @Override
      public List<URI> getResult(ResultSet rs, int columnIndex) {
        // do nothing, fake method
        return null;
      }

      @Override
      public List<URI> getResult(ResultSet rs, String columnName) {
        // do nothing, fake method
        return null;
      }

    };

    TypeReference<List<URI>> type = new TypeReference<List<URI>>(){};

    typeHandlerRegistry.register(type, fakeHandler);
    assertSame(fakeHandler, typeHandlerRegistry.getTypeHandler(type));
  }

  @Test
  void shouldAutoRegisterAndRetrieveComplexTypeHandler() {
    TypeHandler<List<URI>> fakeHandler = new BaseTypeHandler<List<URI>>() {

      @Override
      public void setNonNullParameter(PreparedStatement ps, int i, List<URI> parameter, JdbcType jdbcType) {
        // do nothing, fake method
      }

      @Override
      public List<URI> getNullableResult(ResultSet rs, String columnName) {
        // do nothing, fake method
        return null;
      }

      @Override
      public List<URI> getNullableResult(ResultSet rs, int columnIndex) {
        // do nothing, fake method
        return null;
      }

      @Override
      public List<URI> getNullableResult(CallableStatement cs, int columnIndex) {
        // do nothing, fake method
        return null;
      }

    };

    typeHandlerRegistry.register(fakeHandler);

    assertSame(fakeHandler, typeHandlerRegistry.getTypeHandler(new TypeReference<List<URI>>(){}));
  }

  @Test
  void shouldBindHandlersToWrapersAndPrimitivesIndividually() {
    typeHandlerRegistry.register(Integer.class, DateTypeHandler.class);
    assertSame(IntegerTypeHandler.class, typeHandlerRegistry.getTypeHandler(int.class).getClass());
    typeHandlerRegistry.register(Integer.class, IntegerTypeHandler.class);
    typeHandlerRegistry.register(int.class, DateTypeHandler.class);
    assertSame(IntegerTypeHandler.class, typeHandlerRegistry.getTypeHandler(Integer.class).getClass());
    typeHandlerRegistry.register(Integer.class, IntegerTypeHandler.class);
  }

  @Test
  void shouldReturnHandlerForSuperclassIfRegistered() {
    class MyDate extends Date {
      private static final long serialVersionUID = 1L;
    }
    assertEquals(DateTypeHandler.class, typeHandlerRegistry.getTypeHandler(MyDate.class).getClass());
  }

  @Test
  void shouldReturnHandlerForSuperSuperclassIfRegistered() {
    class MyDate1 extends Date {
      private static final long serialVersionUID = 1L;
    }
    class MyDate2 extends MyDate1 {
      private static final long serialVersionUID = 1L;
    }
    assertEquals(DateTypeHandler.class, typeHandlerRegistry.getTypeHandler(MyDate2.class).getClass());
  }

  interface SomeInterface {
  }

  interface ExtendingSomeInterface extends SomeInterface {
  }

  interface NoTypeHandlerInterface {
  }

  enum SomeEnum implements SomeInterface {
  }

  enum ExtendingSomeEnum implements ExtendingSomeInterface {
  }

  enum ImplementingMultiInterfaceSomeEnum implements NoTypeHandlerInterface, ExtendingSomeInterface {
  }

  enum NoTypeHandlerInterfaceEnum implements NoTypeHandlerInterface {
  }

  class SomeClass implements SomeInterface {
  }

  @MappedTypes(SomeInterface.class)
  public static class SomeInterfaceTypeHandler<E extends Enum<E> & SomeInterface> extends BaseTypeHandler<E> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }

  @Test
  void demoTypeHandlerForSuperInterface() {
    typeHandlerRegistry.register(SomeInterfaceTypeHandler.class);
    assertNull(typeHandlerRegistry.getTypeHandler(SomeClass.class), "Registering interface works only for enums.");
    assertSame(EnumTypeHandler.class, typeHandlerRegistry.getTypeHandler(NoTypeHandlerInterfaceEnum.class).getClass(),
        "When type handler for interface is not exist, apply default enum type handler.");
    assertSame(SomeInterfaceTypeHandler.class, typeHandlerRegistry.getTypeHandler(SomeEnum.class).getClass());
    assertSame(SomeInterfaceTypeHandler.class, typeHandlerRegistry.getTypeHandler(ExtendingSomeEnum.class).getClass());
    assertSame(SomeInterfaceTypeHandler.class,
        typeHandlerRegistry.getTypeHandler(ImplementingMultiInterfaceSomeEnum.class).getClass());
  }

  @Test
  void shouldRegisterReplaceNullMap() {
    class Address {
    }
    assertFalse(typeHandlerRegistry.hasTypeHandler(Address.class));
    typeHandlerRegistry.register(Address.class, StringTypeHandler.class);
    assertTrue(typeHandlerRegistry.hasTypeHandler(Address.class));
  }

  enum TestEnum {
    ONE,
    TWO
  }

  @Test
  void shouldAutoRegisterEnutmTypeInMultiThreadEnvironment() throws Exception {
    // gh-1820
    ExecutorService executorService = Executors.newCachedThreadPool();
    try {
      for (int iteration = 0; iteration < 2000; iteration++) {
        TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
        List<Future<Boolean>> taskResults = IntStream.range(0, 2)
            .mapToObj(taskIndex -> executorService.submit(() -> {
              return typeHandlerRegistry.hasTypeHandler(TestEnum.class, JdbcType.VARCHAR);
            })).collect(Collectors.toList());
        for (int i = 0; i < taskResults.size(); i++) {
          Future<Boolean> future = taskResults.get(i);
          assertTrue(future.get(), "false is returned at round " + iteration);
        }
      }
    } finally {
      executorService.shutdownNow();
    }
  }
}
