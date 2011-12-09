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
package org.apache.ibatis.submitted.parent_child_circular;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.sql.DataSource;

public class ParentChildCircularTest extends BaseDataTest {

  @Test
  public void shouldDemonstrateCircularRelationShipsNotSupportedWithJoinMapping() throws Exception {
    DataSource ds = BaseDataTest.createBlogDataSource();
    Environment env = new Environment("test",new JdbcTransactionFactory(),ds);
    Configuration config = new Configuration(env);
    config.addMapper(NodeMapper.class);
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(config);
    SqlSession session = sqlMapper.openSession();
    try {
      NodeMapper mapper = session.getMapper(NodeMapper.class);

      try {
        mapper.selectNode(1);
        fail("Expected StackOverFlowException");
      } catch (Throwable t) {
        assertTrue(t instanceof StackOverflowError);
      }

    } finally {
      session.rollback();
      if (session != null) session.close();
    }

  }


}