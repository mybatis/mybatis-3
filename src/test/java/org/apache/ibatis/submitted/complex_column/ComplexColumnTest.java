/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.complex_column;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;

public class ComplexColumnTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    public static void initDatabase() throws Exception {
        try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/complex_column/ibatisConfig.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }

        BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
                "org/apache/ibatis/submitted/complex_column/CreateDB.sql");
    }

    @Test
    public void testWithoutComplex() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.getWithoutComplex(2l);
            Assertions.assertNotNull(person, "person must not be null");
            Assertions.assertEquals("Christian", person.getFirstName());
            Assertions.assertEquals("Poitras", person.getLastName());
            Person parent = person.getParent();
            Assertions.assertNotNull(parent, "parent must not be null");
            Assertions.assertEquals("John", parent.getFirstName());
            Assertions.assertEquals("Smith", parent.getLastName());
        }
    }

    @Test
    public void testWithComplex() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.getWithComplex(2l);
            Assertions.assertNotNull(person, "person must not be null");
            Assertions.assertEquals("Christian", person.getFirstName());
            Assertions.assertEquals("Poitras", person.getLastName());
            Person parent = person.getParent();
            Assertions.assertNotNull(parent, "parent must not be null");
            Assertions.assertEquals("John", parent.getFirstName());
            Assertions.assertEquals("Smith", parent.getLastName());
        }
    }

    @Test
    public void testWithComplex2() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.getWithComplex2(2l);
            Assertions.assertNotNull(person, "person must not be null");
            Assertions.assertEquals("Christian", person.getFirstName());
            Assertions.assertEquals("Poitras", person.getLastName());
            Person parent = person.getParent();
            Assertions.assertNotNull(parent, "parent must not be null");
            Assertions.assertEquals("John", parent.getFirstName());
            Assertions.assertEquals("Smith", parent.getLastName());
        }
    }

    @Test
    public void testWithComplex3() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.getWithComplex3(2l);
            Assertions.assertNotNull(person, "person must not be null");
            Assertions.assertEquals("Christian", person.getFirstName());
            Assertions.assertEquals("Poitras", person.getLastName());
            Person parent = person.getParent();
            Assertions.assertNotNull(parent, "parent must not be null");
            Assertions.assertEquals("John", parent.getFirstName());
            Assertions.assertEquals("Smith", parent.getLastName());
        }
    }

    @Test
    public void testWithComplex4() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
          Person criteria = new Person();
          criteria.setFirstName("Christian");
          criteria.setLastName("Poitras");
          Person person = personMapper.getParentWithComplex(criteria);
          Assertions.assertNotNull(person, "person must not be null");
          Assertions.assertEquals("Christian", person.getFirstName());
          Assertions.assertEquals("Poitras", person.getLastName());
          Person parent = person.getParent();
          Assertions.assertNotNull(parent, "parent must not be null");
          Assertions.assertEquals("John", parent.getFirstName());
          Assertions.assertEquals("Smith", parent.getLastName());
      }
    }

    @Test
    public void testWithParamAttributes() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.getComplexWithParamAttributes(2l);
            Assertions.assertNotNull(person, "person must not be null");
            Assertions.assertEquals("Christian", person.getFirstName());
            Assertions.assertEquals("Poitras", person.getLastName());
            Person parent = person.getParent();
            Assertions.assertNotNull(parent, "parent must not be null");
            Assertions.assertEquals("John", parent.getFirstName());
            Assertions.assertEquals("Smith", parent.getLastName());
        }
    }
}
