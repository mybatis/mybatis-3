package org.apache.ibatis.builder.xml.dynamic;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DynamicSqlSourceTest extends BaseDataTest {

  @Test
  public void shouldDemonstrateSimpleExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    final MixedSqlNode sqlNode = mixedContents(new TextSqlNode(expected));
    DynamicSqlSource source = createDynamicSqlSource(sqlNode);
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldDemonstrateMultipartExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new TextSqlNode("WHERE ID = ?"));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyIncludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new IfSqlNode(mixedContents(new TextSqlNode("WHERE ID = ?")), "true"
        ));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyExcludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new IfSqlNode(mixedContents(new TextSqlNode("WHERE ID = ?")), "false"
        ));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyDefault() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'DEFAULT'";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new ChooseSqlNode(new ArrayList() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?")), "false"
          ));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'")), "false"
          ));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyChooseFirst() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new ChooseSqlNode(new ArrayList() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?")), "true"
          ));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'")), "false"
          ));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyChooseSecond() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'NONE'";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new ChooseSqlNode(new ArrayList() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?")), "false"
          ));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'")), "true"
          ));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREInsteadOfANDForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?  ")), "true"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode("   or NAME = ?  ")), "false"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREInsteadOfORForSecondCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?  ")), "false"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode("   or NAME = ?  ")), "true"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREInsteadOfANDForBothConditions() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  ID = ?   OR NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?   ")), "true"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode("OR NAME = ?  ")), "true"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimNoWhereClause() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?   ")), "false"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode("OR NAME = ?  ")), "false"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimSETInsteadOfCOMMAForBothConditions() throws Exception {
    final String expected = "UPDATE BLOG SET ID = ?,  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("UPDATE BLOG"),
        new SetSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode(" ID = ?, ")), "true"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode(" NAME = ?, ")), "true"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimNoSetClause() throws Exception {
    final String expected = "UPDATE BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("UPDATE BLOG"),
        new SetSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   , ID = ?   ")), "false"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode(", NAME = ?  ")), "false"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldIterateOnceForEachItemInCollection() throws Exception {
    final HashMap<String, String[]> parameterObject = new HashMap() {{
      put("array", new String[]{"one", "two", "three"});
    }};
    final String expected = "SELECT * FROM BLOG WHERE ID in (  one = ? AND two = ? AND three = ? )";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG WHERE ID in"),
        new ForEachSqlNode(new Configuration(),mixedContents(new TextSqlNode("${item} = #{item}")), "array", "index", "item", "(", ")", "AND"));
    BoundSql boundSql = source.getBoundSql(parameterObject);
    assertEquals(expected, boundSql.getSql());
    assertEquals(3, boundSql.getParameterMappings().size());
    assertEquals("__frch_item_0", boundSql.getParameterMappings().get(0).getProperty());
    assertEquals("__frch_item_1", boundSql.getParameterMappings().get(1).getProperty());
    assertEquals("__frch_item_2", boundSql.getParameterMappings().get(2).getProperty());
  }

  private DynamicSqlSource createDynamicSqlSource(SqlNode... contents) throws IOException, SQLException {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
    Configuration configuration = sqlMapper.getConfiguration();
    MixedSqlNode sqlNode = mixedContents(contents);
    return new DynamicSqlSource(configuration, sqlNode);
  }

  private MixedSqlNode mixedContents(SqlNode... contents) {
    return new MixedSqlNode(Arrays.asList(contents));
  }

}
