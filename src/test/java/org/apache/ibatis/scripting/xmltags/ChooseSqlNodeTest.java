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
import java.util.List;

import org.apache.ibatis.domain.blog.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>{@code
 *  SELECT *
 *  FROM BLOG
 *  WHERE state = 'active'
 *  <choose>
 * 		<when test="title != null">
 * 		 	AND title like #{title}
 * 		</when>
 * 		<when test="author != null && author.username != null">
 * 		 	AND author_name like #{author.username}
 * 		</when>
 * 		<otherwise>
 * 		 	AND featured = 1
 * 		</otherwise>
 *  </choose>
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#choose-when-otherwise">choose</a>
 */
class ChooseSqlNodeTest extends SqlNodeTest {

  private static final String FIRST_TEXT = " AND title like #{title}";
  private static final String SECOND_TEXT = " AND author_name like #{author.username}";
  private static final String OTHERWISE_TEXT = " AND featured = 1";

  private SqlNode sqlNode;

  @BeforeEach
  void setup() {
    SqlNode first = new IfSqlNode(new StaticTextSqlNode(FIRST_TEXT), "title != null");
    SqlNode second = new IfSqlNode(new StaticTextSqlNode(SECOND_TEXT), "author != null && author.username != null");
    List<SqlNode> ifNodes = Arrays.asList(first, second);

    SqlNode defaultNode = new StaticTextSqlNode(OTHERWISE_TEXT);

    this.sqlNode = new ChooseSqlNode(ifNodes, defaultNode);
  }

  @Test
  @Override
  public void shouldApply() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("title", "abc");
        put("author", new Author(1, "mybatis", "***", null, null, null));
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql(FIRST_TEXT);
  }

  @Test
  public void shouldAppendSecond() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("author", new Author(1, "mybatis", "***", null, null, null));
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql(SECOND_TEXT);
  }

  @Test
  public void shouldAppendOtherwise() throws Exception {
    when(context.getBindings()).thenReturn(new HashMap<>());

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql(OTHERWISE_TEXT);
  }
}
