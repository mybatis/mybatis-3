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

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class XMLScriptBuilderTest {

  @Test
  void shouldWhereInsertWhitespace() throws Exception {
    String xml = """
        <script>
        select * from user
        <where>
        <if test="1==1">and id = 1</if>
        <if test="1==1">and id > 0</if>
        </where>
        </script>
        """;
    SqlSource sqlSource = new XMLScriptBuilder(new Configuration(), new XPathParser(xml).evalNode("/script"))
        .parseScriptNode();
    assertThat(sqlSource.getBoundSql(1).getSql())
        .containsPattern("(?m)^\\s*select \\* from user\\s+WHERE\\s+id = 1\\s+and id > 0\\s*$");
  }

}
