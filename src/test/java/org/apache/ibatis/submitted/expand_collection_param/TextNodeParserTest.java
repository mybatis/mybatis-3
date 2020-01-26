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
package org.apache.ibatis.submitted.expand_collection_param;

import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextNodeParserTest {

  @Test
  public void shouldParseStaticText() {
    TextNodeParserResult result = TextNodeParser.parse(new Configuration(), "" +
      "SELECT id, name " +
      "FROM blog WHERE id = 1");

    assertEquals(false, result.isDynamic);
    assertEquals(1, result.nodes.size());
    assertTrue(result.nodes.get(0) instanceof StaticTextSqlNode);
  }

  @Test
  public void shouldParseStaticTextWithParameter() {
    TextNodeParserResult result = TextNodeParser.parse(new Configuration(), "SELECT id, name FROM blog WHERE id = #{id}");

    assertEquals(false, result.isDynamic);
    assertEquals(1, result.nodes.size());
    assertTrue(result.nodes.get(0) instanceof StaticTextSqlNode);
  }

  @Test
  public void shouldParseDynamicText() {
    TextNodeParserResult result = TextNodeParser.parse(new Configuration(), "" +
      "SELECT id, name " +
      "FROM blog " +
      "WHERE ${column} = 1");

    assertEquals(true, result.isDynamic);
    assertEquals(1, result.nodes.size());
    assertTrue(result.nodes.get(0) instanceof TextSqlNode);
  }

  @Test
  public void shouldParseOneCollection() {
    TextNodeParserResult result = TextNodeParser.parse(new Configuration(), "" +
      "SELECT id, name " +
      "FROM blog " +
      "WHERE id IN (#{ids...})");

    assertEquals(true, result.isDynamic);
    assertEquals(3, result.nodes.size());
    assertTrue(result.nodes.get(0) instanceof StaticTextSqlNode);
    assertTrue(result.nodes.get(1) instanceof ForEachSqlNode);
    assertTrue(result.nodes.get(2) instanceof StaticTextSqlNode);
  }

  @Test
  public void shouldParseOneCollectionAndDynamicText() {
    TextNodeParserResult result = TextNodeParser.parse(new Configuration(), "" +
      "SELECT id, name FROM blog " +
      "WHERE id IN (#{ids...}) " +
      "AND ${column} = 1");

    assertEquals(true, result.isDynamic);
    assertEquals(3, result.nodes.size());
    assertTrue(result.nodes.get(0) instanceof StaticTextSqlNode);
    assertTrue(result.nodes.get(1) instanceof ForEachSqlNode);
    assertTrue(result.nodes.get(2) instanceof TextSqlNode);
  }


  @Test
  public void shouldParseMultipleCollections() {
    TextNodeParserResult result = TextNodeParser.parse(new Configuration(), "" +
      "SELECT id, name FROM blog " +
      "WHERE id IN (#{ids...}) " +
      "AND category IN (#{categories...})" +
      "AND ${column} = 1");

    assertEquals(true, result.isDynamic);
    assertEquals(5, result.nodes.size());
    assertTrue(result.nodes.get(0) instanceof StaticTextSqlNode);
    assertTrue(result.nodes.get(1) instanceof ForEachSqlNode);
    assertTrue(result.nodes.get(2) instanceof StaticTextSqlNode);
    assertTrue(result.nodes.get(3) instanceof ForEachSqlNode);
    assertTrue(result.nodes.get(4) instanceof TextSqlNode);
  }
}
