/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.xml_external_ref;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShortNameTest {
  @Test
  void getStatementByShortName() throws Exception {
    Configuration configuration = getConfiguration();
    // statement can be referenced by its short name.
    MappedStatement selectPet = configuration.getMappedStatement("selectPet");
    assertNotNull(selectPet);
  }

  @Test
  void ambiguousShortNameShouldFail() throws Exception {
    Configuration configuration = getConfiguration();
    // ambiguous short name should throw an exception.
    Assertions.assertThrows(IllegalArgumentException.class, () -> configuration.getMappedStatement("select"));
  }

  private Configuration getConfiguration() throws IOException {
    try (Reader configReader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/xml_external_ref/MapperConfig.xml")) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
      return sqlSessionFactory.getConfiguration();
    }
  }
}
