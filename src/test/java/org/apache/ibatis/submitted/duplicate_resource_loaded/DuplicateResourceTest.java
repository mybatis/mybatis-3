package org.apache.ibatis.submitted.duplicate_resource_loaded;

import junit.framework.Assert;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.util.List;

public class DuplicateResourceTest extends BaseDataTest {

  @Before
  public void setup() throws Exception {
    BaseDataTest.createBlogDataSource();
  }

  @Test
  public void shouldDemonstrateDuplicateResourceIssue() throws Exception {
    final String resource = "org/apache/ibatis/submitted/duplicate_resource_loaded/Config.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    final SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    final SqlSessionFactory factory = builder.build(reader);
    final SqlSession sqlSession = factory.openSession();
    try {
      final Mapper mapper = sqlSession.getMapper(Mapper.class);
      final List list = mapper.selectAllBlogs();
      Assert.assertEquals(2,list.size());
    } finally {
      sqlSession.close();
    }
  }
}
