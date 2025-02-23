/*
 *    Copyright 2009-2025 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LocallySpecifiedTypeHandlerResolutionTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Environment environment = new Environment("test", new JdbcTransactionFactory(),
        new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:localthresolution", null));
    Configuration configuration = new Configuration();
    configuration.setEnvironment(environment);
    configuration.addMapper(LocallySpecifiedHandlerMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    BaseDataTest.runScript(configuration.getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/typebasedtypehandlerresolution/CreateDB.sql");
  }

  @Test
  void specifyHandlerInResultAnnotation() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      User user = mapper.getUser(1);
      assertEquals("garden", user.getStrvalue().getValue());
      assertEquals(31, user.getIntvalue().getValue());
      assertEquals(LocalDate.of(2020, 5, 11), user.getDatevalue());
      assertEquals(LocalDate.of(2020, 5, 9), user.getDatevalue2());
      assertEquals(Arrays.asList("a", "b", "c"), user.getStrings());
      assertEquals(Arrays.asList(1, 3, 5), user.getIntegers());
    }
  }

  @Test
  void test() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      List<User> users = mapper.getAllUsers();
      assertEquals(2, users.size());
    }
  }

  @Test
  void specifyHandlerInParameterMapping() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      {
        User user = new User();
        user.setId(12);
        user.setStrvalue(new FuzzyBean<String>("park"));
        user.setIntvalue(new FuzzyBean<Integer>(7));
        user.setDatevalue(LocalDate.of(2020, 5, 10));
        user.setDatevalue2(LocalDate.of(2020, 5, 8));
        user.setStrings(Arrays.asList("aa", "bb"));
        user.setIntegers(Arrays.asList(11, 22));
        mapper.insertUser(user);
      }
      {
        User user = mapper.getUser(12);
        assertEquals("park", user.getStrvalue().getValue());
        assertEquals(7, user.getIntvalue().getValue());
        assertEquals(LocalDate.of(2020, 5, 10), user.getDatevalue());
        assertEquals(LocalDate.of(2020, 5, 8), user.getDatevalue2());
        assertEquals(Arrays.asList("aa", "bb"), user.getStrings());
        assertEquals(Arrays.asList(11, 22), user.getIntegers());
      }
    }
  }

  @Test
  void specifyHandlerInParameterMappingSingleArgWithParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      {
        User user = new User();
        user.setId(21);
        user.setStrvalue(new FuzzyBean<String>("park"));
        user.setIntvalue(new FuzzyBean<Integer>(7));
        user.setDatevalue(LocalDate.of(2020, 5, 10));
        user.setDatevalue2(LocalDate.of(2020, 5, 8));
        user.setStrings(Arrays.asList("aa", "bb"));
        user.setIntegers(Arrays.asList(11, 22));
        mapper.insertUserWithParam(user);
      }
      {
        User user = mapper.getUser(21);
        assertEquals("park", user.getStrvalue().getValue());
        assertEquals(7, user.getIntvalue().getValue());
        assertEquals(LocalDate.of(2020, 5, 10), user.getDatevalue());
        assertEquals(LocalDate.of(2020, 5, 8), user.getDatevalue2());
        assertEquals(Arrays.asList("aa", "bb"), user.getStrings());
        assertEquals(Arrays.asList(11, 22), user.getIntegers());
      }
    }
  }

  @Test
  void specifyHandlerInParameterMapping_MultiParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      {
        User user = new User();
        user.setId(13);
        user.setStrvalue(new FuzzyBean<String>("well"));
        user.setIntvalue(new FuzzyBean<Integer>(23));
        user.setDatevalue(LocalDate.of(2020, 5, 16));
        user.setDatevalue2(LocalDate.of(2020, 5, 3));
        user.setStrings(Arrays.asList("aa", "bb"));
        user.setIntegers(Arrays.asList(11, 22));
        mapper.insertUserMultiParam(user, "whatevs");
      }
      {
        User user = mapper.getUser(13);
        assertEquals("well", user.getStrvalue().getValue());
        assertEquals(23, user.getIntvalue().getValue());
        assertEquals(LocalDate.of(2020, 5, 16), user.getDatevalue());
        assertEquals(LocalDate.of(2020, 5, 3), user.getDatevalue2());
        assertEquals(Arrays.asList("aa", "bb"), user.getStrings());
        assertEquals(Arrays.asList(11, 22), user.getIntegers());
      }
    }
  }

  @Test
  void specifyHandlerInParameterMapping_ParamMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      {
        mapper.insertUserParamMap(20, new FuzzyBean<String>("well"), new FuzzyBean<Integer>(23),
            LocalDate.of(2020, 5, 16), LocalDate.of(2020, 5, 3), Arrays.asList("aa", "bb"), Arrays.asList(11, 22));
      }
      {
        User user = mapper.getUser(20);
        assertEquals("well", user.getStrvalue().getValue());
        assertEquals(23, user.getIntvalue().getValue());
        assertEquals(LocalDate.of(2020, 5, 16), user.getDatevalue());
        assertEquals(LocalDate.of(2020, 5, 3), user.getDatevalue2());
        assertEquals(Arrays.asList("aa", "bb"), user.getStrings());
        assertEquals(Arrays.asList(11, 22), user.getIntegers());
      }
    }
  }

  @Test
  void shouldResolveParameterTypeInXmlStatementWithMapperMethod() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      {
        User user = new User();
        user.setId(14);
        user.setStrvalue(new FuzzyBean<String>("reservoir"));
        user.setIntvalue(new FuzzyBean<Integer>(71));
        user.setDatevalue(LocalDate.of(2020, 6, 2));
        user.setDatevalue2(LocalDate.of(2020, 4, 3));
        user.setStrings(Arrays.asList("aa", "bb"));
        user.setIntegers(Arrays.asList(11, 22));
        mapper.insertXmlWithMapperMethod(user);
      }
      {
        User user = mapper.getUser(14);
        assertEquals("reservoir", user.getStrvalue().getValue());
        assertEquals(71, user.getIntvalue().getValue());
        assertEquals(LocalDate.of(2020, 6, 2), user.getDatevalue());
        assertEquals(LocalDate.of(2020, 4, 3), user.getDatevalue2());
        assertEquals(Arrays.asList("aa", "bb"), user.getStrings());
        assertEquals(Arrays.asList(11, 22), user.getIntegers());
      }
    }
  }

  @Test
  void shouldResolveParameterTypeInXmlStatementWithMapperMethodMultiParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      {
        User user = new User();
        user.setId(15);
        user.setStrvalue(new FuzzyBean<String>("dentist"));
        user.setIntvalue(new FuzzyBean<Integer>(31));
        user.setDatevalue(LocalDate.of(2020, 6, 3));
        user.setDatevalue2(LocalDate.of(2020, 4, 4));
        user.setStrings(Arrays.asList("aa", "bb"));
        user.setIntegers(Arrays.asList(11, 22));
        mapper.insertXmlWithMapperMethodMultiParam(user, "whatevs");
      }
      {
        User user = mapper.getUser(15);
        assertEquals("dentist", user.getStrvalue().getValue());
        assertEquals(31, user.getIntvalue().getValue());
        assertEquals(LocalDate.of(2020, 6, 3), user.getDatevalue());
        assertEquals(LocalDate.of(2020, 4, 4), user.getDatevalue2());
        assertEquals(Arrays.asList("aa", "bb"), user.getStrings());
        assertEquals(Arrays.asList(11, 22), user.getIntegers());
      }
    }
  }

  @Test
  void shouldResolveParameterTypeInXmlStatementWithMapperMethodSingleArgWithParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
      {
        User user = new User();
        user.setId(22);
        user.setStrvalue(new FuzzyBean<String>("dentist"));
        user.setIntvalue(new FuzzyBean<Integer>(31));
        user.setDatevalue(LocalDate.of(2020, 6, 3));
        user.setDatevalue2(LocalDate.of(2020, 4, 4));
        user.setStrings(Arrays.asList("aa", "bb"));
        user.setIntegers(Arrays.asList(11, 22));
        mapper.insertXmlWithMapperMethodWithParam(user);
      }
      {
        User user = mapper.getUser(22);
        assertEquals("dentist", user.getStrvalue().getValue());
        assertEquals(31, user.getIntvalue().getValue());
        assertEquals(LocalDate.of(2020, 6, 3), user.getDatevalue());
        assertEquals(LocalDate.of(2020, 4, 4), user.getDatevalue2());
        assertEquals(Arrays.asList("aa", "bb"), user.getStrings());
        assertEquals(Arrays.asList(11, 22), user.getIntegers());
      }
    }
  }

  @Test
  void specifyHandlerInXmlResultMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      User user = sqlSession.selectOne(
          "org.apache.ibatis.submitted.typebasedtypehandlerresolution.LocallySpecifiedHandlerMapper.selectXml", 1);
      assertEquals("garden", user.getStrvalue().getValue());
      assertEquals(31, user.getIntvalue().getValue());
      assertEquals(LocalDate.of(2020, 5, 11), user.getDatevalue());
      assertEquals(LocalDate.of(2020, 5, 9), user.getDatevalue2());
      assertEquals(Arrays.asList("a", "b", "c"), user.getStrings());
      assertEquals(Arrays.asList(1, 3, 5), user.getIntegers());
    }
  }

  @Test
  void specifyHandlerInXmlParameter() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      {
        User user = new User();
        user.setId(13);
        user.setStrvalue(new FuzzyBean<String>("pond"));
        user.setIntvalue(new FuzzyBean<Integer>(23));
        user.setDatevalue(LocalDate.of(2020, 5, 13));
        user.setDatevalue2(LocalDate.of(2020, 5, 7));
        user.setStrings(Arrays.asList("aa", "bb"));
        user.setIntegers(Arrays.asList(11, 22));
        sqlSession.insert(
            "org.apache.ibatis.submitted.typebasedtypehandlerresolution.LocallySpecifiedHandlerMapper.insertXml", user);
      }
      {
        LocallySpecifiedHandlerMapper mapper = sqlSession.getMapper(LocallySpecifiedHandlerMapper.class);
        User user = mapper.getUser(13);
        assertEquals("pond", user.getStrvalue().getValue());
        assertEquals(23, user.getIntvalue().getValue());
        assertEquals(LocalDate.of(2020, 5, 13), user.getDatevalue());
        assertEquals(LocalDate.of(2020, 5, 7), user.getDatevalue2());
        assertEquals(Arrays.asList("aa", "bb"), user.getStrings());
        assertEquals(Arrays.asList(11, 22), user.getIntegers());
      }
    }
  }

  @Test
  void specifyHandlerInXmlParameterWithoutParameterType() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      User user = new User();
      user.setId(14);
      user.setStrvalue(new FuzzyBean<String>("pond"));
      user.setIntvalue(new FuzzyBean<Integer>(23));
      user.setDatevalue(LocalDate.of(2020, 5, 13));
      user.setDatevalue2(LocalDate.of(2020, 5, 7));
      // There is no way to obtain info about type parameters.
      assertThatExceptionOfType(PersistenceException.class).isThrownBy(() -> sqlSession.insert(
          "org.apache.ibatis.submitted.typebasedtypehandlerresolution.LocallySpecifiedHandlerMapper.insertXmlWithoutParameterType",
          user)).withMessageContaining("FuzzyBean cannot be cast to class java.lang.String");
    }
  }
}
