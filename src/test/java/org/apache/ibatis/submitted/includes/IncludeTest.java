package org.apache.ibatis.submitted.includes;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.Reader;

public class IncludeTest {

  @Test
  public void testIncludes() throws Exception {
    String resource = "org/apache/ibatis/submitted/includes/MapperConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(reader);
    assertNotNull(sqlMapper);
  }
}
