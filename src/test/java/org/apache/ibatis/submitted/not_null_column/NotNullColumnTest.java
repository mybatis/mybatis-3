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
package org.apache.ibatis.submitted.not_null_column;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultModel;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.result_model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NotNullColumnTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void initDatabase() throws Exception {
        try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/not_null_column/ibatisConfig.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
          sqlSessionFactory.getConfiguration().addMapper(SqlProviderMapper.class);
        }

        BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
                "org/apache/ibatis/submitted/not_null_column/CreateDB.sql");
    }

    @Test
    void testNotNullColumnWithChildrenNoFid() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdNoFid(1);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertEquals(2, test.getChildren().size());
      }
    }

    @Test
    void testNotNullColumnWithoutChildrenNoFid() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdNoFid(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    void testNotNullColumnWithoutChildrenFid() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFid(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    void testNotNullColumnWithoutChildrenWithInternalResultMap() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdWithInternalResultMap(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    void testNotNullColumnWithoutChildrenWithRefResultMap() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdWithRefResultMap(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    void testNotNullColumnWithoutChildrenFidMultipleNullColumns() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidMultipleNullColumns(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    void testNotNullColumnWithoutChildrenFidMultipleNullColumnsAndBrackets() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidMultipleNullColumnsAndBrackets(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    void testNotNullColumnWithoutChildrenFidWorkaround() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

          Father test = fatherMapper.selectByIdFidWorkaround(2);
          assertNotNull(test);
          assertNotNull(test.getChildren());
          assertTrue(test.getChildren().isEmpty());
      }
    }

    @Test
    void testResultModelNotNullColumnWithChildrenNoFid() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        SqlProviderMapper fatherMapper = sqlSession.getMapper(SqlProviderMapper.class);

        Father test = fatherMapper.selectByIdNoFid(1);
        assertNotNull(test);
        assertNotNull(test.getChildren());
        assertEquals(2, test.getChildren().size());
      }
    }

    @Test
    void testResultModelNotNullColumnWithoutChildrenNoFid() {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);

        Father test = fatherMapper.selectByIdNoFid(2);
        assertNotNull(test);
        assertNotNull(test.getChildren());
        assertTrue(test.getChildren().isEmpty());
      }
    }

  public interface SqlProviderMapper {

    @SelectProvider(type = NotNullColumnTest.SqlProviderMapper.SqlProvider.class, method = "selectById")
    @ResultModel(id = "selectByIdNoFid", type = Father.class)
    Father selectByIdNoFid(@Param("id") int id);

    @SuppressWarnings("unused")
    class SqlProvider {
      public static String selectById() {
        return "" +
            "SELECT id, name, Child.id AS child_id, Child.name AS child_name " +
            "FROM Father " +
            "LEFT JOIN Child ON Father.id = Child.father_id " +
            "WHERE id = #{id} ";
      }
    }
  }
}
