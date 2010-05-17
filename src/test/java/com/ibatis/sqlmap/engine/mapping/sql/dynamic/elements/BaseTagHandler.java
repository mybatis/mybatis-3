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
