/**
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.inline_association_with_dot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class InlineCollectionWithDotTest {

  private SqlSession sqlSession;

  public void openSession(String aConfig) throws Exception {

    final String resource = "org/apache/ibatis/submitted/inline_association_with_dot/ibatis-" + aConfig + ".xml";
    try (Reader batisConfigReader = Resources.getResourceAsReader(resource)) {

      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(batisConfigReader);

      BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
          "org/apache/ibatis/submitted/inline_association_with_dot/create.sql");

      sqlSession = sqlSessionFactory.openSession();
    }
  }

  @AfterEach
  void closeSession() {
    if (sqlSession != null) {
      sqlSession.close();
    }
  }

  /*
   * Load an element with an element with and element with a value. Expect that this is
   * possible bij using an inline 'association' map.
   */
  @Test
  void selectElementValueInContainerUsingInline() throws Exception {
    openSession("inline");

    Element myElement = sqlSession.getMapper(ElementMapperUsingInline.class).selectElement();

    assertEquals("value", myElement.getElement().getElement().getValue());
  }

  /*
   * Load an element with an element with and element with a value. Expect that this is
   * possible bij using an sub-'association' map.
   */
  @Test
  void selectElementValueInContainerUsingSubMap() throws Exception {
    openSession("submap");

    Element myElement = sqlSession.getMapper(ElementMapperUsingSubMap.class).selectElement();

    assertEquals("value", myElement.getElement().getElement().getValue());
  }
}
