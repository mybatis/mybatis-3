/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.submitted.immutable_constructor;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public final class ImmutablePOJOTest {

  private static final Integer POJO_ID = 1;
  private static final String POJO_DESCRIPTION = "Description of immutable";

  private static SqlSessionFactory factory;

  @BeforeClass
  public static void setupClass() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/immutable_constructor/ibatisConfig.xml")) {
      factory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(factory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/immutable_constructor/CreateDB.sql");
  }

  @Test
  public void shouldLoadImmutablePOJOBySignature() {
    try (SqlSession session = factory.openSession()) {
      final ImmutablePOJOMapper mapper = session.getMapper(ImmutablePOJOMapper.class);
      final ImmutablePOJO pojo = mapper.getImmutablePOJO(POJO_ID);

      assertEquals(POJO_ID, pojo.getImmutableId());
      assertEquals(POJO_DESCRIPTION, pojo.getImmutableDescription());
    }
  }


  @Test(expected=PersistenceException.class)
  public void shouldFailLoadingImmutablePOJO() {
    try (SqlSession session = factory.openSession()) {
      final ImmutablePOJOMapper mapper = session.getMapper(ImmutablePOJOMapper.class);
      mapper.getImmutablePOJONoMatchingConstructor(POJO_ID);
    }
  }
  
}
