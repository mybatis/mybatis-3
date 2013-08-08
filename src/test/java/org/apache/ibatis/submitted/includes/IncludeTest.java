/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.includes;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.Reader;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;

public class IncludeTest {

  @Test
  public void testIncludes() throws Exception {
    String resource = "org/apache/ibatis/submitted/includes/MapperConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(reader);
    assertNotNull(sqlMapper);

    final SqlSession sqlSession = sqlMapper.openSession();
    try {
      final Integer result = sqlSession.selectOne("org.apache.ibatis.submitted.includes.mapper.selectWithProperty");
      Assert.assertEquals(Integer.valueOf(1), result);
    } finally {
      sqlSession.close();
    }
  }
}
