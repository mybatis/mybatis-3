/*
 *    Copyright 2009-2011 The MyBatis Team
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
package org.apache.ibatis.submitted.keycolumn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Ignore;
import org.junit.Test;

/*
 * This class contains tests for issue #84 - where the Jdbc3KeyGenerator
 * needs to know the key column name because it is not the first column
 * in the resultset returned from getGeneratedKeys().
 * 
 * If PostgreSQL is setup as described in setupdb.txt, then remove
 * the @Ignore annotation to enable the tests.

 * @author Jeff Butler
 */
@Ignore("See setupdb.txt for instructions on how to run the tests in this class")
public class InsertTest {

    @Test
    public void testInsertAnnotated() throws Exception {
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/keycolumn/MapperConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
            Name name = new Name();
            name.setFirstName("Fred");
            name.setLastName("Flintstone");
            
            int rows = mapper.insertNameAnnotated(name);
            
            assertNotNull(name.getId());
            assertEquals(1, rows);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testInsertMapped() throws Exception {
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/keycolumn/MapperConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
            Name name = new Name();
            name.setFirstName("Fred");
            name.setLastName("Flintstone");
            
            int rows = mapper.insertNameMapped(name);
            
            assertNotNull(name.getId());
            assertEquals(1, rows);
        } finally {
            sqlSession.close();
        }
    }
}
