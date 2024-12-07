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
package org.apache.ibatis.scripting.decorator.bind.impl;

import org.apache.ibatis.scripting.decorator.XmlHolder;
import org.apache.ibatis.scripting.decorator.bind.BindFunction;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Element;

import java.util.regex.Pattern;

/**
 * if[.where]语法：([and|or] [like|eq|ne|ge|gt|le|lt|>|>=|!=] column[|property] [true|false]){1,}
 *
 * @author andyslin
 */
/*package*/  class IfBindFunction implements BindFunction {

  // 逗号，分隔多个参数
  private static final Pattern COMMA = Pattern.compile("\\s*(,)\\s*");
  // 空格，分隔多个单词
  private static final Pattern BLANK = Pattern.compile("\\s+");
  // 竖线，分隔列名和属性名
  private static final Pattern VERTICAL_LINE = Pattern.compile("[|]");

  @Override
  public String getName() {
    return "if";
  }

  @Override
  public void eval(Configuration configuration, Element bind, String subName, String bindValue) {
    // 最终生成的XML字符串
    StringBuilder xml = new StringBuilder();
    // 循环处理空格或逗号分隔的配置参数
    boolean addWhere = "where".equalsIgnoreCase(subName);
    boolean isMySql = "mysql".equalsIgnoreCase(configuration.getDatabaseId());
    for (String arg : COMMA.split(bindValue)) {
      if (null != arg && 0 != arg.trim().length()) {
        Tuple tuple = parseTuple(configuration, arg);
        xml.append(tuple.toXml(isMySql));
      }
    }
    if (addWhere) {
      xml.insert(0, "<where>").append("</where>");
    }
    XmlHolder.replaceNode(bind, xml.toString());
  }

  /**
   * 解析为一个元组（连接词、操作符、列名、属性名、属性是否为boolean类型、布尔类型的取值）
   *
   * @param arg
   * @return
   */
  private Tuple parseTuple(Configuration configuration, String arg) {
    Tuple tuple = new Tuple();
    String[] words = BLANK.split(arg);
    if (1 == words.length) {//只有一个单词
      tuple.column = words[0];
    } else if (2 == words.length) {//两个单词
      if ("and".equalsIgnoreCase(words[0]) || "or".equalsIgnoreCase(words[0])) {
        tuple.join = words[0];
      } else {
        tuple.operate = words[0].toLowerCase();
      }
      tuple.column = words[1];
    } else if (3 <= words.length) {//有三个或三个以上单词
      tuple.join = words[0];
      tuple.operate = words[1].toLowerCase();
      tuple.column = words[2];
      if (words.length >= 4) {
        tuple.isBoolean = true;
        tuple.booleanValue = Boolean.parseBoolean(words[3]);
      }
    }

    // 字段|属性
    String[] arr = VERTICAL_LINE.split(tuple.column);
    if (arr.length >= 2) {
      tuple.property = arr[1];
      tuple.column = arr[0];
    } else {
      tuple.property = column2Property(configuration, tuple.column);
    }
    return tuple;
  }

  private String column2Property(Configuration configuration, String column) {
    int index = column.lastIndexOf(".");
    if (-1 != index) {
      column = column.substring(index + 1);
    }

    if (configuration.isMapUnderscoreToCamelCase()) {
      StringBuilder sb = new StringBuilder();
      boolean upper = false, first = true;
      for (char ch : column.trim().toCharArray()) {
        if (ch == '-' || ch == '_') {
          upper = !first;
        } else {
          sb.append(upper ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
          upper = false;
          first = false;
        }
      }
      return sb.toString();
    } else {
      return column.trim();
    }
  }

  private static class Tuple {
    private String join = "and";
    private String operate = "=";
    private String column = "";
    private String property = "";
    private boolean isBoolean = false;
    private boolean booleanValue = true;

    private String toXml(boolean isMySql) {
      StringBuilder xml = new StringBuilder();
      if (this.isBoolean) {
        xml.append("<if test=\"").append(this.booleanValue ? "" : "!").append(this.property).append("\">");
      } else {
        xml.append("<if test=\"").append("null != ").append(this.property).append(" and '' != ").append(this.property).append("\">");
      }
      xml.append(" ").append(this.join).append(" ");
      if ("like".equals(this.operate)) {
        if (isMySql) {
          xml.append(this.column).append(" LIKE concat('%',#{").append(this.property).append(",jdbcType=VARCHAR},'%')");
        } else {
          xml.append(this.column).append(" LIKE '%'||#{").append(this.property).append(",jdbcType=VARCHAR}||'%'");
        }
      } else if ("eq".equals(this.operate)) {
        xml.append(this.column).append(" = #{").append(this.property).append(",jdbcType=VARCHAR}");
      } else if ("ne".equals(this.operate)) {
        xml.append(this.column).append(" != #{").append(this.property).append(",jdbcType=VARCHAR}");
      } else if ("ge".equals(this.operate)) {
        xml.append(this.column).append(" >= #{").append(this.property).append(",jdbcType=VARCHAR}");
      } else if ("gt".equals(this.operate)) {
        xml.append(this.column).append(" > #{").append(this.property).append(",jdbcType=VARCHAR}");
      } else if ("le".equals(this.operate) || "<=".equals(this.operate)) {
        // le、lt通过将column写在后面实现
        xml.append(" #{").append(this.property).append(",jdbcType=VARCHAR} >= ").append(this.column);
      } else if ("lt".equals(this.operate) || "<".equals(this.operate)) {
        xml.append(" #{").append(this.property).append(",jdbcType=VARCHAR} > ").append(this.column);
      } else {
        xml.append(this.column).append(" ").append(this.operate).append(" #{").append(this.property).append(",jdbcType=VARCHAR}");
      }
      xml.append("</if>");
      return xml.toString();
    }
  }
}
