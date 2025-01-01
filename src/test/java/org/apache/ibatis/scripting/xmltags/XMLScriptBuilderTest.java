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
package org.apache.ibatis.scripting.xmltags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class XMLScriptBuilderTest {

  @Test
  void shouldEmptyTextNodesRemoved() throws Exception {
    String xml = """
        <script>
          select * from user
          <if test="_parameter != null">
            where id = 1
          </if>
          <if test="_parameter == null">
            where id > 0
          </if>
        </script>
        """;
    SqlSource sqlSource = new XMLScriptBuilder(new Configuration(), new XPathParser(xml).evalNode("/script"))
        .parseScriptNode();

    Field rootSqlNodeFld = DynamicSqlSource.class.getDeclaredField("rootSqlNode");
    rootSqlNodeFld.setAccessible(true);
    MixedSqlNode sqlNode = (MixedSqlNode) rootSqlNodeFld.get(sqlSource);

    Field contentsFld = MixedSqlNode.class.getDeclaredField("contents");
    contentsFld.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<SqlNode> contents = (List<SqlNode>) contentsFld.get(sqlNode);

    assertEquals(3, contents.size());
    assertThat(contents.get(0)).isInstanceOf(StaticTextSqlNode.class);
    assertThat(contents.get(1)).isInstanceOf(IfSqlNode.class);
    assertThat(contents.get(2)).isInstanceOf(IfSqlNode.class);
  }

}
