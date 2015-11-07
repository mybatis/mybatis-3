/**
 *    Copyright 2009-2015 the original author or authors.
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

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;

/**
 * Tests for type handler of enum using annotations.
 *
 * @see org.apache.ibatis.annotations.Arg
 * @see org.apache.ibatis.annotations.Result
 * @see org.apache.ibatis.annotations.TypeDiscriminator
 *
 * @author Kazuki Shimizu
 * @since #444
 */
public class EnumTypeHandlerUsingAnnotationTest {

    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;

    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:enumtypehandler_on_annotation", "sa", "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/enumtypehandler_on_annotation/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/enumtypehandler_on_annotation/mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            sqlSessionFactory.getConfiguration().getMapperRegistry().addMapper(PersonMapper.class);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Before
    public void openSqlSession() {
        this.sqlSession = sqlSessionFactory.openSession();
    }

    @After
    public void closeSqlSession() {
        sqlSession.close();
    }

    @Test
    public void testForArg() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            Person person = personMapper.findOneUsingConstructor(1);
            assertThat(person.getId(), is(1));
            assertThat(person.getFirstName(), is("John"));
            assertThat(person.getLastName(), is("Smith"));
            assertThat(person.getPersonType(), is(Person.PersonType.PERSON)); // important
        }
        {
            Person employee = personMapper.findOneUsingConstructor(2);
            assertThat(employee.getId(), is(2));
            assertThat(employee.getFirstName(), is("Mike"));
            assertThat(employee.getLastName(), is("Jordan"));
            assertThat(employee.getPersonType(), is(Person.PersonType.EMPLOYEE)); // important
        }
    }

    @Test
    public void testForResult() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            Person person = personMapper.findOneUsingSetter(1);
            assertThat(person.getId(), is(1));
            assertThat(person.getFirstName(), is("John"));
            assertThat(person.getLastName(), is("Smith"));
            assertThat(person.getPersonType(), is(Person.PersonType.PERSON)); // important
        }
        {
            Person employee = personMapper.findOneUsingSetter(2);
            assertThat(employee.getId(), is(2));
            assertThat(employee.getFirstName(), is("Mike"));
            assertThat(employee.getLastName(), is("Jordan"));
            assertThat(employee.getPersonType(), is(Person.PersonType.EMPLOYEE)); // important
        }
    }

    @Test
    public void testForTypeDiscriminator() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            Person person = personMapper.findOneUsingTypeDiscriminator(1);
            assertTrue(person.getClass() == Person.class); // important
            assertThat(person.getId(), is(1));
            assertThat(person.getFirstName(), is("John"));
            assertThat(person.getLastName(), is("Smith"));
            assertThat(person.getPersonType(), is(Person.PersonType.PERSON));
        }
        {
            Person employee = personMapper.findOneUsingTypeDiscriminator(2);
            assertTrue(employee.getClass() == Employee.class); // important
            assertThat(employee.getId(), is(2));
            assertThat(employee.getFirstName(), is("Mike"));
            assertThat(employee.getLastName(), is("Jordan"));
            assertThat(employee.getPersonType(), is(Person.PersonType.EMPLOYEE));
        }
    }

}
