/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.session.defaults;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtendedSqlSessionFactoryTest extends BaseDataTest {

  @Test
  void canUseExtendedSqlSessionUsingDefaults() throws Exception {
    createBlogDataSource();

    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);

    SqlSessionFactory sessionFactory = new ExtendedSqlSessionFactoryBuilder().build(reader);
    Assertions.assertThat(sessionFactory).isNotNull();

    try (SqlSession sqlSession = sessionFactory.openSession()) {
      Assertions.assertThat(sqlSession).isNotNull().isInstanceOf(ExtendedSqlSession.class);
    }
  }
}
