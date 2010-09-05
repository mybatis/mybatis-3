package org.apache.ibatis.submitted.manyanno;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ManyAnnoTest extends BaseDataTest {

  @Test
  public void testGetMessageForEmptyDatabase() throws Exception {
    final Environment environment = new Environment("test", new JdbcTransactionFactory(), BaseDataTest.createBlogDataSource());
    final Configuration config = new Configuration(environment);
    config.addMapper(PostMapper.class);
    final SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(config);
    final SqlSession session = factory.openSession();
    
    PostMapper mapper = session.getMapper(PostMapper.class);
    List<AnnoPost> posts = mapper.getPosts(101);


    assertEquals(3,posts.size());
    assertEquals(3,posts.get(0).getTags().size());
    assertEquals(1,posts.get(1).getTags().size());
    assertEquals(0,posts.get(2).getTags().size());

    session.close();

  }

}
