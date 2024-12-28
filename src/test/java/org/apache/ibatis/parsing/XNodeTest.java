/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class XNodeTest {

  @Test
  void formatXNodeToString() {
    XPathParser parser = new XPathParser(
        "<users><user><id>100</id><name>Tom</name><age>30</age><cars><car index=\"1\">BMW</car><car index=\"2\">Audi</car><car index=\"3\">Benz</car></cars></user></users>");
    String usersNodeToString = parser.evalNode("/users").toString();
    String userNodeToString = parser.evalNode("/users/user").toString();
    String carsNodeToString = parser.evalNode("/users/user/cars").toString();

    // @formatter:off
    String usersNodeToStringExpect =
      "<users>\n"
      + "  <user>\n"
      + "    <id>\n"
      + "      100\n"
      + "    </id>\n"
      + "    <name>\n"
      + "      Tom\n"
      + "    </name>\n"
      + "    <age>\n"
      + "      30\n"
      + "    </age>\n"
      + "    <cars>\n"
      + "      <car index=\"1\">\n"
      + "        BMW\n"
      + "      </car>\n"
      + "      <car index=\"2\">\n"
      + "        Audi\n"
      + "      </car>\n"
      + "      <car index=\"3\">\n"
      + "        Benz\n"
      + "      </car>\n"
      + "    </cars>\n"
      + "  </user>\n"
      + "</users>\n";
    // @formatter:on

    // @formatter:off
    String userNodeToStringExpect =
      "<user>\n"
      + "  <id>\n"
      + "    100\n"
      + "  </id>\n"
      + "  <name>\n"
      + "    Tom\n"
      + "  </name>\n"
      + "  <age>\n"
      + "    30\n"
      + "  </age>\n"
      + "  <cars>\n"
      + "    <car index=\"1\">\n"
      + "      BMW\n"
      + "    </car>\n"
      + "    <car index=\"2\">\n"
      + "      Audi\n"
      + "    </car>\n"
      + "    <car index=\"3\">\n"
      + "      Benz\n"
      + "    </car>\n"
      + "  </cars>\n"
      + "</user>\n";
    // @formatter:on

    // @formatter:off
    String carsNodeToStringExpect =
      "<cars>\n"
      + "  <car index=\"1\">\n"
      + "    BMW\n"
      + "  </car>\n"
      + "  <car index=\"2\">\n"
      + "    Audi\n"
      + "  </car>\n"
      + "  <car index=\"3\">\n"
      + "    Benz\n"
      + "  </car>\n"
      + "</cars>\n";
    // @formatter:on

    assertEquals(usersNodeToStringExpect, usersNodeToString);
    assertEquals(userNodeToStringExpect, userNodeToString);
    assertEquals(carsNodeToStringExpect, carsNodeToString);
  }

  @Test
  void xNodeToString() {
    // @formatter:off
    String xml = "<mapper>\n" +
        "  <select id='select' resultType='map'>\n" +
        "    select\n" +
        "    <var set='foo' value='bar' />\n" +
        "      ID,\n" +
        "      NAME\n" +
        "    from STUDENT\n" +
        "    <where>\n" +
        "      <if test=\"name != null\">\n" +
        "        NAME = #{name}\n" +
        "      </if>\n" +
        "      and DISABLED = false\n" +
        "    </where>\n" +
        "    order by ID\n" +
        "    <choose>\n" +
        "      <when test='limit10'>\n" +
        "        limit 10\n" +
        "      </when>\n" +
        "      <otherwise>limit 20</otherwise>\n" +
        "    </choose>\n" +
        "  </select>\n" +
        "</mapper>";

    String expected = "<select id=\"select\" resultType=\"map\">\n" +
        "  select\n" +
        "  <var set=\"foo\" value=\"bar\" />\n" +
        "  ID,\n" +
        // a little bit ugly here, but not a blocker
        "      NAME\n" +
        "    from STUDENT\n" +
        "  <where>\n" +
        "    <if test=\"name != null\">\n" +
        "      NAME = #{name}\n" +
        "    </if>\n" +
        "    and DISABLED = false\n" +
        "  </where>\n" +
        "  order by ID\n" +
        "  <choose>\n" +
        "    <when test=\"limit10\">\n" +
        "      limit 10\n" +
        "    </when>\n" +
        "    <otherwise>\n" +
        "      limit 20\n" +
        "    </otherwise>\n" +
        "  </choose>\n" +
        "</select>\n";
    // @formatter:on

    XPathParser parser = new XPathParser(xml);
    XNode selectNode = parser.evalNode("/mapper/select");
    assertEquals(expected, selectNode.toString());
  }

  @Test
  void testXnodeToStringVariables() throws Exception {
    String src = "<root attr='${x}'>y = ${y}<sub attr='${y}'>x = ${x}</sub></root>";
    String expected = "<root attr=\"foo\">\n  y = bar\n  <sub attr=\"bar\">\n    x = foo\n  </sub>\n</root>\n";
    Properties vars = new Properties();
    vars.put("x", "foo");
    vars.put("y", "bar");
    XPathParser parser = new XPathParser(src, false, vars);
    XNode selectNode = parser.evalNode("/root");
    assertEquals(expected, selectNode.toString());
  }

}
