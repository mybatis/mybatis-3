/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.ibatis.sqlmap.engine.builder;

import com.ibatis.sqlmap.engine.mapping.sql.SqlText;
import com.ibatis.sqlmap.engine.mapping.sql.simple.SimpleDynamicSql;
import com.ibatis.sqlmap.engine.mapping.sql.statik.StaticSql;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class SimpleSqlSource implements SqlSource {

  private Configuration configuration;
  private XmlSqlMapConfigParser configParser;
  private XmlSqlMapParser mapParser;

  private String sql = "";
  private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();

  public SimpleSqlSource(XmlSqlMapParser mapParser, XNode context) {
    this.configuration = mapParser.getConfigParser().getConfiguration();
    this.configParser = mapParser.getConfigParser();
    this.mapParser = mapParser;
    this.parseNodes(context);
  }

  public BoundSql getBoundSql(Object parameterObject) {
    return new BoundSql(configuration,getSql(parameterObject), parameterMappings, parameterObject);
  }

  private String getSql(Object parameterObject) {
    if (SimpleDynamicSql.isSimpleDynamicSql(sql)) {
      return new SimpleDynamicSql(sql, parameterMappings, configuration.getTypeHandlerRegistry()).getSql(parameterObject);
    }
    return new StaticSql(sql).getSql(parameterObject);
  }

  private void parseNodes(XNode node) {
    StringBuilder sqlBuffer = new StringBuilder(sql);
    NodeList children = node.getNode().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      XNode child = node.newXNode(children.item(i));
      String nodeName = child.getNode().getNodeName();
      if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNode().getNodeType() == Node.TEXT_NODE) {
        String data = child.getStringBody();
        InlineParameterMapParser inlineParameterMapParser = new InlineParameterMapParser(configuration);
        SqlText sqlText = inlineParameterMapParser.parseInlineParameterMap(data);
        sqlText.setPostParseRequired(false);

        parameterMappings.addAll(sqlText.getParameterMappings());
        sqlBuffer.append(sqlText.getText());

      } else if ("include".equals(nodeName)) {
        String refid = child.getStringAttribute("refid");
        XNode includeNode = configParser.getSqlFragment(refid);
        if (includeNode == null) {
          String nsrefid = mapParser.applyNamespace(refid);
          includeNode = configParser.getSqlFragment(nsrefid);
          if (includeNode == null) {
            throw new RuntimeException("Could not find SQL statement to include with refid '" + refid + "'");
          }
        }
        parseNodes(includeNode);
      }
    }
    sql = sqlBuffer.toString();
  }

}
