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
package org.apache.ibatis.scripting.xmltags;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public class VarDeclSqlNode implements SqlNode {

  private final String name;
  private final String expression;
  private final OgnlClassResolver classResolver;

  /**
   * @deprecated Since 3.5.2, please use the {@link #VarDeclSqlNode(String, String, OgnlClassResolver)}
   */
  @Deprecated
  public VarDeclSqlNode(String var, String exp) {
    this(var, exp, null);
  }

  /**
   * @since 3.5.2
   */
  public VarDeclSqlNode(String var, String exp, OgnlClassResolver classResolver) {
    this.name = var;
    this.expression = exp;
    this.classResolver = classResolver;
  }

  @Override
  public boolean apply(DynamicContext context) {
    final Object value = OgnlCache.getValue(expression, context.getBindings(), classResolver);
    context.bind(name, value);
    return true;
  }

}
