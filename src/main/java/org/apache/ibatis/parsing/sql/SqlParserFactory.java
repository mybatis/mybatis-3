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
package org.apache.ibatis.parsing.sql;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

public class SqlParserFactory {

  public static final String DELIMITER_PROP = "delimiter";
  public static final String FULLLINE_DELIMITER_PROP = "full-line delimiter";

  public static SqlParser getInstance(Connection c, Map<Object, Object> additionalProps) {
    SqlParser best = null;
    Integer bestSuitability = Integer.MAX_VALUE;
    for (SqlParser parser : ServiceLoader.load(SqlParser.class)) {
      Integer suitability = parser.suitabilityFor(c, additionalProps);
      if (suitability != null) {
        if (best == null || suitability < bestSuitability) {
          best = parser;
          bestSuitability = suitability;
        }
      }
    }
    return best.withProperties(additionalProps);
  }

  public static SqlParser getInstance(Connection c) {
    return getInstance(c, new Properties());
  }
}
