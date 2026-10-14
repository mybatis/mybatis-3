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
package org.apache.ibatis.submitted.reuse_statement_settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Statement;
import java.util.Iterator;
import java.util.UUID;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ReuseStatementSettingsTest {

  @ParameterizedTest
  @CsvSource({ "SIMPLE,false", "SIMPLE,true", "REUSE,false", "REUSE,true" })
  void shouldHonorGeneratedKeysForEachMappedStatement(ExecutorType executorType, boolean generatedKeysFirst)
      throws Exception {
    try (SqlSession session = openSession(executorType)) {
      Mapper mapper = session.getMapper(Mapper.class);
      Item withoutKey = new Item("without key");
      Item withKey = new Item("with key");
      if (generatedKeysFirst) {
        assertEquals(1, mapper.insertWithKey(withKey));
        assertEquals(1, mapper.insertWithoutKey(withoutKey));
      } else {
        assertEquals(1, mapper.insertWithoutKey(withoutKey));
        assertEquals(1, mapper.insertWithKey(withKey));
      }
      assertNull(withoutKey.id);
      assertNotNull(withKey.id);
      assertEquals(mapper.selectId(withKey.name), withKey.id);
    }
  }

  @ParameterizedTest
  @CsvSource({ "SIMPLE,false", "SIMPLE,true", "REUSE,false", "REUSE,true" })
  void shouldHonorStatementTypeForEachMappedStatement(ExecutorType executorType, boolean preparedFirst)
      throws Exception {
    try (SqlSession session = openSession(executorType)) {
      Mapper mapper = session.getMapper(Mapper.class);
      if (preparedFirst) {
        assertEquals(42, mapper.selectPrepared());
        assertEquals(42, mapper.selectStatement());
      } else {
        assertEquals(42, mapper.selectStatement());
        assertEquals(42, mapper.selectPrepared());
      }
    }
  }

  @ParameterizedTest
  @EnumSource(value = ExecutorType.class, names = { "SIMPLE", "REUSE" })
  void shouldHonorStatementTypeForCursor(ExecutorType executorType) throws Exception {
    try (SqlSession session = openSession(executorType)) {
      Mapper mapper = session.getMapper(Mapper.class);
      assertEquals(42, mapper.selectStatement());
      try (Cursor<Integer> cursor = mapper.selectCursor()) {
        Iterator<Integer> iterator = cursor.iterator();
        assertEquals(42, iterator.next());
      }
    }
  }

  private SqlSession openSession(ExecutorType executorType) throws Exception {
    UnpooledDataSource dataSource = new UnpooledDataSource("org.h2.Driver", "jdbc:h2:mem:" + UUID.randomUUID(), "sa",
        "");
    Configuration configuration = new Configuration(new Environment("test", new JdbcTransactionFactory(), dataSource));
    configuration.setLocalCacheScope(LocalCacheScope.STATEMENT);
    configuration.addMapper(Mapper.class);
    SqlSession session = new SqlSessionFactoryBuilder().build(configuration).openSession(executorType);
    try (Statement statement = session.getConnection().createStatement()) {
      statement.execute("create table items (id bigint generated always as identity primary key, name varchar(40))");
    }
    return session;
  }

  interface Mapper {
    @Insert("insert into items(name) values(#{name})")
    int insertWithoutKey(Item item);

    @Insert("insert into items(name) values(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertWithKey(Item item);

    @Select("select id from items where name = #{name}")
    Long selectId(String name);

    @Select("select 42")
    @Options(statementType = StatementType.STATEMENT)
    int selectStatement();

    @Select("select 42")
    int selectPrepared();

    @Select("select 42")
    Cursor<Integer> selectCursor();
  }

  static class Item {
    Long id;
    String name;

    Item(String name) {
      this.name = name;
    }
  }
}
