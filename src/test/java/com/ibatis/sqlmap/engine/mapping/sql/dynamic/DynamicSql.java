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
package com.ibatis.sqlmap.engine.mapping.sql.dynamic;

import com.ibatis.sqlmap.engine.builder.InlineParameterMapParser;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.mapping.sql.SqlChild;
import com.ibatis.sqlmap.engine.mapping.sql.SqlText;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.*;
import com.ibatis.sqlmap.engine.mapping.sql.simple.SimpleDynamicSql;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DynamicSql implements Sql, DynamicParent {

  private List children = new ArrayList();

  private ThreadLocal<SqlTagContext> sqlTagContext = new ThreadLocal<SqlTagContext>();

  private Configuration configuration;
  private TypeHandlerRegistry typeHandlerRegistry;
  private List<ParameterMapping> parameterMappings;

  public DynamicSql(Configuration configuration) {
    this.parameterMappings = new ArrayList<ParameterMapping>();
    this.configuration = configuration;
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
  }

  public String getSql(Object parameterObject) {
    if (sqlTagContext.get() == null) {
      sqlTagContext.set(process(parameterObject));
    }
    return sqlTagContext.get().getDynamicSql();
  }

  public List<ParameterMapping> getParameterMappings(Object parameterObject) {
    if (sqlTagContext.get() == null) {
      sqlTagContext.set(process(parameterObject));
    }
    return sqlTagContext.get().getParameterMappings();
  }

  private SqlTagContext process(Object parameterObject) {
    SqlTagContext ctx = new SqlTagContext();
    List localChildren = children;
    processBodyChildren(ctx, parameterObject, localChildren.iterator());

    String dynSql = ctx.getBodyText();

    // Processes $substitutions$ after DynamicSql
    if (SimpleDynamicSql.isSimpleDynamicSql(dynSql)) {
      dynSql = new SimpleDynamicSql(dynSql, ctx.getParameterMappings(), typeHandlerRegistry).getSql(parameterObject);
    }

    ctx.setDynamicSql(dynSql);
    for (ParameterMapping mapping : parameterMappings) {
      ctx.addParameterMapping(mapping);
    }
    return ctx;
  }

  private void processBodyChildren(SqlTagContext ctx, Object parameterObject, Iterator localChildren) {
    PrintWriter out = ctx.getWriter();
    processBodyChildren(ctx, parameterObject, localChildren, out);
  }

  private void processBodyChildren(SqlTagContext ctx, Object parameterObject, Iterator localChildren, PrintWriter out) {
    while (localChildren.hasNext()) {
      SqlChild child = (SqlChild) localChildren.next();
      if (child instanceof SqlText) {
        SqlText sqlText = (SqlText) child;
        String sqlStatement = sqlText.getText();
        if (sqlText.isWhiteSpace()) {
          out.print(sqlStatement);
        } else if (!sqlText.isPostParseRequired()) {

          // BODY OUT
          out.print(sqlStatement);

          List<ParameterMapping> mappings = sqlText.getParameterMappings();
          if (mappings != null) {
            for (int i = 0, n = mappings.size(); i < n; i++) {
              ctx.addParameterMapping(mappings.get(i));
            }
          }
        } else {

          IterateContext itCtx = ctx.peekIterateContext();

          if (null != itCtx && itCtx.isAllowNext()) {
            itCtx.next();
            itCtx.setAllowNext(false);
            if (!itCtx.hasNext()) {
              itCtx.setFinal(true);
            }
          }

          if (itCtx != null) {
            StringBuffer sqlStatementBuffer = new StringBuffer(sqlStatement);
            iteratePropertyReplace(sqlStatementBuffer, itCtx);
            sqlStatement = sqlStatementBuffer.toString();
          }

          sqlText = new InlineParameterMapParser(configuration).parseInlineParameterMap(sqlStatement);

          List<ParameterMapping> mappings = sqlText.getParameterMappings();
          out.print(sqlText.getText());
          if (mappings != null) {
            for (int i = 0, n = mappings.size(); i < n; i++) {
              ctx.addParameterMapping(mappings.get(i));
            }
          }
        }
      } else if (child instanceof SqlTag) {
        SqlTag tag = (SqlTag) child;
        SqlTagHandler handler = tag.getHandler();
        int response;
        do {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);

          response = handler.doStartFragment(ctx, tag, parameterObject);
          if (response != SqlTagHandler.SKIP_BODY) {

            processBodyChildren(ctx, parameterObject, tag.getChildren(), pw);
            pw.flush();
            pw.close();
            StringBuffer body = sw.getBuffer();
            response = handler.doEndFragment(ctx, tag, parameterObject, body);
            handler.doPrepend(ctx, tag, parameterObject, body);

            if (response != SqlTagHandler.SKIP_BODY) {
              if (body.length() > 0) {
                out.print(body.toString());
              }
            }

          }
        } while (response == SqlTagHandler.REPEAT_BODY);

        ctx.popRemoveFirstPrependMarker(tag);

        if (ctx.peekIterateContext() != null && ctx.peekIterateContext().getTag() == tag) {
          ctx.setAttribute(ctx.peekIterateContext().getTag(), null);
          ctx.popIterateContext();
        }

      }
    }
  }

  protected void iteratePropertyReplace(StringBuffer bodyContent, IterateContext iterate) {
    if (iterate != null) {
      String[] mappings = new String[]{"#", "$"};
      for (String mapping : mappings) {
        int startIndex = 0;
        int endIndex = -1;
        while (startIndex > -1 && startIndex < bodyContent.length()) {
          startIndex = bodyContent.indexOf(mapping, endIndex + 1);
          endIndex = bodyContent.indexOf(mapping, startIndex + 1);
          if (startIndex > -1 && endIndex > -1) {
            String replacement = iterate.addIndexToTagProperty(bodyContent.substring(startIndex + 1, endIndex));
            bodyContent.replace(startIndex + 1, endIndex, replacement);
          }
        }
      }
    }
  }

  protected static void replace(StringBuffer buffer, String find, String replace) {
    int pos = buffer.toString().indexOf(find);
    int len = find.length();
    while (pos > -1) {
      buffer.replace(pos, pos + len, replace);
      pos = buffer.toString().indexOf(find);
    }
  }

  public void addChild(SqlChild child) {
    children.add(child);
  }

}
