/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Types;

import org.junit.jupiter.api.Test;

class JdbcTypeTest {
  private static final String[] requiredStandardTypeNames = {
    "ARRAY", "BIGINT", "BINARY", "BIT", "BLOB", "BOOLEAN", "CHAR", "CLOB",
    "DATALINK", "DATE", "DECIMAL", "DISTINCT", "DOUBLE", "FLOAT", "INTEGER",
    "JAVA_OBJECT", "LONGNVARCHAR", "LONGVARBINARY", "LONGVARCHAR", "NCHAR",
    "NCLOB", "NULL", "NUMERIC","NVARCHAR", "OTHER", "REAL", "REF", "ROWID",
    "SMALLINT", "SQLXML", "STRUCT", "TIME", "TIMESTAMP", "TINYINT",
    "VARBINARY", "VARCHAR"
  };

  @Test
  void shouldHaveRequiredStandardConstants() throws Exception {
    for (String typeName : requiredStandardTypeNames) {
      int typeCode = Types.class.getField(typeName).getInt(null);
      JdbcType jdbcType = JdbcType.valueOf(typeName);
      assertEquals(typeCode, jdbcType.TYPE_CODE);
    }
  }

  @Test
  void shouldHaveDateTimeOffsetConstant() {
    JdbcType jdbcType = JdbcType.valueOf("DATETIMEOFFSET");
    assertEquals(-155, jdbcType.TYPE_CODE);
  }
}
