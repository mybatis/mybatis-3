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
