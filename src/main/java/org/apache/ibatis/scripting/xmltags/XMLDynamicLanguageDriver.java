/*
 * Copyright 2012 MyBatis.org.
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
package org.apache.ibatis.scripting.xmltags;

import java.util.ArrayList;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

public class XMLDynamicLanguageDriver implements LanguageDriver {

  public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
  }

  public SqlSource createSqlSource(Configuration configuration, MapperBuilderAssistant builderAssistant, Object script, String databaseId) {
    XNode context;
    if (script instanceof XNode) {
      context = (XNode) script;
      XMLScriptBuilder builder = new XMLScriptBuilder(configuration, builderAssistant, context, databaseId);
      return builder.parseScriptNode();
    }
    else {
      ArrayList<SqlNode> contents = new ArrayList<SqlNode>();
      contents.add(new TextSqlNode(script.toString()));
      MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
      return new DynamicSqlSource(configuration, rootSqlNode);
    }
  }

}
