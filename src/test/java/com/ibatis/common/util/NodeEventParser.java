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
package com.ibatis.common.util;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.ibatis.parsing.*;

import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class NodeEventParser {

  private Map nodeletMap = new HashMap();

  private XPathParser xpathParser;
  private boolean validation;
  private EntityResolver entityResolver;
  private Properties variables;

  public NodeEventParser() {
    setValidation(false);
    setVariables(new Properties());
    setEntityResolver(null);
  }

  /*
   * Registers a nodelet for the specified XPath.  Current XPaths supported
   * are:
   * <ul>
   * <li> Element Path - /rootElement/childElement/theElement
   * <li> Closing element - /rootElement/childElement/end()
   * <li> All Elements Named - //theElement
   * </ul>
   */
  public void addNodeletHandler(Object handler) {
    Class type = handler.getClass();
    Method[] methods = type.getMethods();
    for (Method m : methods) {
      NodeEvent n = m.getAnnotation(NodeEvent.class);
      if (n != null) {
        checkMethodApplicable(n, type, m);
        nodeletMap.put(n.value(), new NodeEventWrapper(handler, m));
      }
    }
  }

  /*
   * Begins parsing from the provided Reader.
   */
  public void parse(Reader reader) throws ParsingException {
    try {
      Document doc = createDocument(reader);
      xpathParser = new XPathParser(doc,validation, variables, entityResolver);
      parse(doc.getLastChild());
    } catch (Exception e) {
      throw new ParsingException("Error parsing XML.  Cause: " + e, e);
    }
  }

  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  public void setValidation(boolean validation) {
    this.validation = validation;
  }

  public void setEntityResolver(EntityResolver resolver) {
    this.entityResolver = resolver;
  }

  private void checkMethodApplicable(NodeEvent n, Class type, Method m) {
    if (nodeletMap.containsKey(n.value())) {
      throw new ParsingException("This nodelet parser already has a handler for path " + n.value());
    }
    Class<?>[] params = m.getParameterTypes();
    if (params.length != 1 || params[0] != XNode.class) {
      throw new ParsingException("The method " + m.getName() + " on " + type + " does not take a single parameter of type XNode.");
    }
  }

  /*
   * Begins parsing from the provided Node.
   */
  private void parse(Node node) {
    Path path = new Path();
    processNodelet(node, "/");
    process(node, path);
  }

  /*
   * A recursive method that walkes the DOM tree, registers XPaths and
   * calls Nodelets registered under those XPaths.
   */
  private void process(Node node, Path path) {
    if (node instanceof Element) {
      // Element
      String elementName = node.getNodeName();
      path.add(elementName);
      processNodelet(node, path.toString());
      processNodelet(node, new StringBuffer("//").append(elementName).toString());

      // Children
      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        process(children.item(i), path);
      }
      path.add("end()");
      processNodelet(node, path.toString());
      path.remove();
      path.remove();
    }
  }

  private void processNodelet(Node node, String pathString) {
    NodeEventWrapper nodelet = (NodeEventWrapper) nodeletMap.get(pathString);
    if (nodelet != null) {
      try {
        nodelet.process(new XNode(xpathParser,node, variables));
      } catch (Exception e) {
        throw new ParsingException("Error parsing XPath '" + pathString + "'.  Cause: " + e, e);
      }
    }
  }

  /*
   * Creates a JAXP Document from a reader.
   */
  private Document createDocument(Reader reader) throws ParserConfigurationException, FactoryConfigurationError, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(validation);

    factory.setNamespaceAware(false);
    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(false);
    factory.setCoalescing(false);
    factory.setExpandEntityReferences(true);

    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(entityResolver);
    builder.setErrorHandler(new ErrorHandler() {
      public void error(SAXParseException exception) throws SAXException {
        throw exception;
      }

      public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
      }

      public void warning(SAXParseException exception) throws SAXException {
      }
    });

    return builder.parse(new InputSource(reader));
  }

  /*
   * Inner helper class that assists with building XPath paths.
   * <p/>
   * Note:  Currently this is a bit slow and could be optimized.
   */
  private static class Path {

    private List nodeList = new ArrayList();

    public Path() {
    }

    public void add(String node) {
      nodeList.add(node);
    }

    public void remove() {
      nodeList.remove(nodeList.size() - 1);
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer("/");
      for (int i = 0; i < nodeList.size(); i++) {
        buffer.append(nodeList.get(i));
        if (i < nodeList.size() - 1) {
          buffer.append("/");
        }
      }
      return buffer.toString();
    }
  }

}
