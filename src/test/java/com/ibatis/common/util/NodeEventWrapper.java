package com.ibatis.common.util;

import org.apache.ibatis.parsing.*;

import java.lang.reflect.Method;

public class NodeEventWrapper {

  private Object nodeletTarget;
  private Method method;

  public NodeEventWrapper(Object nodeletTarget, Method method) {
    this.nodeletTarget = nodeletTarget;
    this.method = method;
  }

  public void process(XNode context) {
    try {
      method.invoke(nodeletTarget, new Object[]{context});
    } catch (Exception e) {
      throw new ParsingException("Error processing node " + context.getNode().getNodeName() + ". Cause: " + e, e);
    }
  }

}
