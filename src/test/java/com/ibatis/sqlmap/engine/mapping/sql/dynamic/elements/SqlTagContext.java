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

import org.apache.ibatis.mapping.ParameterMapping;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SqlTagContext {

  private StringWriter sw;
  private PrintWriter out;

  private HashMap attributes;

  private LinkedList removeFirstPrependStack;
  private LinkedList iterateContextStack;

  private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
  private String dynamicSql;


  public SqlTagContext() {
    sw = new StringWriter();
    out = new PrintWriter(sw);
    attributes = new HashMap();
    removeFirstPrependStack = new LinkedList();
    iterateContextStack = new LinkedList();
  }

  public PrintWriter getWriter() {
    return out;
  }

  public String getBodyText() {
    out.flush();
    return sw.getBuffer().toString();
  }

  public void setAttribute(Object key, Object value) {
    attributes.put(key, value);
  }

  public Object getAttribute(Object key) {
    return attributes.get(key);
  }

  public void addParameterMapping(ParameterMapping mapping) {
    parameterMappings.add(mapping);
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

  public boolean isEmptyRemoveFirtPrepend() {
    return removeFirstPrependStack.size() <= 0;
  }

  /*
   * examine the value of the top RemoveFirstPrependMarker object on the stack.
   *
   * @param sqlTag
   * @return was the first prepend removed
   */
  public boolean peekRemoveFirstPrependMarker(SqlTag sqlTag) {

    RemoveFirstPrependMarker removeFirstPrepend =
        (RemoveFirstPrependMarker) removeFirstPrependStack.get(1);

    return removeFirstPrepend.isRemoveFirstPrepend();
  }

  /*
   * pop the first RemoveFirstPrependMarker once the recursion is on it's way out
   * of the recursion loop and return it's internal value.
   *
   * @param tag
   */
  public void popRemoveFirstPrependMarker(SqlTag tag) {

    RemoveFirstPrependMarker removeFirstPrepend =
        (RemoveFirstPrependMarker) removeFirstPrependStack.getFirst();

    if (tag == removeFirstPrepend.getSqlTag()) {
      removeFirstPrependStack.removeFirst();
    }
  }

  /*
   * push a new RemoveFirstPrependMarker object with the specified internal state
   *
   * @param tag
   */
  public void pushRemoveFirstPrependMarker(SqlTag tag) {

    if (tag.getHandler() instanceof DynamicTagHandler) {
      // this was added to retain default behavior
      if (tag.isPrependAvailable()) {
        removeFirstPrependStack.addFirst(
            new RemoveFirstPrependMarker(tag, true));
      } else {
        removeFirstPrependStack.addFirst(
            new RemoveFirstPrependMarker(tag, false));
      }
    } else if ("true".equals(tag.getRemoveFirstPrepend())
        || "iterate".equals(tag.getRemoveFirstPrepend())) {
      // you must be specific about the removal otherwise it
      // will function as ibatis has always functioned and add
      // the prepend
      removeFirstPrependStack.addFirst(
          new RemoveFirstPrependMarker(tag, true));
    } else if (!tag.isPrependAvailable() &&
        !"true".equals(tag.getRemoveFirstPrepend()) &&
        !"iterate".equals(tag.getRemoveFirstPrepend()) &&
        tag.getParent() != null) {
      // if no prepend or removeFirstPrepend is specified 
      // we need to look to the parent tag for default values
      if ("true".equals(tag.getParent().getRemoveFirstPrepend())
          || "iterate".equals(tag.getParent().getRemoveFirstPrepend())) {
        removeFirstPrependStack.addFirst(
            new RemoveFirstPrependMarker(tag, true));
      }
    } else {
      removeFirstPrependStack.addFirst(
          new RemoveFirstPrependMarker(tag, false));
    }

  }

  /*
   * set a new internal state for top RemoveFirstPrependMarker object
   */
  public void disableRemoveFirstPrependMarker() {
    ((RemoveFirstPrependMarker) removeFirstPrependStack.get(1)).setRemoveFirstPrepend(false);
  }

  public void reEnableRemoveFirstPrependMarker() {
    ((RemoveFirstPrependMarker) removeFirstPrependStack.get(0)).setRemoveFirstPrepend(true);
  }

  /*
   * iterate context is stored here for nested dynamic tags in
   * the body of the iterate tag
   *
   * @param iterateContext
   */
  public void pushIterateContext(IterateContext iterateContext) {
    iterateContextStack.addFirst(iterateContext);
  }

  /*
   * iterate context is removed here from the stack when iterate tag is finished being
   * processed
   *
   * @return the top element of the context stack
   */
  public IterateContext popIterateContext() {
    IterateContext retVal = null;
    if (!iterateContextStack.isEmpty()) {
      retVal = (IterateContext) iterateContextStack.removeFirst();
    }
    return retVal;
  }

  /*
   * iterate context is removed here from the stack when iterate tag is finished being
   * processed
   *
   * @return the top element on the context stack
   */
  public IterateContext peekIterateContext() {
    IterateContext retVal = null;
    if (!iterateContextStack.isEmpty()) {
      retVal = (IterateContext) iterateContextStack.getFirst();
    }
    return retVal;
  }


  public void setDynamicSql(String dynamicSql) {
    this.dynamicSql = dynamicSql;
  }

  public String getDynamicSql() {
    return dynamicSql;
  }

}

