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

    String usersNodeToStringExpect = """
        <users>
          <user>
            <id>
              100
            </id>
            <name>
              Tom
            </name>
            <age>
              30
            </age>
            <cars>
              <car index="1">
                BMW
              </car>
              <car index="2">
                Audi
              </car>
              <car index="3">
                Benz
              </car>
            </cars>
          </user>
        </users>
        """;

    String userNodeToStringExpect = """
        <user>
          <id>
            100
          </id>
          <name>
            Tom
          </name>
          <age>
            30
          </age>
          <cars>
            <car index="1">
              BMW
            </car>
            <car index="2">
              Audi
            </car>
            <car index="3">
              Benz
            </car>
          </cars>
        </user>
        """;

    String carsNodeToStringExpect = """
        <cars>
          <car index="1">
            BMW
          </car>
          <car index="2">
            Audi
          </car>
          <car index="3">
            Benz
          </car>
        </cars>
        """;

    assertEquals(usersNodeToStringExpect, usersNodeToString);
    assertEquals(userNodeToStringExpect, userNodeToString);
    assertEquals(carsNodeToStringExpect, carsNodeToString);
  }

  @Test
  void xNodeToString() {
    String xml = """
        <mapper>
          <select id='select' resultType='map'>
            select
            <var set='foo' value='bar' />
              ID,
              NAME
            from STUDENT
            <where>
              <if test="name != null">
                NAME = #{name}
              </if>
              and DISABLED = false
            </where>
            order by ID
            <choose>
              <when test='limit10'>
                limit 10
              </when>
              <otherwise>limit 20</otherwise>
            </choose>
          </select>
        </mapper>
        """;

    // a little bit ugly with id/name break, but not a blocker
    String expected = """
        <select id="select" resultType="map">
          select
          <var set="foo" value="bar" />
          ID,
              NAME
            from STUDENT
          <where>
            <if test="name != null">
              NAME = #{name}
            </if>
            and DISABLED = false
          </where>
          order by ID
          <choose>
            <when test="limit10">
              limit 10
            </when>
            <otherwise>
              limit 20
            </otherwise>
          </choose>
        </select>
        """;

    XPathParser parser = new XPathParser(xml);
    XNode selectNode = parser.evalNode("/mapper/select");
    assertEquals(expected, selectNode.toString());
  }

  @Test
  void xnodeToStringVariables() throws Exception {
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
