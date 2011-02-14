package org.apache.ibatis.submitted.refid_resolution;

import java.io.Reader;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

public class RefidResolutionTest {
  @Test(expected = PersistenceException.class)
  public void testIncludes() throws Exception {
    String resource = "org/apache/ibatis/submitted/refid_resolution/MapperConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlSessionFactory = builder.build(reader);
    sqlSessionFactory.getConfiguration().getMappedStatementNames();
  }
}
