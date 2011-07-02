package org.apache.ibatis.submitted.multidb;

import static org.junit.Assert.assertEquals;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.Test;

public class ProviderTest {

  @Test
  public void shouldUseDefaultId() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multidb/MultiDbConfig.xml");
    DefaultSqlSessionFactory sqlSessionFactory = (DefaultSqlSessionFactory) new SqlSessionFactoryBuilder().build(reader);
    Configuration c = sqlSessionFactory.getConfiguration();
    assertEquals("HSQL Database Engine", c.getEnvironment().getDatabaseId());
  }

  @Test
  public void shouldUseSpecifiedId() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multidb/SpecifiedIdConfig.xml");
    DefaultSqlSessionFactory sqlSessionFactory = (DefaultSqlSessionFactory) new SqlSessionFactoryBuilder().build(reader);
    Configuration c = sqlSessionFactory.getConfiguration();
    assertEquals("specified", c.getEnvironment().getDatabaseId());
  }

  @Test
  public void shouldUseProvider() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multidb/ProviderConfig.xml");
    DefaultSqlSessionFactory sqlSessionFactory = (DefaultSqlSessionFactory) new SqlSessionFactoryBuilder().build(reader);
    Configuration c = sqlSessionFactory.getConfiguration();
    assertEquals("translated", c.getEnvironment().getDatabaseId());
  }
}
