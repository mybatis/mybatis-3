/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.not_null_column;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class NotNullColumnTest {
    
    private static SqlSessionFactory sqlSessionFactory;
    
    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:not_null_column", "sa",
                    "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/not_null_column/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/not_null_column/ibatisConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    @Test
    public void testNotNullColumnWithChildrenNoFid() {
      SqlSession sqlSession = sqlSessionFactory.openSession();
      try {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);
          
          Father test = fatherMapper.selectByIdNoFid(1);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertEquals(2, test.getChildren().size());
      } finally {
        sqlSession.close();
      }
    }
    
    @Test
    public void testNotNullColumnWithoutChildrenNoFid() {
      SqlSession sqlSession = sqlSessionFactory.openSession();
      try {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);
          
          Father test = fatherMapper.selectByIdNoFid(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      } finally {
        sqlSession.close();
      }
    }

    @Test
    public void testNotNullColumnWithoutChildrenFid() {
      SqlSession sqlSession = sqlSessionFactory.openSession();
      try {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFid(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      } finally {
        sqlSession.close();
      }
    }
  
    @Test
    public void testNotNullColumnWithoutChildrenWithInternalResultMap() {
      SqlSession sqlSession = sqlSessionFactory.openSession();
      try {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdWithInternalResultMap(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      } finally {
        sqlSession.close();
      }
    }
    
    @Test
    public void testNotNullColumnWithoutChildrenWithRefResultMap() {
      SqlSession sqlSession = sqlSessionFactory.openSession();
      try {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdWithRefResultMap(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      } finally {
        sqlSession.close();
      }
    }
    
    @Test
    public void testNotNullColumnWithoutChildrenFidMultipleNullColumns() {
      SqlSession sqlSession = sqlSessionFactory.openSession();
      try {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidMultipleNullColumns(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      } finally {
        sqlSession.close();
      }
    }

    @Test
    public void testNotNullColumnWithoutChildrenFidMultipleNullColumnsAndBrackets() {
      SqlSession sqlSession = sqlSessionFactory.openSession();
      try {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidMultipleNullColumnsAndBrackets(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      } finally {
        sqlSession.close();
      }
    }

    @Test
    public void testNotNullColumnWithoutChildrenFidWorkaround() {
      SqlSession sqlSession = sqlSessionFactory.openSession();
      try {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidWorkaround(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      } finally {
        sqlSession.close();
      }
    }
}
