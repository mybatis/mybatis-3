/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.typehandler;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.typehandler.Product.ConstantProductIdTypeHandler;
import org.apache.ibatis.submitted.typehandler.Product.ProductId;
import org.apache.ibatis.submitted.typehandler.Product.ProductIdTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.Before;
import org.junit.Test;

public class TypeHandlerTest {

  private SqlSessionFactory sqlSessionFactory;

  @Before
  public void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/typehandler/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();
    sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(StringTrimmingTypeHandler.class);

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/typehandler/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  // Some tests need to register additional type handler
  // before adding mapper.
  private void addMapper() {
    sqlSessionFactory.getConfiguration().addMapper(Mapper.class);
  }

  @Test
  public void shouldGetAUser() {
    addMapper();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      assertEquals("User1", user.getName());
      assertEquals("Carmel", user.getCity());
      assertEquals("IN", user.getState());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldApplyTypeHandlerOnGeneratedKey() {
    addMapper();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = new Product();
      product.setName("new product");
      mapper.insertProduct(product);
      assertNotNull(product.getId());
      assertNotNull(product.getId().getValue());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldApplyTypeHandlerWithJdbcTypeSpecified() throws Exception {
    addMapper();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByName("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldApplyTypeHandlerUsingConstructor() throws Exception {
    addMapper();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByName("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldApplyTypeHandlerOnReturnTypeWithJdbcTypeSpecified() throws Exception {
    addMapper();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ProductId productId = mapper.getProductIdByName("iPad");
      assertEquals(Integer.valueOf(2), productId.getValue());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldPickSoleTypeHandlerOnXmlResultMap() throws Exception {
    addMapper();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByNameXml("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldPickSameTypeHandlerMappedToDifferentJdbcTypes() throws Exception {
    sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(ProductId.class, JdbcType.BIGINT, ProductIdTypeHandler.class);
    addMapper();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByNameXml("iPad");
      assertEquals(Integer.valueOf(2), product.getId().getValue());
    } finally {
      sqlSession.close();
    }
  }

  @Test(expected = BuilderException.class)
  public void shouldFailIfMultipleHandlerMappedToAType() throws Exception {
    sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(ProductId.class, JdbcType.BIGINT, ConstantProductIdTypeHandler.class);
    // multiple type handlers are mapped to ProductId and
    // none of them are mapped to null jdbcType.
    addMapper();
  }

  @Test
  public void shouldPickHandlerForNull() throws Exception {
    sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(ProductId.class, null, ConstantProductIdTypeHandler.class);
    // multiple type handlers are mapped to ProductId and
    // one of them are mapped to null jdbcType.
    addMapper();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Product product = mapper.getProductByNameXml("iPad");
      assertEquals(Integer.valueOf(999), product.getId().getValue());
    } finally {
      sqlSession.close();
    }
  }
}
