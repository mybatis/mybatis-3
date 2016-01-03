package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * @author liuzh
 */
@Ignore("See setupdb.txt for instructions on how to run the tests in this class")
public class Jdbc3KeyGeneratorTest {

  @Test
  public void shouldInsertListAndRetrieveId() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/executor/keygen/MapperConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
      List<Country> countries = new ArrayList<Country>();
      countries.add(new Country("China", "CN"));
      countries.add(new Country("United Kiongdom", "GB"));
      countries.add(new Country("United States of America", "US"));
      mapper.insertList(countries);
      for (Country country : countries) {
        assertNotNull(country.getId());
      }
    } finally {
      sqlSession.rollback();
      sqlSession.close();
    }
  }
}
