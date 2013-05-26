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
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public class IterateTagHandler extends BaseTagHandler {

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    IterateContext iterate = (IterateContext) ctx.getAttribute(tag);
    if (iterate == null) {
      IterateContext parentIterate = ctx.peekIterateContext();

      ctx.pushRemoveFirstPrependMarker(tag);

      Object collection;
      String prop = tag.getPropertyAttr();
      if (prop != null && !prop.equals("")) {
        if (null != parentIterate && parentIterate.isAllowNext()) {
          parentIterate.next();
          parentIterate.setAllowNext(false);
          if (!parentIterate.hasNext()) {
            parentIterate.setFinal(true);
          }
        }

        if (parentIterate != null) {
          prop = parentIterate.addIndexToTagProperty(prop);
        }

        collection = SystemMetaObject.forObject(parameterObject).getValue(prop);
      } else {
        collection = parameterObject;
      }
      iterate = new IterateContext(collection, tag, parentIterate);

      iterate.setProperty(null == prop ? "" : prop);

      ctx.setAttribute(tag, iterate);
      ctx.pushIterateContext(iterate);
    } else if ("iterate".equals(tag.getRemoveFirstPrepend())) {
      ctx.reEnableRemoveFirstPrependMarker();
    }

    if (iterate.hasNext()) {
      return INCLUDE_BODY;
    } else {
      return SKIP_BODY;
    }
  }

  public int doEndFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {
    IterateContext iterate = (IterateContext) ctx.getAttribute(tag);

    if (iterate.hasNext() || iterate.isFinal()) {

      if (iterate.isAllowNext()) {
        iterate.next();
      }

      if (bodyContent.toString().trim().length() > 0) {
        // the sub element produced a result.  If it is the first one
        // to produce a result, then we need to add the open
        // text.  If it is not the first to produce a result then
        // we need to add the conjunction text
        if (iterate.someSubElementsHaveContent()) {
          if (tag.isConjunctionAvailable()) {
            bodyContent.insert(0, tag.getConjunctionAttr());
          }
        } else {
          // we need to specify that this is the first content
          // producing element so that the doPrepend method will
          // add the prepend
          iterate.setPrependEnabled(true);

          if (tag.isOpenAvailable()) {
            bodyContent.insert(0, tag.getOpenAttr());
          }
        }
        iterate.setSomeSubElementsHaveContent(true);
      }

      if (iterate.isLast() && iterate.someSubElementsHaveContent()) {
        if (tag.isCloseAvailable()) {
          bodyContent.append(tag.getCloseAttr());
        }
      }

      iterate.setAllowNext(true);
      if (iterate.isFinal()) {
        return super.doEndFragment(ctx, tag, parameterObject, bodyContent);
      } else {
        return REPEAT_BODY;
      }

    } else {
      return super.doEndFragment(ctx, tag, parameterObject, bodyContent);
    }
  }

  public void doPrepend(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {
    IterateContext iterate = (IterateContext) ctx.getAttribute(tag);
    if (iterate.isPrependEnabled()) {
      super.doPrepend(ctx, tag, parameterObject, bodyContent);
      iterate.setPrependEnabled(false);  // only do the prepend one time
    }
  }

  public boolean isPostParseRequired() {
    return true;
  }

}

