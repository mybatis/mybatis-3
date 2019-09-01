/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.scripting.decorator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * @author andyslin
 */
public class XmlHolder {

  /**
   * 将xml转换为Node
   * 需注意的是，这个Node属于新的Document，如果需要和另外的Document交互，需要先调用{@link Document#importNode(Node, boolean)}
   *
   * @param xml
   * @return
   */
  public static NodeList string2Node(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      DocumentBuilder docBuilder = factory.newDocumentBuilder();
      xml = "<virual-root>" + xml + "</virual-root>";
      InputSource is = new InputSource(new StringReader(xml));
      Document document = docBuilder.parse(is);
      return document.getDocumentElement().getChildNodes();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 替换节点
   *
   * @param node
   * @param xml
   * @return
   */
  public static void replaceNode(Node node, String xml) {
    try {
      NodeList tmp = string2Node(xml);
      int max = tmp.getLength() - 1;
      Document document = node.getOwnerDocument();
      Node parentNode = node.getParentNode();
      Node newNode = null;
      for (int i = max; i >= 0; i--) {
        Node item = tmp.item(i);
        newNode = document.importNode(item, true);
        if (i == max) {
          parentNode.replaceChild(newNode, node);
        } else {
          parentNode.insertBefore(newNode, node);
        }
        node = newNode;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
