/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.keygen;

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
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/keygen/MapperConfig.xml");
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
