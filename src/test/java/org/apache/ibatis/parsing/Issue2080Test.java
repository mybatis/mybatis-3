package org.apache.ibatis.parsing;

import org.junit.jupiter.api.Test;

public class Issue2080Test {


  @Test
  void test() {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
      "<!DOCTYPE mapper\n" +
      "  PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n" +
      "  \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
      "\n" +
      "<mapper namespace=\"demo.StudentMapper\">\n" +
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
  }
}
