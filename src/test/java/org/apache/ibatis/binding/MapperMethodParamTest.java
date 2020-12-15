/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.binding;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapperMethodParamTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setup() throws Exception {
    DataSource dataSource = BaseDataTest.createUnpooledDataSource(BaseDataTest.BLOG_PROPERTIES);
    BaseDataTest.runScript(dataSource, "org/apache/ibatis/binding/paramtest-schema.sql");
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test
  void parameterNameIsSizeAndTypeIsLong() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      mapper.insert("foo", Long.MAX_VALUE);
      assertThat(mapper.selectSize("foo")).isEqualTo(Long.MAX_VALUE);
    }
  }

  @Test
  void parameterNameIsSizeUsingHashMap() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      HashMap<String, Object> params = new HashMap<>();
      params.put("id", "foo");
      params.put("size", Long.MAX_VALUE);
      Mapper mapper = session.getMapper(Mapper.class);
      mapper.insertUsingHashMap(params);
      assertThat(mapper.selectSize("foo")).isEqualTo(Long.MAX_VALUE);
    }
  }

  /**
   * Using nested lists as parameters
   * <br/>
   * Test whether {@link PropertyTokenizer#PropertyTokenizer(java.lang.String)} be parsed correctly
   */
  @Test
  void parameterNameIsSizeUsingNestedList() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      // Constructed parameters
      List<List<List<Long>>> listOuter = new ArrayList<>();
      List<List<Long>> listMiddle = new ArrayList<>();
      List<Long> listInner = new ArrayList<>();
      listOuter.add(listMiddle);
      listMiddle.add(listInner);
      listInner.add(Long.MAX_VALUE);

      Mapper mapper = session.getMapper(Mapper.class);
      mapper.insertSizeUsingNestedList("foo", listOuter);
      assertThat(mapper.selectSize("foo")).isEqualTo(Long.MAX_VALUE);
    }
  }

  interface Mapper {
    @Insert("insert into param_test (id, size) values(#{id}, #{size})")
    void insert(@Param("id") String id, @Param("size") long size);

    @Insert("insert into param_test (id, size) values(#{id}, #{size})")
    void insertUsingHashMap(HashMap<String, Object> params);

    @Insert("insert into param_test (id, size) values(#{id}, #{list[0][0][0]})")
    long insertSizeUsingNestedList(@Param("id") String id, @Param("list") List<List<List<Long>>> list);

    @Select("select size from param_test where id = #{id}")
    long selectSize(@Param("id") String id);
  }

}
