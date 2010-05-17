package org.apache.ibatis.submitted.primitive_result_type;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;

public class IbatisConfig {

  private static SqlSessionFactory sqlSessionFactory;

  private IbatisConfig() {
  }

  private static synchronized void init() {
    if (sqlSessionFactory == null)
      try {
        final String resource = "org/apache/ibatis/submitted/primitive_result_type/ibatis.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
  }

  public static SqlSession getSession() {
    if (sqlSessionFactory == null) {
      init();
    }
    return sqlSessionFactory.openSession();
  }
}