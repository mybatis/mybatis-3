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


import com.ibatis.sqlmap.client.SqlMapException;

import java.lang.reflect.Array;
import java.util.*;

public class IterateContext implements Iterator {

  private static final String PROCESS_INDEX = "ProcessIndex";
  private static final String PROCESS_STRING = "ProcessString";

  private Iterator iterator;
  private int index = -1;

  private String property;
  private boolean allowNext = true;

  private boolean isFinal = false;
  private SqlTag tag;

  private IterateContext parent;

  /*
   * This variable is true if some of the sub elements have
   * actually produced content.  This is used to test
   * whether to add the open and conjunction text to the
   * generated statement.
   * <p/>
   * This variable is used to replace the deprecated and dangerous
   * isFirst method.
   */
  private boolean someSubElementsHaveContent;

  /*
   * This variable is set by the doEndFragment method in IterateTagHandler
   * to specify that the first content producing sub element has happened.
   * The doPrepend method will test the value to know whether or not
   * to process the prepend.
   * <p/>
   * This variable is used to replace the deprecated and dangerous
   * isFirst method.
   */
  private boolean isPrependEnabled;

  public IterateContext(Object collection, SqlTag tag, IterateContext parent) {
    this.parent = parent;
    this.tag = tag;
    if (collection instanceof Collection) {
      this.iterator = ((Collection) collection).iterator();
    } else if (collection instanceof Iterator) {
      this.iterator = ((Iterator) collection);
    } else if (collection.getClass().isArray()) {
      List list = arrayToList(collection);
      this.iterator = list.iterator();
    } else {
      throw new SqlMapException("ParameterObject or property was not a Collection, Array or Iterator.");
    }
  }

  public boolean hasNext() {
    return iterator != null && iterator.hasNext();
  }

  public Object next() {
    index++;
    return iterator.next();
  }

  public void remove() {
    iterator.remove();
  }

  public int getIndex() {
    return index;
  }

  /*
   * @return
   * @deprecated This method should not be used to decide whether or not to
   *             add prepend and open text to the generated statement.  Rather, use the
   *             methods isPrependEnabled() and someSubElementsHaveContent().
   */
  public boolean isFirst() {
    return index == 0;
  }

  public boolean isLast() {
    return iterator != null && !iterator.hasNext();
  }

  private List arrayToList(Object array) {
    List list = null;
    if (array instanceof Object[]) {
      list = Arrays.asList((Object[]) array);
    } else {
      list = new ArrayList();
      for (int i = 0, n = Array.getLength(array); i < n; i++) {
        list.add(Array.get(array, i));
      }
    }
    return list;
  }

  /*
   * @return Returns the property.
   */
  public String getProperty() {
    return property;
  }

  /*
   * This property specifies whether to increment the iterate in
   * the doEndFragment. The ConditionalTagHandler has the ability
   * to increment the IterateContext, so it is neccessary to avoid
   * incrementing in both the ConditionalTag and the IterateTag.
   *
   * @param property The property to set.
   */
  public void setProperty(String property) {
    this.property = property;
  }

  /*
   * @return Returns the allowNext.
   */
  public boolean isAllowNext() {
    return allowNext;
  }

  /*
   * @param performIterate The allowNext to set.
   */
  public void setAllowNext(boolean performIterate) {
    this.allowNext = performIterate;
  }

  /*
   * @return Returns the tag.
   */
  public SqlTag getTag() {
    return tag;
  }

  /*
   * @param tag The tag to set.
   */
  public void setTag(SqlTag tag) {
    this.tag = tag;
  }

  /*
   * @return
   */
  public boolean isFinal() {
    return isFinal;
  }

  /*
   * This attribute is used to mark whether an iterate tag is
   * in it's final iteration. Since the ConditionalTagHandler
   * can increment the iterate the final iterate in the doEndFragment
   * of the IterateTagHandler needs to know it is in it's final iterate.
   *
   * @param aFinal
   */
  public void setFinal(boolean aFinal) {
    isFinal = aFinal;
  }


  /*
   * Returns the last property of any bean specified in this IterateContext.
   *
   * @return The last property of any bean specified in this IterateContext.
   */
  public String getEndProperty() {
    if (parent != null) {
      int parentPropertyIndex = property.indexOf(parent.getProperty());
      if (parentPropertyIndex > -1) {
        int endPropertyIndex1 = property.indexOf(']', parentPropertyIndex);
        int endPropertyIndex2 = property.indexOf('.', parentPropertyIndex);
        return property.substring(parentPropertyIndex + Math.max(endPropertyIndex1, endPropertyIndex2) + 1, property.length());
      } else {
        return property;
      }
    } else {
      return property;
    }
  }

  /*
   * Replaces value of a tag property to match it's value with current iteration and all other iterations.
   *
   * @param tagProperty the property of a TagHandler.
   * @return A Map containing the modified tag property in PROCESS_STRING key and the index where the modification occured in PROCESS_INDEX key.
   */
  protected Map processTagProperty(String tagProperty) {
    if (parent != null) {
      Map parentResult = parent.processTagProperty(tagProperty);
      return this.addIndex((String) parentResult.get(PROCESS_STRING), (Integer) parentResult.get(PROCESS_INDEX));
    } else {
      return this.addIndex(tagProperty, 0);
    }
  }

  /*
   * Replaces value of a tag property to match it's value with current iteration and all other iterations.
   *
   * @param tagProperty the property of a TagHandler.
   * @return The tag property with all "[]" replaced with the correct iteration value.
   */
  public String addIndexToTagProperty(String tagProperty) {
    Map map = this.processTagProperty(tagProperty);
    return (String) map.get(PROCESS_STRING);
  }

  /*
   * Adds index value to the first found property matching this Iteration starting at index startIndex.
   *
   * @param input      The input String.
   * @param startIndex The index where search for property begins.
   * @return A Map containing the modified tag property in PROCESS_STRING key and the index where the modification occured in PROCESS_INDEX key.
   */
  protected Map addIndex(String input, int startIndex) {
    if (input != null && input.startsWith("[")) {
      input = "_collection" + input;
    }
    String endProperty = getEndProperty() + "[";
    int propertyIndex = input.indexOf(endProperty, startIndex);
    int modificationIndex = 0;
    // Is the iterate property in the tag property at all?
    if (propertyIndex > -1) {
      // Make sure the tag property does not already have a number.
      if (input.charAt(propertyIndex + endProperty.length()) == ']') {
        // Add iteration number to property.
        input = input.substring(0, propertyIndex + endProperty.length()) + this.getIndex() + input.substring(propertyIndex + endProperty.length());
        modificationIndex = propertyIndex + endProperty.length();
      }
    }
    Map ret = new HashMap();
    ret.put(PROCESS_INDEX, new Integer(modificationIndex));
    ret.put(PROCESS_STRING, input);
    return ret;
  }


  public IterateContext getParent() {
    return parent;
  }

  public void setParent(IterateContext parent) {
    this.parent = parent;
  }

  public boolean someSubElementsHaveContent() {
    return someSubElementsHaveContent;
  }

  public void setSomeSubElementsHaveContent(boolean someSubElementsHaveContent) {
    this.someSubElementsHaveContent = someSubElementsHaveContent;
  }

  public boolean isPrependEnabled() {
    return isPrependEnabled;
  }

  public void setPrependEnabled(boolean isPrependEnabled) {
    this.isPrependEnabled = isPrependEnabled;
  }
}