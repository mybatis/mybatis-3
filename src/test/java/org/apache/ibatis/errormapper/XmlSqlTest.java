package org.apache.ibatis.errormapper;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengdu
 * @date 2020/1/24
 */
public class XmlSqlTest extends BaseDataTest {
  private static SqlSessionFactory sqlMapper;


  @Test
  public void buildXml() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperErrorConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    try {
      sqlMapper = new SqlSessionFactoryBuilder().build(reader);
      Assert.fail("bug");
      SqlSession session = sqlMapper.openSession();
      TestErrorMapperXml mapper = session.getMapper(TestErrorMapperXml.class);
      Author author1 = new Author(1000, "chengdu", "******", "chengdu@somewhere.com", "Something...", null);
      Author author2 = new Author(1001, "chengdu", "******", "chengdu@somewhere.com", "Something...", null);
      List<Author> list = new ArrayList<>();
      list.add(author1);
      list.add(author2);
      mapper.insertAuthorList(list);
    } catch (Exception e) {
    }
  }
}
