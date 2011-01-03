package org.apache.ibatis.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.junit.Test;

public class SqlSessionBuilderTest extends BaseDataTest {

  final String resource = "org/apache/ibatis/builder/MapperConfig.xml";

  @Test
  public void shouldBuildADefaultConfig() throws Exception {
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    assertNotNull(sqlSessionFactory.getConfiguration());
    assertEquals(Configuration.class, sqlSessionFactory.getConfiguration().getClass());
  }

  @Test
  public void shouldBuildCustomizedConfiguration() throws Exception {
    InputStream inputStream = Resources.getResourceAsStream(resource);
    Configuration myCustomConfig = new CustomConfig();
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, null, null, myCustomConfig);
    assertNotNull(sqlSessionFactory.getConfiguration());
    assertEquals(CustomConfig.class, sqlSessionFactory.getConfiguration().getClass());
  }

  @Test(expected=PersistenceException.class)
  public void shouldNotAllowPassingANullConfig() throws Exception {
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, null, null, null);
  }

  public static class CustomConfig extends Configuration {
    
  }
}
