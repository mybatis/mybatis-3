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
package org.apache.ibatis.submitted.refcursor;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.util.SocketUtil;

/**
 * @author Jeff Butler
 */
@Tag("EmbeddedPostgresqlTests")
class RefCursorTest {

  private static final EmbeddedPostgres postgres = new EmbeddedPostgres();

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // Launch PostgreSQL server. Download / unarchive if necessary.
    String url = postgres.start(EmbeddedPostgres.cachedRuntimeConfig(Paths.get(System.getProperty("java.io.tmpdir"), "pgembed")), "localhost", SocketUtil.findFreePort(), "refcursor", "postgres", "root", Collections.emptyList());

    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(), new UnpooledDataSource(
        "org.postgresql.Driver", url, null));
    configuration.setEnvironment(environment);
    configuration.addMapper(OrdersMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/refcursor/CreateDB.sql");
  }

  @AfterAll
  static void tearDown() {
    postgres.stop();
  }

  @Test
  void testRefCursor1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
      Map<String, Object> parameter = new HashMap<>();
      parameter.put("orderId", 1);
      mapper.getOrder1(parameter);

      assertNotNull(parameter.get("order"));
      @SuppressWarnings("unchecked")
      List<Order> orders = (List<Order>) parameter.get("order");
      assertEquals(1, orders.size());
      Order order = orders.get(0);
      assertEquals(3, order.getDetailLines().size());
    }
  }

  @Test
  void testRefCursor2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
      Map<String, Object> parameter = new HashMap<>();
      parameter.put("orderId", 1);
      mapper.getOrder2(parameter);

      assertNotNull(parameter.get("order"));
      @SuppressWarnings("unchecked")
      List<Order> orders = (List<Order>) parameter.get("order");
      assertEquals(1, orders.size());
      Order order = orders.get(0);
      assertEquals(3, order.getDetailLines().size());
    }
  }

  @Test
  void shouldUseResultHandlerOnOutputParam() {
    class OrderResultHandler implements ResultHandler<Order> {
      private List<Order> orders = new ArrayList<>();

      @Override
      public void handleResult(ResultContext<? extends Order> resultContext) {
        Order order = resultContext.getResultObject();
        order.setCustomerName("Anonymous");
        orders.add(order);
      }

      List<Order> getResult() {
        return orders;
      }
    }

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
      OrderResultHandler handler = new OrderResultHandler();
      Map<String, Object> parameter = new HashMap<>();
      parameter.put("orderId", 1);
      mapper.getOrder3(parameter, handler);

      assertNull(parameter.get("order"));
      assertEquals(3, parameter.get("detailCount"));
      assertEquals("Anonymous", handler.getResult().get(0).getCustomerName());
    }
  }

  @Test
  void shouldNullResultSetNotCauseNpe() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
      Map<String, Object> parameter = new HashMap<>();
      parameter.put("orderId", 99);
      mapper.getOrder3(parameter, resultContext -> {
        // won't be used
      });
      assertEquals(0, parameter.get("detailCount"));
    }
  }
}
