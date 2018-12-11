/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.submitted.ognl_enum;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.ognl_enum.Person.Type;
import org.apache.ibatis.submitted.ognl_enum.PersonMapper.PersonType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class EnumWithOgnlTest {
    
    private static SqlSessionFactory sqlSessionFactory;
    
    @BeforeClass
    public static void initDatabase() throws Exception {
        try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/ognl_enum/ibatisConfig.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }

        BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
                "org/apache/ibatis/submitted/ognl_enum/CreateDB.sql");
    }
    
    @Test
    public void testEnumWithOgnl() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            List<Person> persons = personMapper.selectAllByType(null);
            Assert.assertEquals("Persons must contain 3 persons", 3, persons.size());
        }
    }

  @Test
    public void testEnumWithOgnlDirector() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            List<Person> persons = personMapper.selectAllByType(Person.Type.DIRECTOR);
            Assert.assertEquals("Persons must contain 1 persons", 1, persons.size());
        }
    }

    @Test
    public void testEnumWithOgnlDirectorNameAttribute() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            List<Person> persons = personMapper.selectAllByTypeNameAttribute(Person.Type.DIRECTOR);
            Assert.assertEquals("Persons must contain 1 persons", 1, persons.size());
        }
    }

  @Test
    public void testEnumWithOgnlDirectorWithInterface() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            List<Person> persons = personMapper.selectAllByTypeWithInterface(new PersonType() {
                @Override
                public Type getType() {
                    return Person.Type.DIRECTOR;
                }
            });
            Assert.assertEquals("Persons must contain 1 persons", 1, persons.size());
        }
    }
    @Test
    public void testEnumWithOgnlDirectorNameAttributeWithInterface() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            List<Person> persons = personMapper.selectAllByTypeNameAttributeWithInterface(new PersonType() {
                @Override
                public Type getType() {
                    return Person.Type.DIRECTOR;
                }
            });
            Assert.assertEquals("Persons must contain 1 persons", 1, persons.size());
        }
    }
}
