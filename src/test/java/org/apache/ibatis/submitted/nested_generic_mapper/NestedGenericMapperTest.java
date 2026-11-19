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
package org.apache.ibatis.submitted.nested_generic_mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;

class NestedGenericMapperTest {

  @Test
  void shouldMapRowsThroughNestedGenericReturnType() throws Exception {
    UnpooledDataSource dataSource = new UnpooledDataSource("org.h2.Driver",
        "jdbc:h2:mem:nested_generic_mapper", "sa", "");
    try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
      statement.execute("create table users (id integer primary key, name varchar(100))");
      statement.execute("create table products (id integer primary key, name varchar(100))");
      statement.execute("insert into users values (1, 'Ada'), (2, 'Linus'), (3, 'Grace')");
      statement.execute("insert into products values (1, 'Keyboard'), (2, 'Monitor')");

      Configuration configuration = new Configuration(
          new Environment("test", new JdbcTransactionFactory(), dataSource));
      configuration.addMapper(UserMapper.class);
      configuration.addMapper(ProductMapper.class);
      SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(configuration);
      try (SqlSession session = factory.openSession()) {
        List<User> users = session.getMapper(UserMapper.class).select(2);
        assertEquals(2, users.size());
        assertEquals(2, users.get(0).id);
        assertEquals("Linus", users.get(0).name);
        assertEquals(3, users.get(1).id);
        assertEquals("Grace", users.get(1).name);

        List<Product> products = session.getMapper(ProductMapper.class).select(1);
        assertEquals(2, products.size());
        assertEquals(1, products.get(0).id);
        assertEquals("Keyboard", products.get(0).name);
        assertEquals(2, products.get(1).id);
        assertEquals("Monitor", products.get(1).name);
        assertTrue(session.getMapper(UserMapper.class).select(99).isEmpty());
      }
    }
  }

  interface Query<R> {
    @SelectProvider(type = SqlProvider.class, method = "select")
    R select(@Param("minId") int minId);
  }

  interface ListQuery<T> extends Query<List<T>> {
  }

  interface UserMapper extends ListQuery<User> {
  }

  interface ProductMapper extends ListQuery<Product> {
  }

  public static class SqlProvider {
    public static String select(ProviderContext context) {
      String table = context.getMapperType() == UserMapper.class ? "users" : "products";
      return "select id, name from " + table + " where id >= #{minId} order by id";
    }
  }

  static class User {
    public int id;
    public String name;
  }

  static class Product {
    public int id;
    public String name;
  }
}
