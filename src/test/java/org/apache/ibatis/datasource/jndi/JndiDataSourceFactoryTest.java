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
package org.apache.ibatis.datasource.jndi;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

class JndiDataSourceFactoryTest extends BaseDataTest {

  private static final String TEST_INITIAL_CONTEXT_FACTORY = MockContextFactory.class.getName();
  private static final String TEST_INITIAL_CONTEXT = "/mypath/path/";
  private static final String TEST_DATA_SOURCE = "myDataSource";
  private UnpooledDataSource expectedDataSource;

  @BeforeEach
  void setup() throws Exception {
    expectedDataSource = createUnpooledDataSource(BLOG_PROPERTIES);
  }

  @Test
  void shouldRetrieveDataSourceFromJNDI() {
    createJndiDataSource();
    JndiDataSourceFactory factory = new JndiDataSourceFactory();
    factory.setProperties(new Properties() {
      {
        setProperty(JndiDataSourceFactory.ENV_PREFIX + Context.INITIAL_CONTEXT_FACTORY, TEST_INITIAL_CONTEXT_FACTORY);
        setProperty(JndiDataSourceFactory.INITIAL_CONTEXT, TEST_INITIAL_CONTEXT);
        setProperty(JndiDataSourceFactory.DATA_SOURCE, TEST_DATA_SOURCE);
      }
    });
    DataSource actualDataSource = factory.getDataSource();
    assertEquals(expectedDataSource, actualDataSource);
  }

  private void createJndiDataSource() {
    try {
      Properties env = new Properties();
      env.put(Context.INITIAL_CONTEXT_FACTORY, TEST_INITIAL_CONTEXT_FACTORY);

      MockContext ctx = new MockContext(false);
      ctx.bind(TEST_DATA_SOURCE, expectedDataSource);

      InitialContext initCtx = new InitialContext(env);
      initCtx.bind(TEST_INITIAL_CONTEXT, ctx);
    } catch (NamingException e) {
      throw new DataSourceException("There was an error configuring JndiDataSourceTransactionPool. Cause: " + e, e);
    }
  }

  public static class MockContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
      return new MockContext(false);
    }
  }

  public static class MockContext extends InitialContext {
    private static Map<String,Object> bindings = new HashMap<>();

    MockContext(boolean lazy) throws NamingException {
      super(lazy);
    }

    @Override
    public Object lookup(String name) {
      return bindings.get(name);
    }

    @Override
    public void bind(String name, Object obj) {
      bindings.put(name, obj);
    }
  }

}
