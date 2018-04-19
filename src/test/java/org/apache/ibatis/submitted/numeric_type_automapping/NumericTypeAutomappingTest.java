/**
 * Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.numeric_type_automapping;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;

public class NumericTypeAutomappingTest {

    private static final int EXISTING_ITEM_ID = 1;

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        // create an SqlSessionFactory
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/numeric_type_automapping/mybatis-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        reader.close();

        // populate in-memory database
        SqlSession session = sqlSessionFactory.openSession();
        Connection conn = session.getConnection();
        reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/numeric_type_automapping/CreateDB.sql");
        ScriptRunner runner = new ScriptRunner(conn);
        runner.setLogWriter(null);
        runner.runScript(reader);
        conn.close();
        reader.close();
        session.close();
    }

    @Test
    public void firstLevelInheritedField() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Mapper mapper = sqlSession.getMapper(Mapper.class);

        Concrete1 concrete1 = mapper.getFirstLevelInheritance(EXISTING_ITEM_ID);

        Assert.assertEquals(Integer.class, concrete1.getId().getClass());
    }

    @Test
    public void secondLevelInheritedField() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Mapper mapper = sqlSession.getMapper(Mapper.class);

        Concrete2 concrete2 = mapper.getSecondLevelInheritance(EXISTING_ITEM_ID);

        Assert.assertEquals(Integer.class, concrete2.getId().getClass());
    }

    @Test
    public void thirdLevelInheritedField() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Mapper mapper = sqlSession.getMapper(Mapper.class);

        Concrete3 concrete3 = mapper.getThirdLevelInheritance(EXISTING_ITEM_ID);

        Assert.assertEquals(Integer.class, concrete3.getId().getClass());
    }
}
