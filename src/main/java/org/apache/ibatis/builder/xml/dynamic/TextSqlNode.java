/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.builder.xml.dynamic;

import ognl.OgnlException;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.type.SimpleTypeRegistry;

public class TextSqlNode implements SqlNode {
  private String text;

  public TextSqlNode(String text) {
    this.text = text;
  }

  public boolean apply(DynamicContext context) {
    GenericTokenParser parser = new GenericTokenParser("${", "}", new BindingTokenParser(context));
    context.appendSql(parser.parse(text));
    return true;
  }

  private static class BindingTokenParser implements TokenHandler {

    private DynamicContext context;

    public BindingTokenParser(DynamicContext context) {
      this.context = context;
    }

    public String handleToken(String content) {
      try {
        Object parameter = context.getBindings().get("_parameter");
        if (parameter == null) {
          context.getBindings().put("value", null);
        } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
          context.getBindings().put("value", parameter);
        }
        Object value = OgnlCache.getValue(content, context.getBindings());
        return (value == null ? "" : String.valueOf(value)); // issue #274 return "" instead of "null"
      } catch (OgnlException e) {
        throw new BuilderException("Error evaluating expression '" + content + "'. Cause: " + e, e);
      }
    }
  }

}