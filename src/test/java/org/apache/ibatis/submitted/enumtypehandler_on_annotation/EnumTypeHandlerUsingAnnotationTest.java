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
package org.apache.ibatis.submitted.enumtypehandler_on_annotation;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for type handler of enum using annotations.
 *
 * @since #444
 *
 * @author Kazuki Shimizu
 *
 * @see org.apache.ibatis.annotations.Arg
 * @see org.apache.ibatis.annotations.Result
 * @see org.apache.ibatis.annotations.TypeDiscriminator
 */
class EnumTypeHandlerUsingAnnotationTest {

    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;

    @BeforeAll
    static void initDatabase() throws Exception {
        try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/enumtypehandler_on_annotation/mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            sqlSessionFactory.getConfiguration().getMapperRegistry().addMapper(PersonMapper.class);
        }

        BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
                "org/apache/ibatis/submitted/enumtypehandler_on_annotation/CreateDB.sql");
    }

    @BeforeEach
    void openSqlSession() {
        this.sqlSession = sqlSessionFactory.openSession();
    }

    @AfterEach
    void closeSqlSession() {
        sqlSession.close();
    }

    @Test
    void testForArg() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            Person person = personMapper.findOneUsingConstructor(1);
            assertThat(person.getId()).isEqualTo(1);
            assertThat(person.getFirstName()).isEqualTo("John");
            assertThat(person.getLastName()).isEqualTo("Smith");
            assertThat(person.getPersonType()).isEqualTo(Person.PersonType.PERSON); // important
        }
        {
            Person employee = personMapper.findOneUsingConstructor(2);
            assertThat(employee.getId()).isEqualTo(2);
            assertThat(employee.getFirstName()).isEqualTo("Mike");
            assertThat(employee.getLastName()).isEqualTo("Jordan");
            assertThat(employee.getPersonType()).isEqualTo(Person.PersonType.EMPLOYEE); // important
        }
    }

    @Test
    void testForResult() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            Person person = personMapper.findOneUsingSetter(1);
            assertThat(person.getId()).isEqualTo(1);
            assertThat(person.getFirstName()).isEqualTo("John");
            assertThat(person.getLastName()).isEqualTo("Smith");
            assertThat(person.getPersonType()).isEqualTo(Person.PersonType.PERSON); // important
        }
        {
            Person employee = personMapper.findOneUsingSetter(2);
            assertThat(employee.getId()).isEqualTo(2);
            assertThat(employee.getFirstName()).isEqualTo("Mike");
            assertThat(employee.getLastName()).isEqualTo("Jordan");
            assertThat(employee.getPersonType()).isEqualTo(Person.PersonType.EMPLOYEE); // important
        }
    }

    @Test
    void testForTypeDiscriminator() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            Person person = personMapper.findOneUsingTypeDiscriminator(1);
            assertThat(person.getClass()).isEqualTo(Person.class); // important
            assertThat(person.getId()).isEqualTo(1);
            assertThat(person.getFirstName()).isEqualTo("John");
            assertThat(person.getLastName()).isEqualTo("Smith");
            assertThat(person.getPersonType()).isEqualTo(Person.PersonType.PERSON);
        }
        {
            Person employee = personMapper.findOneUsingTypeDiscriminator(2);
            assertThat(employee.getClass()).isEqualTo(Employee.class); // important
            assertThat(employee.getId()).isEqualTo(2);
            assertThat(employee.getFirstName()).isEqualTo("Mike");
            assertThat(employee.getLastName()).isEqualTo("Jordan");
            assertThat(employee.getPersonType()).isEqualTo(Person.PersonType.EMPLOYEE);
        }
    }

}
