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

import java.util.Map;
import org.apache.ibatis.reflection.SystemMetaObject;

public class IsPropertyAvailableTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    if (parameterObject == null) {
      return false;
    } else if (parameterObject instanceof Map) {
      return ((Map) parameterObject).containsKey(tag.getPropertyAttr());
    } else {
      String property = getResolvedProperty(ctx, tag);
      // if this is a compound property, then we need to get the next to the last
      // value from the parameter object, and then see if there is a readable property
      // for the last value.  This logic was added for IBATIS-281 and IBATIS-293
      int lastIndex = property.lastIndexOf('.');
      if (lastIndex != -1) {
        String firstPart = property.substring(0, lastIndex);
        String lastPart = property.substring(lastIndex + 1);
        parameterObject = SystemMetaObject.forObject(parameterObject).getValue(firstPart);
        property = lastPart;
      }

      if (parameterObject instanceof Map) {
        // we do this because the PROBE always returns true for 
        // properties in Maps and that's not the behavior we want here
        return ((Map) parameterObject).containsKey(property);
      } else {
        return SystemMetaObject.forObject(parameterObject).hasGetter(property);
      }
    }
  }
}
