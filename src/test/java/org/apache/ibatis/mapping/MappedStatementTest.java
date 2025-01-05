/*
 *    Copyright 2009-2025 the original author or authors.
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

package org.apache.ibatis.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MappedStatementTest {

  @CsvSource(value = { "aRS|true", "nested_cursor|true", "aRS," + ResultMapping.NESTED_CURSOR + "|false",
      ResultMapping.NESTED_CURSOR + ",aRS|false" }, delimiter = '|')
  @ParameterizedTest
  void shouldRejectReservedResultSetName(String resultSets, boolean shouldPass) {
    final Configuration config = new Configuration();
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

    try {
      new MappedStatement.Builder(config, "select", new StaticSqlSource(config, "select 1"), SqlCommandType.SELECT)
          .resultMaps(List.of(new ResultMap.Builder(config, "authorRM", HashMap.class,
              List.of(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(Integer.class)).build()))
                  .build()))
          .resultSets(resultSets).build();
      if (!shouldPass) {
        fail("Reserved result set name should be rejected.");
      }
    } catch (IllegalStateException e) {
      if (shouldPass) {
        fail("Non-reserved result set names should not be rejected.");
      }
      assertEquals("Result set name '" + ResultMapping.NESTED_CURSOR + "' is reserved, please assign another name.",
          e.getMessage());
    }
  }

}
