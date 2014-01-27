/*
 * Copyright 2012 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.builder.xml;

import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLIncludeTransformer {

  private final Configuration configuration;
  private final MapperBuilderAssistant builderAssistant;

  public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
    this.configuration = configuration;
    this.builderAssistant = builderAssistant;
  }

  public void applyIncludes(Node source) {
    if (source.getNodeName().equals("include")) {
      Node toInclude = findSqlFragment(getStringAttribute(source, "refid"));
      applyIncludes(toInclude);
      if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
        toInclude = source.getOwnerDocument().importNode(toInclude, true);
      }
      source.getParentNode().replaceChild(toInclude, source);
      while (toInclude.hasChildNodes()) {
        toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
      }
      toInclude.getParentNode().removeChild(toInclude);
    } else if (source.getNodeName().equals("includeColumns")) {
      Node toInclude = findSqlFragment(getStringAttribute(source, "refid"));
      String tableAlias = getStringAttribute(source, "tableAlias");
      String columnAliasPrefix = getOptionalStringAttribute(source, "columnAliasPrefix");
      applyIncludes(toInclude);
      if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
        toInclude = source.getOwnerDocument().importNode(toInclude, true);
      }
      source.getParentNode().replaceChild(toInclude, source);
      while (toInclude.hasChildNodes()) {
        Node firstChild = toInclude.getFirstChild();
        String transformedColumnList = transformColumnList(firstChild.getTextContent(), tableAlias, columnAliasPrefix);
        firstChild.setTextContent(transformedColumnList);
        toInclude.getParentNode().insertBefore(firstChild, toInclude);
      }
      toInclude.getParentNode().removeChild(toInclude);
    } else if (source.getNodeType() == Node.ELEMENT_NODE) {
      NodeList children = source.getChildNodes();
      for (int i=0; i<children.getLength(); i++) {
        applyIncludes(children.item(i));
      }
    }
  }

  private Node findSqlFragment(String refid) {
    refid = PropertyParser.parse(refid, configuration.getVariables());
    refid = builderAssistant.applyCurrentNamespace(refid, true);
    try {
      XNode nodeToInclude = configuration.getSqlFragments().get(refid);
      Node result = nodeToInclude.getNode().cloneNode(true);
      return result;
    } catch (IllegalArgumentException e) {
      throw new IncompleteElementException("Could not find SQL statement to include with refid '" + refid + "'", e);
    }
  }

  private String getStringAttribute(Node node, String name) {
    return node.getAttributes().getNamedItem(name).getNodeValue();
  }

  private String getOptionalStringAttribute(Node node, String name) {
    Node namedItem = node.getAttributes().getNamedItem(name);
    if (namedItem == null) {
      return null;
    }

    return namedItem.getNodeValue();
  }

  private String transformColumnList(String columns, String tableAlias, String columnAliasPrefix) {
    if (columnAliasPrefix == null) {
      columnAliasPrefix = tableAlias;
    }
    StringBuilder stringBuilder = new StringBuilder();
    String textContent = columns.trim();
    String[] tokens = textContent.split(",");
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].trim();
      stringBuilder.append(tableAlias).append(".").append(token);
      if (!token.toLowerCase().contains(" ")) {
        stringBuilder.append(" as ").append(columnAliasPrefix).append("_").append(token);
      }

      if (i < tokens.length - 1) {
        stringBuilder.append(", ");
      }
    }

    return stringBuilder.toString();
  }
}
