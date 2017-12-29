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
package org.apache.ibatis.binding;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import java.util.HashMap;

public class MapperMethodParamTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    DataSource dataSource = BaseDataTest.createUnpooledDataSource(BaseDataTest.BLOG_PROPERTIES);
    BaseDataTest.runScript(dataSource, "org/apache/ibatis/binding/paramtest-schema.sql");
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test
  public void parameterNameIsSizeAndTypeIsLong() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      mapper.insert("foo", Long.MAX_VALUE);
      assertThat(mapper.selectSize("foo")).isEqualTo(Long.MAX_VALUE);
    } finally {
      session.close();
    }
  }

  @Test
  public void parameterNameIsSizeUsingHashMap() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      HashMap<String, Object> params = new HashMap<String, Object>();
      params.put("id", "foo");
      params.put("size", Long.MAX_VALUE);
      Mapper mapper = session.getMapper(Mapper.class);
      mapper.insertUsingHashMap(params);
      assertThat(mapper.selectSize("foo")).isEqualTo(Long.MAX_VALUE);
    } finally {
      session.close();
    }
  }

  interface Mapper {
    @Insert("insert into param_test (id, size) values(#{id}, #{size})")
    void insert(@Param("id") String id, @Param("size") long size);

    @Insert("insert into param_test (id, size) values(#{id}, #{size})")
    void insertUsingHashMap(HashMap<String, Object> params);

    @Select("select size from param_test where id = #{id}")
    long selectSize(@Param("id") String id);
  }

}
