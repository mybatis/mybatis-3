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
package org.apache.ibatis.submitted.not_null_column;

import static org.junit.Assert.*;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class NotNullColumnTest {
    
    private static SqlSessionFactory sqlSessionFactory;
    
    @BeforeClass
    public static void initDatabase() throws Exception {
        try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/not_null_column/ibatisConfig.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }

        BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
                "org/apache/ibatis/submitted/not_null_column/CreateDB.sql");
    }
    
    @Test
    public void testNotNullColumnWithChildrenNoFid() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);
          
          Father test = fatherMapper.selectByIdNoFid(1);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertEquals(2, test.getChildren().size());
      }
    }
    
    @Test
    public void testNotNullColumnWithoutChildrenNoFid() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);
          
          Father test = fatherMapper.selectByIdNoFid(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    public void testNotNullColumnWithoutChildrenFid() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFid(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }
  
    @Test
    public void testNotNullColumnWithoutChildrenWithInternalResultMap() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdWithInternalResultMap(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }
    
    @Test
    public void testNotNullColumnWithoutChildrenWithRefResultMap() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdWithRefResultMap(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }
    
    @Test
    public void testNotNullColumnWithoutChildrenFidMultipleNullColumns() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidMultipleNullColumns(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    public void testNotNullColumnWithoutChildrenFidMultipleNullColumnsAndBrackets() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidMultipleNullColumnsAndBrackets(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    public void testNotNullColumnWithoutChildrenFidWorkaround() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidWorkaround(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }
}
