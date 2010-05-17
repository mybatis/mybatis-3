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