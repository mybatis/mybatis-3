/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.submitted.databaseid_split;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/**
 * Verifies that a {@code databaseId}-specific statement overrides the default statement with the same id even when the
 * two are declared in separate mapper files (a common pattern when default and vendor-specific SQL are kept in
 * different XML files). Before the fix, parsing the default file first and the vendor-specific file afterwards threw an
 * {@link IllegalArgumentException} for a duplicate statement id.
 */
class DatabaseIdSplitMapperTest {

  private static final String STATEMENT_ID = "org.apache.ibatis.submitted.databaseid_split.TestMapper.selectTest";
  private static final String BASE = "org/apache/ibatis/submitted/databaseid_split/";

  @Test
  void databaseSpecificStatementOverridesDefaultWhenDefaultParsedFirst() {
    Configuration configuration = new Configuration();
    configuration.setDatabaseId("oracle");

    parseMapper(configuration, BASE + "DefaultMapper.xml");
    parseMapper(configuration, BASE + "PostgresMapper.xml");
    parseMapper(configuration, BASE + "OracleMapper.xml");

    MappedStatement ms = configuration.getMappedStatement(STATEMENT_ID);
    assertEquals("oracle", ms.getDatabaseId());
    assertEquals("select 1 from dual", boundSql(ms));
  }

  @Test
  void resolutionIsOrderIndependentWhenDatabaseSpecificParsedFirst() {
    Configuration configuration = new Configuration();
    configuration.setDatabaseId("oracle");

    parseMapper(configuration, BASE + "OracleMapper.xml");
    parseMapper(configuration, BASE + "PostgresMapper.xml");
    parseMapper(configuration, BASE + "DefaultMapper.xml");

    MappedStatement ms = configuration.getMappedStatement(STATEMENT_ID);
    assertEquals("oracle", ms.getDatabaseId());
    assertEquals("select 1 from dual", boundSql(ms));
  }

  @Test
  void defaultStatementIsUsedWhenNoDatabaseSpecificMatchExists() {
    Configuration configuration = new Configuration();
    configuration.setDatabaseId("oracle");

    parseMapper(configuration, BASE + "DefaultMapper.xml");
    parseMapper(configuration, BASE + "PostgresMapper.xml");

    MappedStatement ms = configuration.getMappedStatement(STATEMENT_ID);
    assertNull(ms.getDatabaseId());
    assertEquals("select 1", boundSql(ms));
  }

  @Test
  void databaseSpecificStatementsAreIgnoredWhenNoDatabaseIdConfigured() {
    Configuration configuration = new Configuration();
    // no databaseId configured

    parseMapper(configuration, BASE + "DefaultMapper.xml");
    parseMapper(configuration, BASE + "OracleMapper.xml");

    MappedStatement ms = configuration.getMappedStatement(STATEMENT_ID);
    assertNull(ms.getDatabaseId());
    assertEquals("select 1", boundSql(ms));
  }

  @Test
  void duplicateStatementsWithSameDatabaseIdStillFail() {
    Configuration configuration = new Configuration();
    configuration.setDatabaseId("oracle");

    parseMapper(configuration, BASE + "OracleMapper.xml");

    // A second statement carrying the same databaseId is a genuine duplicate and must still be rejected.
    BuilderException e = assertThrows(BuilderException.class,
        () -> parseMapper(configuration, BASE + "OracleMapper2.xml"));
    assertEquals(IllegalArgumentException.class, rootCause(e).getClass());
  }

  @Test
  void databaseSpecificStatementOverridesDefaultWithinSameFile() {
    Configuration configuration = new Configuration();
    configuration.setDatabaseId("oracle");

    parseMapper(configuration, BASE + "CombinedMapper.xml");

    MappedStatement ms = configuration
        .getMappedStatement("org.apache.ibatis.submitted.databaseid_split.CombinedMapper.selectTest");
    assertEquals("oracle", ms.getDatabaseId());
    assertEquals("select 1 from dual", boundSql(ms));
  }

  private static String boundSql(MappedStatement ms) {
    return ms.getBoundSql(null).getSql().trim();
  }

  private static Throwable rootCause(Throwable t) {
    Throwable cause = t;
    while (cause.getCause() != null && cause.getCause() != cause) {
      cause = cause.getCause();
    }
    return cause;
  }

  private static void parseMapper(Configuration configuration, String resource) {
    try (Reader reader = Resources.getResourceAsReader(resource)) {
      XMLMapperBuilder mapperParser = new XMLMapperBuilder(reader, configuration, resource,
          configuration.getSqlFragments());
      mapperParser.parse();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
