/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.builder.xml.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.OgnlClassResolver;
import org.apache.ibatis.scripting.xmltags.SetSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.VarDeclSqlNode;
import org.apache.ibatis.scripting.xmltags.WhereSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DynamicSqlSourceTest extends BaseDataTest {

  private TypeAliasRegistry registry = new TypeAliasRegistry();
  private OgnlClassResolver classResolver = new OgnlClassResolver(registry);

  @Test
  void shouldDemonstrateSimpleExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    final MixedSqlNode sqlNode = mixedContents(new TextSqlNode(expected, classResolver));
    DynamicSqlSource source = createDynamicSqlSource(sqlNode);
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldDemonstrateMultipartExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new TextSqlNode("WHERE ID = ?", classResolver));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyIncludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new IfSqlNode(mixedContents(new TextSqlNode("WHERE ID = ?", classResolver)), "true"
        , classResolver));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyExcludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new IfSqlNode(mixedContents(new TextSqlNode("WHERE ID = ?", classResolver)), "false"
        , classResolver));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyDefault() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'DEFAULT'";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new ChooseSqlNode(new ArrayList<SqlNode>() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?", classResolver)), "false"
          , classResolver));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'", classResolver)), "false"
          , classResolver));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'", classResolver))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyChooseFirst() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new ChooseSqlNode(new ArrayList<SqlNode>() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?", classResolver)), "true"
          , classResolver));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'", classResolver)), "false"
          , classResolver));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'", classResolver))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyChooseSecond() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'NONE'";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new ChooseSqlNode(new ArrayList<SqlNode>() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?", classResolver)), "false"
          , classResolver));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'", classResolver)), "true"
          , classResolver));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'", classResolver))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREInsteadOfANDForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?  ", classResolver)), "true"
            , classResolver),
            new IfSqlNode(mixedContents(new TextSqlNode("   or NAME = ?  ", classResolver)), "false"
            , classResolver)
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREANDWithLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and\n ID = ?  ", classResolver)), "true"
                , classResolver)
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREANDWithCRLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \r\n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and\r\n ID = ?  ", classResolver)), "true"
                , classResolver)
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREANDWithTABForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \t ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and\t ID = ?  ", classResolver)), "true"
                , classResolver)
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREORWithLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   or\n ID = ?  ", classResolver)), "true"
                , classResolver)
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREORWithCRLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \r\n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   or\r\n ID = ?  ", classResolver)), "true"
                , classResolver)
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREORWithTABForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \t ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   or\t ID = ?  ", classResolver)), "true"
                , classResolver)
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREInsteadOfORForSecondCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?  ", classResolver)), "false"
            , classResolver),
            new IfSqlNode(mixedContents(new TextSqlNode("   or NAME = ?  ", classResolver)), "true"
            , classResolver)
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREInsteadOfANDForBothConditions() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  ID = ?   OR NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?   ", classResolver)), "true"
            , classResolver),
            new IfSqlNode(mixedContents(new TextSqlNode("OR NAME = ?  ", classResolver)), "true"
            , classResolver)
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimNoWhereClause() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?   ", classResolver)), "false"
            , classResolver),
            new IfSqlNode(mixedContents(new TextSqlNode("OR NAME = ?  ", classResolver)), "false"
            , classResolver)
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimSETInsteadOfCOMMAForBothConditions() throws Exception {
    final String expected = "UPDATE BLOG SET ID = ?,  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("UPDATE BLOG", classResolver),
        new SetSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode(" ID = ?, ", classResolver)), "true"
            , classResolver),
            new IfSqlNode(mixedContents(new TextSqlNode(" NAME = ?, ", classResolver)), "true"
            , classResolver)
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimCommaAfterSET() throws Exception {
    final String expected = "UPDATE BLOG SET  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
      new TextSqlNode("UPDATE BLOG", classResolver),
      new SetSqlNode(new Configuration(), mixedContents(
        new IfSqlNode(mixedContents(new TextSqlNode("ID = ?", classResolver)), "false", classResolver),
        new IfSqlNode(mixedContents(new TextSqlNode(", NAME = ?", classResolver)), "true", classResolver))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimNoSetClause() throws Exception {
    final String expected = "UPDATE BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("UPDATE BLOG", classResolver),
        new SetSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   , ID = ?   ", classResolver)), "false"
            , classResolver),
            new IfSqlNode(mixedContents(new TextSqlNode(", NAME = ?  ", classResolver)), "false"
            , classResolver)
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldIterateOnceForEachItemInCollection() throws Exception {
    final HashMap<String, String[]> parameterObject = new HashMap<String, String[]>() {{
      put("array", new String[]{"one", "two", "three"});
    }};
    final String expected = "SELECT * FROM BLOG WHERE ID in (  one = ? AND two = ? AND three = ? )";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG WHERE ID in", classResolver),
        new ForEachSqlNode(new Configuration(),mixedContents(new TextSqlNode("${item} = #{item}", classResolver)), "array", "index", "item", "(", ")", "AND"));
    BoundSql boundSql = source.getBoundSql(parameterObject);
    assertEquals(expected, boundSql.getSql());
    assertEquals(3, boundSql.getParameterMappings().size());
    assertEquals("__frch_item_0", boundSql.getParameterMappings().get(0).getProperty());
    assertEquals("__frch_item_1", boundSql.getParameterMappings().get(1).getProperty());
    assertEquals("__frch_item_2", boundSql.getParameterMappings().get(2).getProperty());
  }

  @Test
  void shouldHandleOgnlExpression() throws Exception {
    final HashMap<String, String> parameterObject = new HashMap<String, String>() {{
      put("name", "Steve");
    }};
    final String expected = "Expression test: 3 / yes.";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode("Expression test: ${name.indexOf('v')} / ${name in {'Bob', 'Steve'\\} ? 'yes' : 'no'}.", classResolver));
    BoundSql boundSql = source.getBoundSql(parameterObject);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldSkipForEachWhenCollectionIsEmpty() throws Exception {
    final HashMap<String, Integer[]> parameterObject = new HashMap<String, Integer[]>() {{
        put("array", new Integer[] {});
    }};
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode("SELECT * FROM BLOG", classResolver),
        new ForEachSqlNode(new Configuration(), mixedContents(
            new TextSqlNode("#{item}", classResolver)), "array", null, "item", "WHERE id in (", ")", ","));
    BoundSql boundSql = source.getBoundSql(parameterObject);
    assertEquals(expected, boundSql.getSql());
    assertEquals(0, boundSql.getParameterMappings().size());
  }

  @Test
  void shouldPerformStrictMatchOnForEachVariableSubstitution() throws Exception {
    final Map<String, Object> param = new HashMap<>();
    final Map<String, String> uuu = new HashMap<>();
    uuu.put("u", "xyz");
    List<Bean> uuuu = new ArrayList<>();
    uuuu.add(new Bean("bean id"));
    param.put("uuu", uuu);
    param.put("uuuu", uuuu);
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("INSERT INTO BLOG (ID, NAME, NOTE, COMMENT) VALUES", classResolver),
        new ForEachSqlNode(new Configuration(),mixedContents(
            new TextSqlNode("#{uuu.u}, #{u.id}, #{ u,typeHandler=org.apache.ibatis.type.StringTypeHandler},"
                + " #{u:VARCHAR,typeHandler=org.apache.ibatis.type.StringTypeHandler}", classResolver)), "uuuu", "uu", "u", "(", ")", ","));
    BoundSql boundSql = source.getBoundSql(param);
    assertEquals(4, boundSql.getParameterMappings().size());
    assertEquals("uuu.u", boundSql.getParameterMappings().get(0).getProperty());
    assertEquals("__frch_u_0.id", boundSql.getParameterMappings().get(1).getProperty());
    assertEquals("__frch_u_0", boundSql.getParameterMappings().get(2).getProperty());
    assertEquals("__frch_u_0", boundSql.getParameterMappings().get(3).getProperty());
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

  @Test
  void shouldMapNullStringsToEmptyStrings() {
    final String expected = "id=${id}";
    final MixedSqlNode sqlNode = mixedContents(new TextSqlNode(expected, classResolver));
    final DynamicSqlSource source = new DynamicSqlSource(new Configuration(), sqlNode);
    String sql = source.getBoundSql(new Bean(null)).getSql();
    Assertions.assertEquals("id=", sql);
  }

  public static class Bean {
    public String id;
    Bean(String property) {
      this.id = property;
    }
    public String getId() {
      return id;
    }
    public void setId(String property) {
      this.id = property;
    }
  }

  @Test
  void shouldResolveTypeAlias() throws IOException, SQLException {
    registry.registerAlias(Constant.class);
    registry.registerAlias(AuditUtils.class);
    final String expected = "SELECT * FROM BLOG WHERE ID = ? AND delete_flg = '0' AND reg_user_id = 'U0000001'";
    DynamicSqlSource source = createDynamicSqlSource(
      new TextSqlNode("SELECT * FROM BLOG", classResolver),
      new IfSqlNode(
        mixedContents(
          new VarDeclSqlNode("operatorId", "@AuditUtils@getCurrentUserId()", classResolver),
          new TextSqlNode("WHERE ID = ? AND delete_flg = '${@Constant@OFF}' AND reg_user_id = '${operatorId}'", classResolver)
        ), "flag == @Constant@ON", classResolver)
    );
    class MyBean {
      public String flag = Constant.ON;
    }
    BoundSql boundSql = source.getBoundSql(new MyBean());
    assertEquals(expected, boundSql.getSql());
    assertEquals("U0000001", boundSql.getAdditionalParameter("operatorId"));
  }

  static class Constant {
    public static final String OFF = "0";
    public static final String ON = "1";
  }

  static class AuditUtils {
    public static String getCurrentUserId() {
      return "U0000001";
    }
  }

}
