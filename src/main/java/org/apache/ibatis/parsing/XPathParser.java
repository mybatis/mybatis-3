package org.apache.ibatis.parsing;

import org.apache.ibatis.builder.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class XPathParser {

  private Document document;
  private boolean validation;
  private EntityResolver entityResolver;
  private Properties variables;
  private XPath xpath;

  public XPathParser(Reader reader, boolean validation, EntityResolver entityResolver, Properties variables) {
    this.validation = validation;
    this.entityResolver = entityResolver;
    this.variables = variables;
    this.document = createDocument(reader);
    XPathFactory factory = XPathFactory.newInstance();
    this.xpath = factory.newXPath();
  }

  public XPathParser(Document document, boolean validation, EntityResolver entityResolver, Properties variables) {
    this.validation = validation;
    this.entityResolver = entityResolver;
    this.variables = variables;
    this.document = document;
    XPathFactory factory = XPathFactory.newInstance();
    this.xpath = factory.newXPath();
  }

  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  public String evalString(String expression) {
    return evalString(document, expression);
  }

  public String evalString(Object root, String expression) {
    String result = (String) evaluate(expression, root, XPathConstants.STRING);
    result = PropertyParser.parse(result, variables);
    return result;
  }

  public Boolean evalBoolean(String expression) {
    return evalBoolean(document, expression);
  }

  public Boolean evalBoolean(Object root, String expression) {
    return (Boolean) evaluate(expression, root, XPathConstants.BOOLEAN);
  }

  public Double evalDouble(String expression) {
    return evalDouble(document, expression);
  }

  public Double evalDouble(Object root, String expression) {
    return (Double) evaluate(expression, root, XPathConstants.NUMBER);
  }

  public List<XNode> evalNodes(String expression) {
    return evalNodes(document, expression);
  }

  public List<XNode> evalNodes(Object root, String expression) {
    List<XNode> xnodes = new ArrayList<XNode>();
    NodeList nodes = (NodeList) evaluate(expression, root, XPathConstants.NODESET);
    for (int i = 0; i < nodes.getLength(); i++) {
      xnodes.add(new XNode(this, nodes.item(i), variables));
    }
    return xnodes;
  }

  public XNode evalNode(String expression) {
    return evalNode(document, expression);
  }

  public XNode evalNode(Object root, String expression) {
    Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
    if (node == null) {
      return null;
    }
    return new XNode(this, node, variables);
  }

  private Object evaluate(String expression, Object root, QName returnType) {
    try {
      return xpath.evaluate(expression, root, returnType);
    } catch (Exception e) {
      throw new BuilderException("Error evaluating XPath.  Cause: " + e, e);
    }
  }

  private Document createDocument(Reader reader) {
    try {
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
    } catch (Exception e) {
      throw new BuilderException("Error creating document instance.  Cause: " + e, e);
    }
  }

}
