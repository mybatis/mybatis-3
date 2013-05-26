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

public abstract class BaseTagHandler implements SqlTagHandler {

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    ctx.pushRemoveFirstPrependMarker(tag);
    return INCLUDE_BODY;
  }

  public int doEndFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {
    if (tag.isCloseAvailable() && !(tag.getHandler() instanceof IterateTagHandler)) {
      if (bodyContent.toString().trim().length() > 0) {
        bodyContent.append(tag.getCloseAttr());
      }
    }
    return INCLUDE_BODY;
  }

  public void doPrepend(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {

    if (tag.isOpenAvailable() && !(tag.getHandler() instanceof IterateTagHandler)) {
      if (bodyContent.toString().trim().length() > 0) {
        bodyContent.insert(0, tag.getOpenAttr());
      }
    }

    if (tag.isPrependAvailable()) {
      if (bodyContent.toString().trim().length() > 0) {
        if (tag.getParent() != null && ctx.peekRemoveFirstPrependMarker(tag)) {
          ctx.disableRemoveFirstPrependMarker();
        } else {
          bodyContent.insert(0, tag.getPrependAttr());
        }
      }
    }

  }
}
