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
package org.apache.ibatis.parsing.sql.mysql;

import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MySQLSqlParserTest {
  @Test
  public void testProcedures() {
    InputStream sql = getClass().getClassLoader().getResourceAsStream("org/apache/ibatis/parsing/sql/mysql/stored-proc.sql");
    MySQLSqlParser parser = new MySQLSqlParser();
    parser.setSqlReader(new InputStreamReader(sql));

    List<String> statements = StreamSupport.stream(parser.spliterator(), false).collect(Collectors.toList());

    assertEquals(5, statements.size());
    assertEquals("/*!50003 SET sql_mode              = @saved_sql_mode */ ", statements.get(4));
    assertThat(statements.get(3), startsWith("CREATE DEFINER=`root`@`%` PROCEDURE `thread_query`("));
    assertThat(statements.get(3), endsWith("      DATE( start_time ) = DATE(  NOW() );\n  END;\n"));
  }
}