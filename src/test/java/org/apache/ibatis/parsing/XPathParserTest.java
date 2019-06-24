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
package org.apache.ibatis.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.*;

class XPathParserTest {
  private String resource = "resources/nodelet_test.xml";

  //InputStream Source
  @Test
  void constructorWithInputStreamValidationVariablesEntityResolver() throws Exception {

    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XPathParser parser = new XPathParser(inputStream, false, null, null);
      testEvalMethod(parser);
    }
  }

  @Test
  void constructorWithInputStreamValidationVariables() throws IOException {
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XPathParser parser = new XPathParser(inputStream, false, null);
      testEvalMethod(parser);
    }
  }

  @Test
  void constructorWithInputStreamValidation() throws IOException {
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XPathParser parser = new XPathParser(inputStream, false);
      testEvalMethod(parser);
    }
  }

  @Test
  void constructorWithInputStream() throws IOException {
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XPathParser parser = new XPathParser(inputStream);
      testEvalMethod(parser);
    }
  }

  //Reader Source
  @Test
  void constructorWithReaderValidationVariablesEntityResolver() throws Exception {

    try (Reader reader = Resources.getResourceAsReader(resource)) {
      XPathParser parser = new XPathParser(reader, false, null, null);
      testEvalMethod(parser);
    }
  }

  @Test
  void constructorWithReaderValidationVariables() throws IOException {
    try (Reader reader = Resources.getResourceAsReader(resource)) {
      XPathParser parser = new XPathParser(reader, false, null);
      testEvalMethod(parser);
    }
  }

  @Test
  void constructorWithReaderValidation() throws IOException {
    try (Reader reader = Resources.getResourceAsReader(resource)) {
      XPathParser parser = new XPathParser(reader, false);
      testEvalMethod(parser);
    }
  }

  @Test
  void constructorWithReader() throws IOException {
    try (Reader reader = Resources.getResourceAsReader(resource)) {
      XPathParser parser = new XPathParser(reader);
      testEvalMethod(parser);
    }
  }

  //Xml String Source
  @Test
  void constructorWithStringValidationVariablesEntityResolver() throws Exception {
    XPathParser parser = new XPathParser(getXmlString(resource), false, null, null);
    testEvalMethod(parser);
  }

  @Test
  void constructorWithStringValidationVariables() throws IOException {
    XPathParser parser = new XPathParser(getXmlString(resource), false, null);
    testEvalMethod(parser);
  }

  @Test
  void constructorWithStringValidation() throws IOException {
    XPathParser parser = new XPathParser(getXmlString(resource), false);
    testEvalMethod(parser);
  }

  @Test
  void constructorWithString() throws IOException {
    XPathParser parser = new XPathParser(getXmlString(resource));
    testEvalMethod(parser);
  }

  //Document Source
  @Test
  void constructorWithDocumentValidationVariablesEntityResolver() {
    XPathParser parser = new XPathParser(getDocument(resource), false, null, null);
    testEvalMethod(parser);
  }

  @Test
  void constructorWithDocumentValidationVariables() {
    XPathParser parser = new XPathParser(getDocument(resource), false, null);
    testEvalMethod(parser);
  }

  @Test
  void constructorWithDocumentValidation() {
    XPathParser parser = new XPathParser(getDocument(resource), false);
    testEvalMethod(parser);
  }

  @Test
  void constructorWithDocument() {
    XPathParser parser = new XPathParser(getDocument(resource));
    testEvalMethod(parser);
  }

  private Document getDocument(String resource) {
    try {
      InputSource inputSource = new InputSource(Resources.getResourceAsReader(resource));
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(false);
      factory.setCoalescing(false);
      factory.setExpandEntityReferences(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(inputSource);//already closed resource in builder.parse method
    } catch (Exception e) {
      throw new BuilderException("Error creating document instance.  Cause: " + e, e);
    }
  }

  private String getXmlString(String resource) throws IOException {
    try (BufferedReader bufferedReader = new BufferedReader(Resources.getResourceAsReader(resource))) {
      StringBuilder sb = new StringBuilder();
      String temp;
      while ((temp = bufferedReader.readLine()) != null) {
        sb.append(temp);
      }
      return sb.toString();
    }
  }

  private void testEvalMethod(XPathParser parser) {
    assertEquals((Long) 1970L, parser.evalLong("/employee/birth_date/year"));
    assertEquals((short) 6, (short) parser.evalShort("/employee/birth_date/month"));
    assertEquals((Integer) 15, parser.evalInteger("/employee/birth_date/day"));
    assertEquals((Float) 5.8f, parser.evalFloat("/employee/height"));
    assertEquals((Double) 5.8d, parser.evalDouble("/employee/height"));
    assertEquals("${id_var}", parser.evalString("/employee/@id"));
    assertEquals(Boolean.TRUE, parser.evalBoolean("/employee/active"));
    assertEquals("<id>${id_var}</id>", parser.evalNode("/employee/@id").toString().trim());
    assertEquals(7, parser.evalNodes("/employee/*").size());
    XNode node = parser.evalNode("/employee/height");
    assertEquals("employee/height", node.getPath());
    assertEquals("employee[${id_var}]_height", node.getValueBasedIdentifier());
  }

}
