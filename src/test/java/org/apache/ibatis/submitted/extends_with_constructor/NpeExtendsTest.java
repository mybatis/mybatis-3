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
package org.apache.ibatis.submitted.extends_with_constructor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/*
 * Test for NPE when using extends.
 *
 * @author poitrac
 */
class NpeExtendsTest {

    @BeforeAll
    static void initDatabase() throws Exception {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryWithConstructor();

        BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
                "org/apache/ibatis/submitted/extends_with_constructor/CreateDB.sql");
    }

    @Test
    void testNoConstructorConfiguration() {
        Configuration configuration = new Configuration();
        configuration.addMapper(StudentMapper.class);
        configuration.addMapper(TeacherMapper.class);
        configuration.getMappedStatementNames();
    }
    @Test
    void testWithConstructorConfiguration() {
        Configuration configuration = new Configuration();
        configuration.addMapper(StudentConstructorMapper.class);
        configuration.addMapper(TeacherMapper.class);
        configuration.getMappedStatementNames();
    }

    private static SqlSessionFactory getSqlSessionFactoryWithConstructor() {
        UnpooledDataSourceFactory unpooledDataSourceFactory = new UnpooledDataSourceFactory();
        Properties properties = new Properties();
        properties.setProperty("driver", "org.hsqldb.jdbcDriver");
        properties.setProperty("url", "jdbc:hsqldb:mem:extends_with_constructor");
        properties.setProperty("username", "sa");
        unpooledDataSourceFactory.setProperties(properties);
        Environment environment = new Environment("extends_with_constructor", new JdbcTransactionFactory(), unpooledDataSourceFactory.getDataSource());

        Configuration configuration = new Configuration();
        configuration.setEnvironment(environment);
        configuration.addMapper(StudentConstructorMapper.class);
        configuration.addMapper(TeacherMapper.class);
        configuration.getMappedStatementNames();
        configuration.setAutoMappingBehavior(AutoMappingBehavior.NONE);

        return new DefaultSqlSessionFactory(configuration);
    }
    @Test
    void testSelectWithTeacher() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryWithConstructor();
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            StudentConstructorMapper studentConstructorMapper = sqlSession.getMapper(StudentConstructorMapper.class);
            StudentConstructor testStudent = studentConstructorMapper.selectWithTeacherById(1);
            assertEquals(1, testStudent.getConstructors().size());
            assertTrue(testStudent.getConstructors().contains(StudentConstructor.Constructor.ID_NAME));
        }
    }
    @Test
    void testSelectNoName() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryWithConstructor();
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            StudentConstructorMapper studentConstructorMapper = sqlSession.getMapper(StudentConstructorMapper.class);
            StudentConstructor testStudent = studentConstructorMapper.selectNoNameById(1);
            assertEquals(1, testStudent.getConstructors().size());
            assertTrue(testStudent.getConstructors().contains(StudentConstructor.Constructor.ID));
            assertNull(testStudent.getName());
        }
    }
}
