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
package org.apache.ibatis.submitted.typebasedtypehandlerresolution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GenericConstructorMappingTest {
  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  void preservesTypesForAutomaticAndExplicitConstructorMappings(boolean byName) {
    Configuration configuration = new Configuration(
        new Environment("generic-constructors", new JdbcTransactionFactory(),
            new UnpooledDataSource("org.h2.Driver", "jdbc:h2:mem:generic-constructors", "sa", "")));
    configuration.setArgNameBasedConstructorAutoMapping(byName);
    configuration.getTypeHandlerRegistry().register(List.class, ListHandler.class);
    configuration.addMapper(Mapper.class);
    try (var session = new SqlSessionFactoryBuilder().build(configuration).openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      Values expected = new Values(List.of("text"), List.of(42));
      assertEquals(expected, mapper.automatic());
      assertEquals(expected, mapper.explicit());
      assertEquals("text", mapper.generic().payload);
      if (!byName) {
        Overloaded values = mapper.overloaded();
        assertEquals(expected.strings(), values.strings);
        assertEquals(expected.numbers(), values.numbers);
      }
    }
  }

  public static class GenericValue<T> {
    public final T payload;

    public GenericValue(T payload) {
      this.payload = payload;
    }
  }

  public record Values(List<String> strings, List<Integer> numbers) {
  }

  public static class Overloaded {
    public final List<String> strings;
    public final List<Integer> numbers;

    public Overloaded(List<String> strings, List<Integer> numbers) {
      this.strings = strings;
      this.numbers = numbers;
    }

    public Overloaded(int ignored) {
      this(List.of(), List.of());
    }
  }

  interface Mapper {
    @Select("select 'text' as payload")
    GenericValue<String> generic();

    @Select("select 'text' as strings, '42' as numbers")
    Values automatic();

    @Select("select 'text' as strings, '42' as numbers")
    @ConstructorArgs({
        @Arg(name = "strings", column = "strings", javaType = List.class, typeHandler = ListHandler.class),
        @Arg(name = "numbers", column = "numbers", javaType = List.class, typeHandler = ListHandler.class) })
    Values explicit();

    @Select("select 'text' as strings, '42' as numbers")
    Overloaded overloaded();
  }

  public static class ListHandler extends BaseTypeHandler<List<?>> {
    private final Type type;

    public ListHandler(Type type) {
      this.type = type;
    }

    private List<?> read(String value) {
      if (value == null) {
        return null;
      }
      Type element = ((ParameterizedType) type).getActualTypeArguments()[0];
      return element == Integer.class ? List.of(Integer.valueOf(value)) : List.of(value);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<?> value, JdbcType jdbcType) throws SQLException {
      ps.setString(i, value.get(0).toString());
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, String column) throws SQLException {
      return read(rs.getString(column));
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, int column) throws SQLException {
      return read(rs.getString(column));
    }

    @Override
    public List<?> getNullableResult(CallableStatement cs, int column) throws SQLException {
      return read(cs.getString(column));
    }
  }
}
