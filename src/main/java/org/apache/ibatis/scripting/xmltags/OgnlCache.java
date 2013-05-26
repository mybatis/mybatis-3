/*
 *    Copyright 2009-2012 the original author or authors.
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

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ognl.ExpressionSyntaxException;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlException;
import ognl.OgnlParser;
import ognl.ParseException;
import ognl.TokenMgrError;

import org.apache.ibatis.builder.BuilderException;

/**
 * 
 * Caches OGNL parsed expressions. Have a look at
 * http://code.google.com/p/mybatis/issues/detail?id=342
 * 
 */
public class OgnlCache {

  private static final Map<String, ognl.Node> expressionCache = new ConcurrentHashMap<String, ognl.Node>();

  public static Object getValue(String expression, Object root) {
    try {
      return Ognl.getValue(parseExpression(expression), root);
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
    }
  }

  private static Object parseExpression(String expression) throws OgnlException {
    try {
      Node node = expressionCache.get(expression);
      if (node == null) {
        node = new OgnlParser(new StringReader(expression)).topLevelExpression();
        expressionCache.put(expression, node);
      }
      return node;
    } catch (ParseException e) {
      throw new ExpressionSyntaxException(expression, e);
    } catch (TokenMgrError e) {
      throw new ExpressionSyntaxException(expression, e);
    }
  }

}
