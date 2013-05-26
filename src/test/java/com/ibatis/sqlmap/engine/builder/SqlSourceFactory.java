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

import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.SqlTagHandler;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.SqlTagHandlerFactory;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SqlSourceFactory {

  private XmlSqlMapParser mapParser;
  private XmlSqlMapConfigParser configParser;
  private Ibatis2Configuration configuration;

  public SqlSourceFactory(XmlSqlMapParser mapParser) {
    this.mapParser = mapParser;
    this.configParser = mapParser.getConfigParser();
    this.configuration = mapParser.getConfigParser().getConfiguration();
  }

  public SqlSource newSqlSourceIntance(XmlSqlMapParser mapParser, XNode context) {
    if (isDynamic(context, false)) {
      return new DynamicSqlSource(mapParser, context);
    } else {
      return new SimpleSqlSource(mapParser, context);
    }
  }

  private boolean isDynamic(XNode node, boolean isDynamic) {
    NodeList children = node.getNode().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      XNode child = node.newXNode(children.item(i));
      String nodeName = child.getNode().getNodeName();
      if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNode().getNodeType() == Node.TEXT_NODE) {
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
        isDynamic = isDynamic(includeNode, isDynamic);
      } else {
        SqlTagHandler handler = SqlTagHandlerFactory.getSqlTagHandler(nodeName);
        if (handler != null) {
          isDynamic = true;
        }
      }
    }
    return isDynamic;
  }


}
