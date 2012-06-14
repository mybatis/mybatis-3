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

import ognl.OgnlException;
import org.apache.ibatis.builder.BuilderException;

public class VarDeclSqlNode implements SqlNode {

  private final String name;

  private final String expression;

  public VarDeclSqlNode(String var, String exp) {
    name = var;
    expression = exp;
  }

  public boolean apply(DynamicContext context) {
    try {
      final Object value = OgnlCache.getValue(expression, context.getBindings());
      context.bind(name, value);
      return true;
    } catch (OgnlException ex) {
      throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + ex.getMessage(), ex);
    }
  }

}
