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

import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 */
class MixedSqlNodeTest extends SqlNodeTest {

  private static final String FIRST_TEXT = "abc";
  private static final String SECOND_TEXT = "bcd";
  private SqlNode sqlNode;

  @BeforeEach
  void setup() {
    SqlNode first = new StaticTextSqlNode(FIRST_TEXT);
    SqlNode second = new StaticTextSqlNode(SECOND_TEXT);
    this.sqlNode = new MixedSqlNode(Arrays.asList(first, second));
  }

  @Test
  @Override
  public void shouldApply() throws Exception {
    sqlNode.apply(context);

    verify(context).appendSql("abc");
    verify(context).appendSql("bcd");
  }
}
