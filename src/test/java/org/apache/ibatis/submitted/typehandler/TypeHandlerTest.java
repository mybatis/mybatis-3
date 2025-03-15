/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.typehandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Reader;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.typehandler.Product.ConstantProductIdTypeHandler;
import org.apache.ibatis.submitted.typehandler.Product.ProductId;
import org.apache.ibatis.submitted.typehandler.Product.ProductIdTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.LocalDateTypeHandler;
import org.apache.ibatis.type.TypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeHandlerTest {

  private SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/typehandler/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(StringTrimmingTypeHandler.class);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/typehandler/CreateDB.sql");
  }

  // Some tests need to register additional type handler
  // before adding mapper.
  private void addMapper() {
    sqlSessionFactory.getConfiguration().addMapper(Mapper.class);
  }

  @Test
  void shouldGetAUser() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      assertEquals("User1", user.getName());
      assertEquals("Carmel", user.getCity());
      assertEquals("IN", user.getState());
    }
  }

  @Test
  void shouldApplyTypeHandlerOnGeneratedKey() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = new Product();
      product.setName("new product");
      mapper.insertProduct(product);
      assertNotNull(product.getId());
      assertNotNull(product.getId().getValue());
    }
  }

  @Test
  void shouldApplyTypeHandlerWithJdbcTypeSpecified() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByName("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    }
  }

  @Test
  void shouldApplyTypeHandlerUsingConstructor() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByName("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    }
  }

  @Test
  void shouldApplyTypeHandlerOnReturnTypeWithJdbcTypeSpecified() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ProductId productId = mapper.getProductIdByName("iPad");
      assertEquals(Integer.valueOf(2), productId.getValue());
    }
  }

  @Test
  void shouldPickSoleTypeHandlerOnXmlResultMap() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByNameXml("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    }
  }

  @Test
  void shouldPickSameTypeHandlerMappedToDifferentJdbcTypes() {
    sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(ProductId.class, JdbcType.BIGINT,
        ProductIdTypeHandler.class);
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByNameXml("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    }
  }

  @Test
  void shouldFailIfMultipleHandlerMappedToAType() {
    sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(ProductId.class, JdbcType.BIGINT,
        ConstantProductIdTypeHandler.class);
    // Two type handlers are mapped to ProductId.
    // One for JdbcType=BIGINT and the other for JdbcType=INTEGER
    // The runtime JdbcType is INTEGER, so the second one should be used.
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByNameXml("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    }
  }

  @Test
  void shouldRespectClassNameFromMetadataByDefault() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map<String, Object> map = mapper.selectDateTime();
      assertEquals(java.sql.Date.class, map.get("D").getClass());
      assertEquals(OffsetTime.class, map.get("T").getClass());
      assertEquals(OffsetDateTime.class, map.get("TS").getClass());
      assertEquals(Time.class, map.get("LT").getClass());
      assertEquals(Timestamp.class, map.get("LTS").getClass());
    }
  }

  @Test
  void shouldHandlerBePickedBasedOnRuntimeJdbcType() {
    sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(ProductId.class, null,
        ConstantProductIdTypeHandler.class);
    // Two type handlers are mapped to ProductId.
    // One for JdbcType=NULL and the other for JdbcType=INTEGER
    // The runtime JdbcType is INTEGER, so the second one should be used.
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByNameXml("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    }
  }

  @Test
  void shouldHandlerBePickedBasedOnRuntimeJdbcType_Map() {
    // gh-591
    // If a handler is registered against Object:JdbcType,
    // it will be used when result type is Map
    sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(Object.class, JdbcType.DATE,
        new LocalDateTypeHandler());
    // The follwing also works for now, but it is not recommended because it could affect
    // internal type handling like cache key creation.
    // sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(JdbcType.DATE, new
    // LocalDateTypeHandler());
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map<String, Object> map = mapper.getProductAsMap(1);
      assertEquals(LocalDate.of(2001, 11, 10), map.get("RELEASED_ON"));
    }
  }

  @Test
  void shouldThrowProperExceptionWhenNoHandlerFoundForParam() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.insertVague(new VagueBean());
      fail("Should throw exception");
    } catch (Exception e) {
      assertThat(e).cause().isExactlyInstanceOf(TypeException.class).hasMessage(
          "Could not find type handler for Java type '" + VagueBean.class.getName() + "' nor JDBC type 'OTHER'");
    }
  }

  @Test
  void shouldThrowProperExceptionWhenNoHandlerFoundForResult() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.selectVague(1);
      fail("Should throw exception");
    } catch (Exception e) {
      assertThat(e).cause().isExactlyInstanceOf(ExecutorException.class)
          .hasMessage("No type handler found for 'java.util.Set<?>' and JDBC type 'OTHER'");
    }
  }

  @Test
  void shouldCreateRowKeyNotThrowNpe() {
    addMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      VagueBean bean = mapper.selectVagueNested(1);
      assertEquals(1, bean.getId());
      assertTrue(bean.getVague().isEmpty());
    }
  }

}
