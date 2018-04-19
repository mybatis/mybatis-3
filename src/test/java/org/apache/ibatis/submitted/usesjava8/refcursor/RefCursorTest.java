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
package org.apache.ibatis.submitted.usesjava8.refcursor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.test.EmbeddedPostgresqlTests;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.util.SocketUtil;

/**
 * @author Jeff Butler
 */
@Category(EmbeddedPostgresqlTests.class)
public class RefCursorTest {

  private static final EmbeddedPostgres postgres = new EmbeddedPostgres();

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // Launch PostgreSQL server. Download / unarchive if necessary.
    String url = postgres.start(EmbeddedPostgres.cachedRuntimeConfig(Paths.get(System.getProperty("java.io.tmpdir"), "pgembed")), "localhost", SocketUtil.findFreePort(), "refcursor", "postgres", "root", Collections.emptyList());

    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(), new UnpooledDataSource(
        "org.postgresql.Driver", url, null));
    configuration.setEnvironment(environment);
    configuration.addMapper(OrdersMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    try (SqlSession session = sqlSessionFactory.openSession();
        Connection conn = session.getConnection();
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/usesjava8/refcursor/CreateDB.sql")) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.runScript(reader);
    }
  }

  @AfterClass
  public static void tearDown() {
    postgres.stop();
  }

  @Test
  public void testRefCursor1() throws IOException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
      Map<String, Object> parameter = new HashMap<String, Object>();
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
  public void testRefCursor2() throws IOException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
      Map<String, Object> parameter = new HashMap<String, Object>();
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
  public void shouldUseResultHandlerOnOutputParam() throws IOException {
    class OrderResultHandler implements ResultHandler<Order> {
      private List<Order> orders = new ArrayList<Order>();

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
      Map<String, Object> parameter = new HashMap<String, Object>();
      parameter.put("orderId", 1);
      mapper.getOrder3(parameter, handler);

      assertNull(parameter.get("order"));
      assertEquals(Integer.valueOf(3), parameter.get("detailCount"));
      assertEquals("Anonymous", handler.getResult().get(0).getCustomerName());
    }
  }

  @Test
  public void shouldNullResultSetNotCauseNpe() throws IOException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
      Map<String, Object> parameter = new HashMap<String, Object>();
      parameter.put("orderId", 99);
      mapper.getOrder3(parameter, new ResultHandler<Order>() {
        @Override
        public void handleResult(ResultContext<? extends Order> resultContext) {
          // won't be used
        }
      });
      assertEquals(Integer.valueOf(0), parameter.get("detailCount"));
    }
  }
}
