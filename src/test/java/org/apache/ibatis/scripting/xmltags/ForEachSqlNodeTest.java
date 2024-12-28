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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * <pre>{@code
 * 	SELECT *
 * 	FROM POST
 * 	<where>
 * 	    <foreach item="item" index="index" collection="list" open="ID in (" separator="," close=")" nullable="true">
 * 	        #{item}
 * 	    </foreach>
 * 	</where>
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#foreach">foreach</a>
 */
class ForEachSqlNodeTest extends SqlNodeTest {

  private SqlNode sqlNode;

  @BeforeEach
  void setup() {
    SqlNode contents = new StaticTextSqlNode("#{name}");
    this.sqlNode = new ForEachSqlNode(configuration, contents, "list", "index", "item", "ID in (", ")", ",");
  }

  @Test
  @Override
  public void shouldApply() throws Exception {
    ArgumentCaptor<String> bindKeyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object> bindValueCaptor = ArgumentCaptor.forClass(Object.class);
    doNothing().when(context).bind(bindKeyCaptor.capture(), bindValueCaptor.capture());

    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("list", Arrays.asList("a", "b", "c"));
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("ID in (");
    verify(context).appendSql(")");

    List<String> allKeyValues = bindKeyCaptor.getAllValues();
    List<Object> allValValues = bindValueCaptor.getAllValues();
    assertEquals(Arrays.asList("index", "__frch_index_0", "item", "__frch_item_0", "index", "__frch_index_0", "item",
        "__frch_item_0", "index", "__frch_index_0", "item", "__frch_item_0"), allKeyValues);
    assertEquals(Arrays.asList(0, 0, "a", "a", 1, 1, "b", "b", 2, 2, "c", "c"), allValValues);
  }
}
