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

import java.util.Arrays;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>{@code
 * 	SELECT *
 * 	FROM users
 * 	<where>
 * 	    <if test="id != null">
 * 			AND id = #{id}
 * 	    </if>
 * 	    <if test="name != null">
 * 			AND name = #{name}
 * 	    </if>
 * 	</where>
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#trim-where-set">trim-where-set</a>
 */
class WhereSqlNodeTest extends SqlNodeTest {

  private static final String FIRST_TEXT = " AND id = #{id}";
  private static final String SECOND_TEXT = " AND name = #{name}";

  private SqlNode sqlNode;

  @BeforeEach
  void setup() {
    SqlNode first = new IfSqlNode(new StaticTextSqlNode(FIRST_TEXT), "id != null");
    SqlNode second = new IfSqlNode(new StaticTextSqlNode(SECOND_TEXT), "name != null");
    SqlNode contents = new MixedSqlNode(Arrays.asList(first, second));

    this.sqlNode = new WhereSqlNode(configuration, contents);
  }

  @Test
  @Override
  public void shouldApply() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("id", 1);
        put("name", "mybatis");
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("WHERE  id = #{id} AND name = #{name}");
  }

  @Test
  public void shouldAppendOnlyId() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("id", 1);
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("WHERE  id = #{id}");
  }

  @Test
  public void shouldAppendOnlyName() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("name", "mybatis");
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("WHERE  name = #{name}");
  }

  @Test
  public void shouldAppendNone() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>());

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("");
  }
}
