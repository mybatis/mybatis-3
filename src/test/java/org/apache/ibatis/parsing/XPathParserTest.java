/*
 *    Copyright 2009-2022 the original author or authors.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

class XPathParserTest {
  private String resource = "resources/nodelet_test.xml";

  // InputStream Source
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

  // Reader Source
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

  // Xml String Source
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

  // Document Source
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
      return builder.parse(inputSource);// already closed resource in builder.parse method
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

  enum EnumTest {
    YES, NO
  }

  private void testEvalMethod(XPathParser parser) {
    assertEquals((Long) 1970L, parser.evalLong("/employee/birth_date/year"));
    assertEquals((Long) 1970L, parser.evalNode("/employee/birth_date/year").getLongBody());
    assertEquals((short) 6, (short) parser.evalShort("/employee/birth_date/month"));
    assertEquals((Integer) 15, parser.evalInteger("/employee/birth_date/day"));
    assertEquals((Integer) 15, parser.evalNode("/employee/birth_date/day").getIntBody());
    assertEquals((Float) 5.8f, parser.evalFloat("/employee/height"));
    assertEquals((Float) 5.8f, parser.evalNode("/employee/height").getFloatBody());
    assertEquals((Double) 5.8d, parser.evalDouble("/employee/height"));
    assertEquals((Double) 5.8d, parser.evalNode("/employee/height").getDoubleBody());
    assertEquals((Double) 5.8d, parser.evalNode("/employee").evalDouble("height"));
    assertEquals("${id_var}", parser.evalString("/employee/@id"));
    assertEquals("${id_var}", parser.evalNode("/employee/@id").getStringBody());
    assertEquals("${id_var}", parser.evalNode("/employee").evalString("@id"));
    assertEquals(Boolean.TRUE, parser.evalBoolean("/employee/active"));
    assertEquals(Boolean.TRUE, parser.evalNode("/employee/active").getBooleanBody());
    assertEquals(Boolean.TRUE, parser.evalNode("/employee").evalBoolean("active"));
    assertEquals(EnumTest.YES, parser.evalNode("/employee/active").getEnumAttribute(EnumTest.class, "bot"));
    assertEquals((Float) 3.2f, parser.evalNode("/employee/active").getFloatAttribute("score"));
    assertEquals((Double) 3.2d, parser.evalNode("/employee/active").getDoubleAttribute("score"));

    assertEquals("<id>\n\t${id_var}\n</id>", parser.evalNode("/employee/@id").toString().trim());
    assertEquals(7, parser.evalNodes("/employee/*").size());
    XNode node = parser.evalNode("/employee/height");
    assertEquals("employee/height", node.getPath());
    assertEquals("employee[${id_var}]_height", node.getValueBasedIdentifier());
  }

  @Test
  public void formatXNodeToString() {
    XPathParser parser = new XPathParser("<users><user><id>100</id><name>Tom</name><age>30</age><cars><car index=\"1\">BMW</car><car index=\"2\">Audi</car><car index=\"3\">Benz</car></cars></user></users>");
    String usersNodeToString = parser.evalNode("/users").toString();
    String userNodeToString = parser.evalNode("/users/user").toString();
    String carsNodeToString = parser.evalNode("/users/user/cars").toString();

    String usersNodeToStringExpect =
      "<users>\n" +
        "\t<user>\n" +
        "\t\t<id>\n" +
        "\t\t\t100\n" +
        "\t\t</id>\n" +
        "\t\t<name>\n" +
        "\t\t\tTom\n" +
        "\t\t</name>\n" +
        "\t\t<age>\n" +
        "\t\t\t30\n" +
        "\t\t</age>\n" +
        "\t\t<cars>\n" +
        "\t\t\t<car index=\"1\">\n" +
        "\t\t\t\tBMW\n" +
        "\t\t\t</car>\n" +
        "\t\t\t<car index=\"2\">\n" +
        "\t\t\t\tAudi\n" +
        "\t\t\t</car>\n" +
        "\t\t\t<car index=\"3\">\n" +
        "\t\t\t\tBenz\n" +
        "\t\t\t</car>\n" +
        "\t\t</cars>\n" +
        "\t</user>\n" +
        "</users>\n";

    String userNodeToStringExpect =
      "<user>\n" +
        "\t<id>\n" +
        "\t\t100\n" +
        "\t</id>\n" +
        "\t<name>\n" +
        "\t\tTom\n" +
        "\t</name>\n" +
        "\t<age>\n" +
        "\t\t30\n" +
        "\t</age>\n" +
        "\t<cars>\n" +
        "\t\t<car index=\"1\">\n" +
        "\t\t\tBMW\n" +
        "\t\t</car>\n" +
        "\t\t<car index=\"2\">\n" +
        "\t\t\tAudi\n" +
        "\t\t</car>\n" +
        "\t\t<car index=\"3\">\n" +
        "\t\t\tBenz\n" +
        "\t\t</car>\n" +
        "\t</cars>\n" +
        "</user>\n";

    String carsNodeToStringExpect =
      "<cars>\n" +
        "\t<car index=\"1\">\n" +
        "\t\tBMW\n" +
        "\t</car>\n" +
        "\t<car index=\"2\">\n" +
        "\t\tAudi\n" +
        "\t</car>\n" +
        "\t<car index=\"3\">\n" +
        "\t\tBenz\n" +
        "\t</car>\n" +
        "</cars>\n";

    assertEquals(usersNodeToStringExpect, usersNodeToString);
    assertEquals(userNodeToStringExpect, userNodeToString);
    assertEquals(carsNodeToStringExpect, carsNodeToString);
  }

  /**
   * In order to fix issue #2080, format all not just <code>Node.ELEMENT_NODE</code> type of <code>XNode</code>
   */
  @Test
  public void testTextNodeToString() {
    String mapperString =
      "<mapper namespace=\"demo.StudentMapper\">\n" +
        "  <select id=\"selectFullStudent\">\n" +
        "    select\n" +
        "      STUDENT.ID ID,\n" +
        "      STUDENT.SNO SNO,\n" +
        "      STUDENT.SNAME SNAME,\n" +
        "      STUDENT.SSEX SSEX,\n" +
        "      STUDENT.SBIRTHDAY SBIRTHDAY,\n" +
        "      STUDENT.CLASS CLASS,\n" +
        "      SCORE.CNO CNO,\n" +
        "      SCORE.DEGREE DEGREE,\n" +
        "      HOUSE.HOUSE_ID HOUSE_ID,\n" +
        "      HOUSE.HOUSE_HOLDER HOUSE_HOLDER,\n" +
        "      HOUSE.HOUSE_MEMBER HOUSE_MEMBER\n" +
        "    from STUDENT, SCORE, HOUSE\n" +
        "    <where>\n" +
        "      <if test=\"sex != null\">\n" +
        "        STUDENT.SSEX = #{sex};\n" +
        "      </if>\n" +
        "      and STUDENT.SNO = SCORE.SNO and HOUSE.HOUSE_MEMBER = STUDENT.SNO\n" +
        "    </where>\n" +
        "  </select>\n" +
        "</mapper>";

    String expectedSelectString =
      "<select id=\"selectFullStudent\">\n" +
        "\tselect\n" +
        "      STUDENT.ID ID,\n" +
        "      STUDENT.SNO SNO,\n" +
        "      STUDENT.SNAME SNAME,\n" +
        "      STUDENT.SSEX SSEX,\n" +
        "      STUDENT.SBIRTHDAY SBIRTHDAY,\n" +
        "      STUDENT.CLASS CLASS,\n" +
        "      SCORE.CNO CNO,\n" +
        "      SCORE.DEGREE DEGREE,\n" +
        "      HOUSE.HOUSE_ID HOUSE_ID,\n" +
        "      HOUSE.HOUSE_HOLDER HOUSE_HOLDER,\n" +
        "      HOUSE.HOUSE_MEMBER HOUSE_MEMBER\n" +
        "    from STUDENT, SCORE, HOUSE\n" +
        "\t<where>\n" +
        "\t\t<if test=\"sex != null\">\n" +
        "\t\t\tSTUDENT.SSEX = #{sex};\n" +
        "\t\t</if>\n" +
        "\t\tand STUDENT.SNO = SCORE.SNO and HOUSE.HOUSE_MEMBER = STUDENT.SNO\n" +
        "\t</where>\n" +
        "</select>\n";
    XPathParser parser = new XPathParser(mapperString);
    XNode selectNode = parser.evalNode("mapper/select");
    String selectString = selectNode.toString();
    assertEquals(expectedSelectString, selectString);
  }
}
