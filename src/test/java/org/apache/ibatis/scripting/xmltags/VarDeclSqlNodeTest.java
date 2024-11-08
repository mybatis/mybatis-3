/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.scripting.xmltags;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>{@code
 * 	<bind name="pattern" value="'%' + _parameter.getTitle() + '%'" />
 * 	SELECT * FROM BLOG
 * 	WHERE title like #{pattern}
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#bind">bind</a>
 */
class VarDeclSqlNodeTest extends SqlNodeTest {

  private SqlNode sqlNode;

  @BeforeEach
  void setup() {
    this.sqlNode = new VarDeclSqlNode("pattern", "'%' + _parameter.getTitle() + '%'");
  }

  @Test
  @Override
  public void shouldApply() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("_parameter", new Bean("abc"));
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).bind("pattern", "%abc%");
  }

  private static class Bean {

    private String title;

    public Bean(String title) {
      this.title = title;
    }

    public String getTitle() {
      return title;
    }
  }
}
