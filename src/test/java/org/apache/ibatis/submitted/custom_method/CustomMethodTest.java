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
package org.apache.ibatis.submitted.custom_method;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.*;
import org.hamcrest.core.Is;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for custom method implementation.
 *
 * @author Kazuki Shimizu
 */
public class CustomMethodTest {

    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:custom_method", "sa", "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/custom_method/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/custom_method/mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            sqlSessionFactory.getConfiguration().getMapperRegistry().addMapper(PersonMapper.class);
            sqlSessionFactory.getConfiguration().getMapperRegistry().addMapper(EmployeeMapper.class);
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
    public void testArgIsNone() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        Page<Person> persons = personMapper.findPage();
        assertThat(persons.getTotal(), is(2L));
        assertThat(persons.getContent().get(0).getId(),is(1));
        assertThat(persons.getContent().get(0).getFirstName(),is("John"));
        assertThat(persons.getContent().get(0).getLastName(),is("Smith"));
        assertThat(persons.getContent().get(1).getId(),is(2));
    }

    @Test
    public void testArgIsRowBounds() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            RowBounds rowBounds = new RowBounds(0, 1);
            Page<Person> persons = personMapper.findPage(rowBounds);
            assertThat(persons.getTotal(), is(2L));
            assertThat(persons.getContent().get(0).getId(), is(1));
            assertThat(persons.getContent().get(0).getFirstName(), is("John"));
            assertThat(persons.getContent().get(0).getLastName(), is("Smith"));
        }
        {
            RowBounds rowBounds = new RowBounds(1, 1);
            Page<Person> persons = personMapper.findPage(rowBounds);
            assertThat(persons.getTotal(), is(2L));
            assertThat(persons.getContent().get(0).getId(), is(2));
            assertThat(persons.getContent().get(0).getFirstName(), is("Mike"));
            assertThat(persons.getContent().get(0).getLastName(), is("Jordan"));
        }
    }

    @Test
    public void testArgIsAnyObject() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            RowBounds rowBounds = new RowBounds(0, 1);
            Page<Person> persons = personMapper.findPageByName("Joh", rowBounds);
            assertThat(persons.getTotal(), is(1L));
            assertThat(persons.getContent().get(0).getId(), is(1));
        }
        {
            RowBounds rowBounds = new RowBounds(0, 1);
            Page<Person> persons = personMapper.findPageByName("Jor", rowBounds);
            assertThat(persons.getTotal(), is(1L));
            assertThat(persons.getContent().get(0).getId(), is(2));
        }
    }

    @Test
    public void testArgIsResultHandlerOnCustomizeImplementation() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        final List<Person> persons = new ArrayList<Person>();
        ResultHandler<Person> resultHandler = new ResultHandler<Person>() {
            @Override
            public void handleResult(ResultContext<? extends Person> resultContext) {
                persons.add(resultContext.getResultObject());
            }
        };
        RowBounds rowBounds = new RowBounds(1,1);
        long total = personMapper.collectPage(rowBounds, resultHandler);
        assertThat(total, is(2L));
        assertThat(persons.size(), is(1));
        assertThat(persons.get(0).getId(), is(2));
    }


    @Test
    public void testDelegatingMapperInterface() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        RowBounds rowBounds = new RowBounds(1,1);
        Page<Person> persons = personMapper.findPageDelegatingMapperInterface(rowBounds);
        assertThat(persons.getTotal(), is(2L));
        assertThat(persons.getContent().get(0).getId(), is(2));
    }

    @Test
    public void testErrorForNotImplementDefaultClass() {
        EmployeeMapper employeeMapper = sqlSession.getMapper(EmployeeMapper.class);

        expectedException.expect(BindingException.class);
        expectedException.expectMessage("Custom method does not found.");
        expectedException.expectCause(Is.isA(ClassNotFoundException.class));

        employeeMapper.countNotFoundDefaultImplementationType();
    }

    @Test
    public void testErrorForNotImplementCustomMethod() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);

        expectedException.expect(BindingException.class);
        expectedException.expectMessage("Custom method does not found.");
        expectedException.expectCause(Is.isA(NoSuchMethodException.class));

        personMapper.findPageNotImplementCustomMethod();
    }

    @Test
    public void testErrorForHandlingRuntimeException() {

        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);

        expectedException.expect(PersistenceException.class);
        expectedException.expectMessage("Test for handling a runtime exception.");
        expectedException.expectCause(Is.isA(SQLException.class));

        personMapper.findPageThrowRuntimeException();
    }

    @Test
    public void testErrorForHandlingError() {

        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);

        expectedException.expect(OutOfMemoryError.class);
        expectedException.expectMessage("Test for handling an error.");

        personMapper.findPageThrowError();
    }

    @Test
    public void testErrorForHandlingException() {

        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);

        expectedException.expect(BindingException.class);
        expectedException.expectMessage("Custom method invocation is failed.");
        expectedException.expectCause(Is.isA(IllegalAccessException.class));

        personMapper.findPageThrowException();
    }

    /**
     * Test for normal mapper methods
     */
    @Test
    public void testCallMapperMethod() {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        {
            List<Person> persons = personMapper.findList(new RowBounds());
            assertThat(persons.size(), is(2));
            assertThat(persons.get(0).getId(), is(1));
            assertThat(persons.get(1).getId(), is(2));
        }
        {
            long count = personMapper.countAll();
            assertThat(count, is(2L));
        }
    }

}
