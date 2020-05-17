/*
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
package org.apache.ibatis.builder;

import java.io.Reader;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SqlSourceBuilderTest {

  private static Configuration configuration;
  private static SqlSourceBuilder sqlSourceBuilder;
  String sqlFromXml = "SELECT * \n        FROM user\n        WHERE user_id = 1";

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
      .getResourceAsReader("org/apache/ibatis/submitted/empty_row/mybatis-config.xml")) {
      XMLConfigBuilder parser = new XMLConfigBuilder(reader, null, null);
      configuration = parser.parse();
    }

    sqlSourceBuilder = new SqlSourceBuilder(configuration);
  }

  @Test
  void testMinifySqlEnabledIsFalse() {
    SqlSource sqlSource = sqlSourceBuilder.parse(sqlFromXml, null, null);
    BoundSql boundSql = sqlSource.getBoundSql(null);
    String actual = boundSql.getSql();
    Assertions.assertEquals(sqlFromXml, actual);
  }

  @Test
  void testMinifySqlEnabledIsTrue() {
    configuration.setShrinkWhitespacesInSql(true);
    SqlSource sqlSource = sqlSourceBuilder.parse(sqlFromXml, null, null);
    BoundSql boundSql = sqlSource.getBoundSql(null);
    String actual = boundSql.getSql();
    Assertions.assertNotEquals(sqlFromXml, actual);
  }
}
