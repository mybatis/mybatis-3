/*
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
package org.apache.ibatis.scripting.xmltags;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public class VarDeclSqlNode implements SqlNode {

  private final XMLLanguageDriver xmlLanguageDriver;
  private final String name;
  private final String expression;

  public VarDeclSqlNode(XMLLanguageDriver xmlLanguageDriver, String var, String exp) {
    this.xmlLanguageDriver = xmlLanguageDriver;
    name = var;
    expression = exp;
  }

  @Override
  public boolean apply(DynamicContext context) {
    final Object value = xmlLanguageDriver.getOgnlValue(expression, context.getBindings());
    context.bind(name, value);
    return true;
  }

}
