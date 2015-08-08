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
package org.apache.ibatis.logging.jdbc;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class BaseJdbcLoggerTest {

  private BaseJdbcLogger baseJdbcLogger;

  @Before
  public void setUp() {
    // We need only a default implementation for calling getResourceContent in the test
    this.baseJdbcLogger = new BaseJdbcLogger(null, 0) {
    };
  }

  @Test
  public void shouldLeaveWhitespaceInsideQuotesAndApostrophesIntact() {
      String originalQuery = getResourceContent("sample-query.txt");
      String queryWithoutWhitespace = baseJdbcLogger.removeBreakingWhitespace(originalQuery);
      assertEquals("SELECT field1 FROM Table WHERE field2 = '   ' AND field3 = \"   \" AND field4=\"  \\\"in  quotes\\\"  \" AND field5 = '  \\'in  apostrophes\\'  ';", queryWithoutWhitespace);
  }

  private static String getResourceContent(String name) {
    InputStream inputStream = BaseJdbcLoggerTest.class.getResourceAsStream(name);
    Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
    return scanner.next();
  }

}
