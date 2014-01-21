/*
 * Copyright 2012-2014 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.scripting.defaults;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * As of 3.2.4 the default XML language is able to identify static statements
 * and create a {@link RawSqlSource}. So there is no need to use RAW unless you
 * want to make sure that there is not any dynamic tag for any reason.
 * 
 * @since 3.2.0
 */
public class RawLanguageDriver implements LanguageDriver {

  public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
  }

  public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
    return new RawSqlSource(configuration, parseXML(script), parameterType);
  }

  public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
    return new RawSqlSource(configuration, script, parameterType);
  }

  private static SqlNode parseXML(XNode script) {
    List<SqlNode> contents = new ArrayList<SqlNode>();
    NodeList children = script.getNode().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      XNode child = script.newXNode(children.item(i));
      String nodeName = child.getNode().getNodeName();
      if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
        String data = child.getStringBody("");
        contents.add(new StaticTextSqlNode(data));
      } else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
        throw new BuilderException("Found an invalid element <" + nodeName + "> for RAW language.");
      }
    }
    return new MixedSqlNode(contents);
  }

}
