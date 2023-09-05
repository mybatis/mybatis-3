/**
 *    Copyright 2009-2021 the original author or authors.
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

import org.junit.jupiter.api.Test;

public class Issue2080Test {


  @Test
  void test() {
    String xml = "<mapper namespace=\"demo.StudentMapper\">\n" +
      "  <select id=\"selectFullStudent\" resultMap=\"fullResult\" databaseId=\"mysql\">\n" +
      "    select\n" +
      "    STUDENT.ID ID,\n" +
      "    STUDENT.SNO SNO,\n" +
      "    STUDENT.SNAME SNAME,\n" +
      "    STUDENT.SSEX SSEX,\n" +
      "    STUDENT.SBIRTHDAY SBIRTHDAY,\n" +
      "    STUDENT.CLASS CLASS,\n" +
      "    SCORE.CNO CNO,\n" +
      "    SCORE.DEGREE DEGREE,\n" +
      "    HOUSE.HOUSE_ID HOUSE_ID,\n" +
      "    HOUSE.HOUSE_HOLDER HOUSE_HOLDER,\n" +
      "    HOUSE.HOUSE_MEMBER HOUSE_MEMBER\n" +
      "    from STUDENT, SCORE, HOUSE\n" +
      "    <where>\n" +
      "      <if test=\"sex != null\">\n" +
      "        STUDENT.SSEX = #{sex};\n" +
      "      </if>\n" +
      "      and STUDENT.SNO = SCORE.SNO and HOUSE.HOUSE_MEMBER = STUDENT.SNO\n" +
      "    </where>\n" +
      "  </select>\n" +
      "</mapper>\n";
    XPathParser xPathParser = new XPathParser(xml);
    XNode select = xPathParser.evalNode("mapper/select");
    System.out.println(select.toString());
    System.out.println(xml);
  }
}
