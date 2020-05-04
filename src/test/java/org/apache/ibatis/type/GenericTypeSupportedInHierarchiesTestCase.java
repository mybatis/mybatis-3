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
package org.apache.ibatis.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

class GenericTypeSupportedInHierarchiesTestCase {

  @Test
  void detectsTheGenericTypeTraversingTheHierarchy() {
    assertEquals(String.class, new CustomStringTypeHandler().getRawType());
  }

  /**
   *
   */
  public static final class CustomStringTypeHandler extends StringTypeHandler {

    /**
     * Defined as reported in #581
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
        throws SQLException {
      // do something
      super.setNonNullParameter(ps, i, parameter, jdbcType);
    }

  }

}
