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
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public class XMLIncludeTransformer {

  private final Configuration configuration;
  private final MapperBuilderAssistant builderAssistant;

  public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
    this.configuration = configuration;
    this.builderAssistant = builderAssistant;
  }

  public void applyIncludes(Node source) {
    Properties placeholderContext = new Properties();
    Properties configurationVariables = configuration.getVariables();
    if (configurationVariables != null) {
      placeholderContext.putAll(configurationVariables);
    }
    applyIncludes(source, placeholderContext);
  }

  /**
   * Recursively apply includes through all SQL fragments.
   * @param source Include node in DOM tree
   * @param placeholderContext Current context for static placeholders with values
   */
  private void applyIncludes(Node source, final Properties placeholderContext) {
    if (source.getNodeName().equals("include")) {
      // new full context for included SQL - contains inherited context and new variables from current include node
      Properties fullContext;

      String refid = getStringAttribute(source, "refid");
      // replace placeholders and variables in include refid value
      refid = PropertyParser.parse(refid, placeholderContext);
      Node toInclude = findSqlFragment(refid);
      Properties newPlaceholderContext = getPlaceholderContext(source);
      if (!newPlaceholderContext.isEmpty()) {
        // replace placeholders in new variables too
        for (Object name : newPlaceholderContext.keySet()) {
          newPlaceholderContext.put(name, PropertyParser.parse(newPlaceholderContext.get(name).toString(), placeholderContext));
        }
        // merge new and inherited into new full one
        applyInheritedContext(newPlaceholderContext, placeholderContext);
        fullContext = newPlaceholderContext;
      } else {
        // no new context - use inherited fully
        fullContext = placeholderContext;
      }
      applyIncludes(toInclude, fullContext);
      if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
        toInclude = source.getOwnerDocument().importNode(toInclude, true);
      }
      source.getParentNode().replaceChild(toInclude, source);
      while (toInclude.hasChildNodes()) {
        toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
      }
      toInclude.getParentNode().removeChild(toInclude);
    } else if (source.getNodeType() == Node.ELEMENT_NODE) {
      NodeList children = source.getChildNodes();
      for (int i=0; i<children.getLength(); i++) {
        applyIncludes(children.item(i), placeholderContext);
      }
    } else if (source.getNodeType() == Node.ATTRIBUTE_NODE && !placeholderContext.isEmpty()) {
      // replace placeholders in all attribute values
      source.setNodeValue(PropertyParser.parse(source.getNodeValue(), placeholderContext));
    } else if (source.getNodeType() == Node.TEXT_NODE && !placeholderContext.isEmpty()) {
      // replace placeholder ins all text nodes
      source.setNodeValue(PropertyParser.parse(source.getNodeValue(), placeholderContext));
    }
  }

  private Node findSqlFragment(String refid) {
    refid = builderAssistant.applyCurrentNamespace(refid, true);
    try {
      XNode nodeToInclude = configuration.getSqlFragments().get(refid);
      return nodeToInclude.getNode().cloneNode(true);
    } catch (IllegalArgumentException e) {
      throw new IncompleteElementException("Could not find SQL statement to include with refid '" + refid + "'", e);
    }
  }

  private String getStringAttribute(Node node, String name) {
    return node.getAttributes().getNamedItem(name).getNodeValue();
  }

  /**
   * Add inherited context into newly created one.
   * @param newContext placeholders defined current include clause where inherited values will be placed
   * @param inheritedContext all inherited placeholder values
   */
  private void applyInheritedContext(Properties newContext, Properties inheritedContext) {
    for (Map.Entry<Object, Object> e : inheritedContext.entrySet()) {
      if (!newContext.containsKey(e.getKey())) {
        newContext.put(e.getKey(), e.getValue());
      }
    }
  }

  /**
   * Read placholders and their values from include node definition. 
   * @param node Include node instance
   * @return placeholder context from include instance (no inherited values)
   */
  private Properties getPlaceholderContext(Node node) {
    List<Node> subElements = getSubElements(node);
    if (subElements.isEmpty()) {
      return new Properties();
    } else {
      Properties placeholderContext = new Properties();
      for (Node placeholderValue : subElements) {
        String name = getStringAttribute(placeholderValue, "name");
        String value = getStringAttribute(placeholderValue, "value");
        // Push new value
        Object originalValue = placeholderContext.put(name, value);
        if (originalValue != null) {
          throw new IllegalArgumentException("Placeholder " + name + " defined twice in the same include definition");
        }
      }
      return placeholderContext;
    }
  }
  
  private List<Node> getSubElements(Node node) {
    NodeList children = node.getChildNodes();
    if (children.getLength() == 0) {
      return Collections.emptyList();
    } else {
      List<Node> elements = new ArrayList<Node>();
      for (int i = 0; i < children.getLength(); i++) {
        Node n = children.item(i);
        if (n.getNodeType() == Node.ELEMENT_NODE) {
          elements.add(n);
        }
      }
      return elements;
    }
  }
  
}
