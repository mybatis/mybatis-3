/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.mapping;

import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResultMappingTest {
  @Mock
  private Configuration configuration;

  // Issue 697: Association with both a resultMap and a select attribute should throw exception
  @Test
  void shouldThrowErrorWhenBothResultMapAndNestedSelectAreSet() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      new ResultMapping.Builder(configuration, "prop")
        .nestedQueryId("nested query ID")
        .nestedResultMapId("nested resultMap")
        .build();
    });
  }

  //Issue 4: column is mandatory on nested queries
  @Test
  void shouldFailWithAMissingColumnInNetstedSelect() {
    Assertions.assertThrows(IllegalStateException.class, () -> new ResultMapping.Builder(configuration, "prop")
        .nestedQueryId("nested query ID")
        .build());
  }

}
