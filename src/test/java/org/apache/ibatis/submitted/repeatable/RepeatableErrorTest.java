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
package org.apache.ibatis.submitted.repeatable;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;

class RepeatableErrorTest {

  @Test
  void noSuchStatementByCurrentDatabase() throws IOException {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-derby");
      BuilderException exception = Assertions.assertThrows(BuilderException.class, () ->
          sqlSessionFactory.getConfiguration().addMapper(NoDefineDefaultDatabaseMapper.class));
      Assertions.assertEquals("Could not find a statement annotation that correspond a current database or default statement on method 'org.apache.ibatis.submitted.repeatable.NoDefineDefaultDatabaseMapper.getUser'. Current database id is [derby].", exception.getMessage());
    }
  }

  @Test
  void bothSpecifySelectAndSelectProvider() throws IOException {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-derby");
      BuilderException exception = Assertions.assertThrows(BuilderException.class, () ->
          sqlSessionFactory.getConfiguration().addMapper(BothSelectAndSelectProviderMapper.class));
      String message = exception.getMessage();
      Assertions.assertTrue(message.startsWith("Detected conflicting annotations "));
      Assertions.assertTrue(message.contains("'@org.apache.ibatis.annotations.Select("));
      Assertions.assertTrue(message.contains("'@org.apache.ibatis.annotations.SelectProvider("));
      Assertions.assertTrue(message.matches(".*databaseId=[\"]*,.*"));
      Assertions.assertTrue(message.endsWith(
          "'org.apache.ibatis.submitted.repeatable.BothSelectAndSelectProviderMapper.getUser'."));
    }
  }

  @Test
  void bothSpecifySelectContainerAndSelectProviderContainer() throws IOException {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-derby");
      BuilderException exception = Assertions.assertThrows(BuilderException.class, () ->
          sqlSessionFactory.getConfiguration().addMapper(BothSelectContainerAndSelectProviderContainerMapper.class));
      String message = exception.getMessage();
      Assertions.assertTrue(message.startsWith("Detected conflicting annotations "));
      Assertions.assertTrue(message.contains("'@org.apache.ibatis.annotations.Select("));
      Assertions.assertTrue(message.contains("'@org.apache.ibatis.annotations.SelectProvider("));
      Assertions.assertTrue(message.matches(".*databaseId=\"?hsql\"?,.*"));
      Assertions.assertTrue(message.endsWith(
          " on 'org.apache.ibatis.submitted.repeatable.BothSelectContainerAndSelectProviderContainerMapper.getUser'."));
    }
  }

}
