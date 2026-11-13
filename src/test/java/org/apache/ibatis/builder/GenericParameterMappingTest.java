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
package org.apache.ibatis.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.TypeReference;
import org.junit.jupiter.api.Test;

class GenericParameterMappingTest {
  public static class GenericHandler extends BaseTypeHandler<Object> {
    final Type type;

    public GenericHandler(Type type) {
      this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int index, Object value, JdbcType jdbcType)
        throws SQLException {
      ps.setObject(index, value);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String name) throws SQLException {
      return rs.getObject(name);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int index) throws SQLException {
      return rs.getObject(index);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int index) throws SQLException {
      return cs.getObject(index);
    }
  }

  @Test
  void preservesDistinctFullTypesInOneStatementIncludingNullValues() {
    Configuration configuration = new Configuration();
    String handler = GenericHandler.class.getName();
    String sql = "select #{parameters.p1,javaType=java.util.Map<java.lang.String,java.util.List<java.lang.Integer>>,typeHandler="
        + handler + "}, #{parameters.p2,javaType=java.util.List<java.lang.String>,typeHandler=" + handler + "}";
    Map<String, Object> values = new HashMap<>();
    values.put("p1", null);
    values.put("p2", List.of());
    BoundSql bound = new RawSqlSource(configuration, sql, Map.class).getBoundSql(Map.of("parameters", values));
    assertEquals(new TypeReference<Map<String, List<Integer>>>() {
    }.getRawType(), ((GenericHandler) bound.getParameterMappings().get(0).getTypeHandler()).type);
    assertEquals(new TypeReference<List<String>>() {
    }.getRawType(), ((GenericHandler) bound.getParameterMappings().get(1).getTypeHandler()).type);
  }

  @Test
  void retainsLegacyClassAndNoArgConstructorsForGenericTypes() {
    Type type = new TypeReference<List<String>>() {
    }.getRawType();
    TypeHandlerRegistry registry = new TypeHandlerRegistry();
    assertEquals(List.class, ((ClassHandler) registry.getInstance(type, ClassHandler.class)).type);
    assertInstanceOf(NoArgHandler.class, registry.getInstance(type, NoArgHandler.class));
  }

  @Test
  void doesNotReuseRawTypeHandlerInstanceForDifferentGenericArguments() {
    TypeHandlerRegistry registry = new TypeHandlerRegistry();
    Type strings = new TypeReference<List<String>>() {
    }.getRawType();
    Type integers = new TypeReference<List<Integer>>() {
    }.getRawType();
    registry.register((Type) List.class, JdbcType.VARCHAR, new GenericHandler(strings));
    GenericHandler handler = (GenericHandler) registry.getTypeHandler(integers, JdbcType.VARCHAR, GenericHandler.class);
    assertEquals(integers, handler.type);
  }

  @Test
  void reusesRegisteredNoArgHandlersForExplicitMappings() {
    TypeHandlerRegistry registry = new TypeHandlerRegistry();
    NoArgHandler handler = new NoArgHandler();
    registry.register((Type) List.class, JdbcType.VARCHAR, handler);
    Type type = new TypeReference<List<String>>() {
    }.getRawType();
    assertSame(handler, registry.getTypeHandler(type, JdbcType.VARCHAR, NoArgHandler.class));
    assertSame(handler, registry.getTypeHandler(type, JdbcType.VARCHAR, null));
  }

  @Test
  void reusesRegisteredClassHandlersForRawTypes() {
    TypeHandlerRegistry registry = new TypeHandlerRegistry();
    ClassHandler handler = new ClassHandler(List.class);
    registry.register((Type) List.class, JdbcType.VARCHAR, handler);
    assertSame(handler, registry.getTypeHandler(List.class, JdbcType.VARCHAR, ClassHandler.class));
    Type type = new TypeReference<List<String>>() {
    }.getRawType();
    assertEquals(List.class, ((ClassHandler) registry.getTypeHandler(type, JdbcType.VARCHAR, ClassHandler.class)).type);
  }

  @Test
  void appliesRawClassFallbackToAutomaticallyMatchedHandlers() {
    TypeHandlerRegistry registry = new TypeHandlerRegistry();
    Type type = new TypeReference<List<String>>() {
    }.getRawType();
    registry.register(type, ClassHandler.class);
    assertEquals(List.class, ((ClassHandler) registry.getTypeHandler(type, JdbcType.VARCHAR)).type);
  }

  @Test
  void rejectsUnboundVariablesForAutomaticallyMatchedHandlers() throws Exception {
    TypeHandlerRegistry registry = new TypeHandlerRegistry();
    registry.register(new TypeReference<List<String>>() {
    }.getRawType(), GenericHandler.class);
    Type unresolved = Unbound.class.getDeclaredField("values").getGenericType();
    TypeException failure = assertThrows(TypeException.class,
        () -> registry.getTypeHandler(unresolved, JdbcType.VARCHAR));
    assertTrue(failure.getMessage().contains("T"));
    assertTrue(failure.getMessage().contains(GenericHandler.class.getName()));
  }

  @Test
  void prefersTypeConstructorForBothExplicitAndAutomaticMappings() {
    TypeHandlerRegistry registry = new TypeHandlerRegistry();
    Type type = new TypeReference<List<String>>() {
    }.getRawType();
    assertEquals(type, ((DualConstructorHandler) registry.getInstance(type, DualConstructorHandler.class)).type);
    registry.register(type, DualConstructorHandler.class);
    assertEquals(type, ((DualConstructorHandler) registry.getTypeHandler(type, JdbcType.VARCHAR)).type);
  }

  @Test
  void preservesBoundVariablesAndRejectsUnboundVariablesForResultProperties() {
    Reflector bound = new Reflector(Bound.class);
    assertEquals(new TypeReference<List<String>>() {
    }.getRawType(), bound.getGenericSetterType("values").getKey());
    Reflector unbound = new Reflector(Unbound.class);
    Type unresolved = unbound.getGenericSetterType("values").getKey();
    assertThrows(TypeException.class, () -> new TypeHandlerRegistry().getInstance(unresolved, GenericHandler.class));
    MapperBuilderAssistant assistant = new MapperBuilderAssistant(new Configuration(), "generic-test");
    BuilderException failure = assertThrows(BuilderException.class,
        () -> assistant.buildResultMapping(Unbound.class, "values", "payload", null, JdbcType.VARCHAR, null, null, null,
            null, GenericHandler.class, List.of(), null, null, false));
    assertTrue(failure.getMessage().contains("values"));
    assertTrue(failure.getMessage().contains("T"));
  }

  public static class Unbound<T> {
    public List<T> values;
  }

  public static class Bound extends Unbound<String> {
  }

  public static class ClassHandler extends GenericHandler {
    public ClassHandler(Class<?> type) {
      super(type);
    }
  }

  public static class DualConstructorHandler extends GenericHandler {
    public DualConstructorHandler(Type type) {
      super(type);
    }

    public DualConstructorHandler(Class<?> type) {
      super(type);
    }
  }

  public static class NoArgHandler extends GenericHandler {
    public NoArgHandler() {
      super(Object.class);
    }
  }
}
