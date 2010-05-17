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
