/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.xml_external_ref;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class XmlExternalRefTest {

  @Test
  void testCrossReferenceXmlConfig() throws Exception {
    testCrossReference(getSqlSessionFactoryXmlConfig());
  }

  @Test
  void testCrossReferenceJavaConfig() throws Exception {
    testCrossReference(getSqlSessionFactoryJavaConfig());
  }

  @Test
  void testFailFastOnBuildAll() {
    Configuration configuration = new Configuration();
    try {
      configuration.addMapper(InvalidMapper.class);
    } catch (Exception e) {
      fail("No exception should be thrown before parsing statement nodes.");
    }
    Assertions.assertThrows(BuilderException.class, configuration::getMappedStatementNames);
  }

  @Test
  void testFailFastOnBuildAllWithInsert() {
    Configuration configuration = new Configuration();
    try {
      configuration.addMapper(InvalidWithInsertMapper.class);
      configuration.addMapper(InvalidMapper.class);
    } catch (Exception e) {
      fail("No exception should be thrown before parsing statement nodes.");
    }
    Assertions.assertThrows(BuilderException.class, configuration::getMappedStatementNames);
  }

  @Test
  void testMappedStatementCache() throws Exception {
    try (Reader configReader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/xml_external_ref/MapperConfig.xml")) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);

      Configuration configuration = sqlSessionFactory.getConfiguration();
      configuration.getMappedStatementNames();

      MappedStatement selectPetStatement = configuration
          .getMappedStatement("org.apache.ibatis.submitted.xml_external_ref.PetMapper.select");
      MappedStatement selectPersonStatement = configuration
          .getMappedStatement("org.apache.ibatis.submitted.xml_external_ref.PersonMapper.select");
      Cache cache = selectPetStatement.getCache();
      assertEquals("org.apache.ibatis.submitted.xml_external_ref.PetMapper", cache.getId());
      assertSame(cache, selectPersonStatement.getCache());
    }
  }

  private void testCrossReference(SqlSessionFactory sqlSessionFactory) {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person person = personMapper.select(1);
      assertEquals((Integer) 1, person.getId());
      assertEquals(2, person.getPets().size());
      assertEquals((Integer) 2, person.getPets().get(1).getId());

      Pet pet = personMapper.selectPet(1);
      assertEquals(Integer.valueOf(1), pet.getId());

      PetMapper petMapper = sqlSession.getMapper(PetMapper.class);
      Pet pet2 = petMapper.select(3);
      assertEquals((Integer) 3, pet2.getId());
      assertEquals((Integer) 2, pet2.getOwner().getId());
    }
  }

  private SqlSessionFactory getSqlSessionFactoryXmlConfig() throws Exception {
    try (Reader configReader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/xml_external_ref/MapperConfig.xml")) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);

      initDb(sqlSessionFactory);

      return sqlSessionFactory;
    }
  }

  private SqlSessionFactory getSqlSessionFactoryJavaConfig() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:xmlextref", null));
    configuration.setEnvironment(environment);
    configuration.addMapper(PersonMapper.class);
    configuration.addMapper(PetMapper.class);

    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    initDb(sqlSessionFactory);

    return sqlSessionFactory;
  }

  private static void initDb(SqlSessionFactory sqlSessionFactory) throws IOException, SQLException {
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/xml_external_ref/CreateDB.sql");
  }

}
