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
package org.apache.ibatis.submitted.duplicate_resource_loaded;

import org.junit.Assert;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.util.List;
import java.util.Map;

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
      final List<Map<String, Object>> list = mapper.selectAllBlogs();
      Assert.assertEquals(2,list.size());
    } finally {
      sqlSession.close();
    }
  }
}
