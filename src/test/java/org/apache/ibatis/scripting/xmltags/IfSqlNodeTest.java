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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>{@code
 * 	<if test="title != null>
 * 		AND title like #{title}
 * 	</if>
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#if">if</a>
 */
class IfSqlNodeTest extends SqlNodeTest {

  private static final String CONDITION = "title != null";
  private static final String TEXT = "AND title like #{title}";

  private SqlNode sqlNode;

  @BeforeEach
  void setup() {
    SqlNode contents = new StaticTextSqlNode(TEXT);
    this.sqlNode = new IfSqlNode(contents, CONDITION);
  }

  @Test
  @Override
  public void shouldApply() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("title", "ENGLISH");
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql(TEXT);
  }

  @Test
  public void shouldAppendNone() {
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("title", null);
      }
    });

    boolean result = sqlNode.apply(context);

    assertFalse(result);
    verify(context, never()).appendSql(TEXT);
  }
}
