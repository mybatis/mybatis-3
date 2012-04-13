/*
 *    Copyright 2009-2012 The MyBatis Team
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
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.DynamicSql;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class DynamicSqlSource implements SqlSource {
  private XNode context;
  private Configuration configuration;
  private XmlSqlMapConfigParser configParser;
  private XmlSqlMapParser mapParser;

  public DynamicSqlSource(XmlSqlMapParser mapParser, XNode context) {
    this.context = context;
    this.configuration = mapParser.getConfigParser().getConfiguration();
    this.configParser = mapParser.getConfigParser();
    this.mapParser = mapParser;
  }

  public BoundSql getBoundSql(Object parameterObject) {
    return new BoundSql(configuration, getSql(parameterObject), getParameterMappings(parameterObject), parameterObject);
  }

  private List<ParameterMapping> getParameterMappings(Object parameterObject) {
    DynamicSql dynamic = new DynamicSql(configuration);
    parseDynamicTags(context, dynamic, true);
    return dynamic.getParameterMappings(parameterObject);
  }

  private String getSql(Object parameterObject) {
    DynamicSql dynamic = new DynamicSql(configuration);
    parseDynamicTags(context, dynamic, true);
    return dynamic.getSql(parameterObject);
  }

  private void parseDynamicTags(XNode node, DynamicParent dynamic, boolean postParseRequired) {
    NodeList children = node.getNode().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      XNode child = node.newXNode(children.item(i));
      String nodeName = child.getNode().getNodeName();
      if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNode().getNodeType() == Node.TEXT_NODE) {
        String data = child.getStringBody("");
        SqlText sqlText;
        if (postParseRequired) {
          sqlText = new SqlText();
          sqlText.setPostParseRequired(postParseRequired);
          sqlText.setText(data);
        } else {
          InlineParameterMapParser inlineParameterMapParser = new InlineParameterMapParser(configuration);
          sqlText = inlineParameterMapParser.parseInlineParameterMap(data);
          sqlText.setPostParseRequired(postParseRequired);
        }
        dynamic.addChild(sqlText);
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
        parseDynamicTags(includeNode, dynamic, postParseRequired);
      } else {
        SqlTagHandler handler = SqlTagHandlerFactory.getSqlTagHandler(nodeName);
        if (handler != null) {
          SqlTag tag = new SqlTag();
          tag.setName(nodeName);
          tag.setHandler(handler);

          tag.setPrependAttr(child.getStringAttribute("prepend"));
          tag.setPropertyAttr(child.getStringAttribute("property"));
          tag.setRemoveFirstPrepend(child.getStringAttribute("removeFirstPrepend"));

          tag.setOpenAttr(child.getStringAttribute("open"));
          tag.setCloseAttr(child.getStringAttribute("close"));

          tag.setComparePropertyAttr(child.getStringAttribute("compareProperty"));
          tag.setCompareValueAttr(child.getStringAttribute("compareValue"));
          tag.setConjunctionAttr(child.getStringAttribute("conjunction"));

          if (handler instanceof IterateTagHandler
              && (tag.getPropertyAttr() == null || "".equals(tag.getPropertyAttr()))) {
            tag.setPropertyAttr("_collection");
          }

          // an iterate ancestor requires a post parse

          if (dynamic instanceof SqlTag) {
            SqlTag parentSqlTag = (SqlTag) dynamic;
            if (parentSqlTag.isPostParseRequired() ||
                tag.getHandler() instanceof IterateTagHandler) {
              tag.setPostParseRequired(true);
            }
          } else if (dynamic instanceof DynamicSql) {
            if (tag.getHandler() instanceof IterateTagHandler) {
              tag.setPostParseRequired(true);
            }
          }

          dynamic.addChild(tag);

          if (child.getNode().hasChildNodes()) {
            parseDynamicTags(child, tag, tag.isPostParseRequired());
          }
        }
      }
    }
  }

}
