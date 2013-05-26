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
import org.apache.ibatis.reflection.MetaObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.ibatis.reflection.SystemMetaObject;

public abstract class ConditionalTagHandler extends BaseTagHandler {

  public static final long NOT_COMPARABLE = Long.MIN_VALUE;
  private static final String DATE_MASK = "yyyy/MM/dd hh:mm:ss";

  private static final String START_INDEX = "[";

  public abstract boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject);

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject) {

    ctx.pushRemoveFirstPrependMarker(tag);

    if (isCondition(ctx, tag, parameterObject)) {
      return INCLUDE_BODY;
    } else {
      return SKIP_BODY;
    }
  }

  public int doEndFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {

    IterateContext iterate = ctx.peekIterateContext();

    if (null != iterate && iterate.isAllowNext()) {
      iterate.next();
      iterate.setAllowNext(false);
      if (!iterate.hasNext()) {
        iterate.setFinal(true);
      }
    }

    //iteratePropertyReplace(bodyContent,ctx.peekIterateContext());

    return super.doEndFragment(ctx, tag, parameterObject, bodyContent);
  }

  protected long compare(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    String comparePropertyName = tag.getComparePropertyAttr();
    String compareValue = tag.getCompareValueAttr();

    String prop = getResolvedProperty(ctx, tag);
    Object value1;
    Class type;

    MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
    if (prop != null) {
      value1 = metaObject.getValue(prop);
      type = metaObject.getGetterType(prop);
    } else {
      value1 = parameterObject;
      if (value1 != null) {
        type = parameterObject.getClass();
      } else {
        type = Object.class;
      }
    }
    if (comparePropertyName != null) {
      Object value2 = metaObject.getValue(comparePropertyName);
      return compareValues(type, value1, value2);
    } else if (compareValue != null) {
      return compareValues(type, value1, compareValue);
    } else {
      throw new RuntimeException("Error comparing in conditional fragment.  Uknown 'compare to' values.");
    }
  }

  protected long compareValues(Class type, Object value1, Object value2) {
    long result;

    if (value1 == null || value2 == null) {
      result = value1 == value2 ? 0 : NOT_COMPARABLE;
    } else {
      if (value2.getClass() != type) {
        value2 = convertValue(type, value2.toString());
      }
      if (value2 instanceof String && type != String.class) {
        value1 = value1.toString();
      }
      if (!(value1 instanceof Comparable && value2 instanceof Comparable)) {
        value1 = value1.toString();
        value2 = value2.toString();
      }
      result = ((Comparable) value1).compareTo(value2);
    }

    return result;
  }

  protected Object convertValue(Class type, String value) {
    if (type == String.class) {
      return value;
    } else if (type == Byte.class || type == byte.class) {
      return Byte.valueOf(value);
    } else if (type == Short.class || type == short.class) {
      return Short.valueOf(value);
    } else if (type == Character.class || type == char.class) {
      return value.charAt(0);
    } else if (type == Integer.class || type == int.class) {
      return Integer.valueOf(value);
    } else if (type == Long.class || type == long.class) {
      return Long.valueOf(value);
    } else if (type == Float.class || type == float.class) {
      return Float.valueOf(value);
    } else if (type == Double.class || type == double.class) {
      return Double.valueOf(value);
    } else if (type == Boolean.class || type == boolean.class) {
      return Boolean.valueOf(value);
    } else if (type == Date.class) {
      return format(DATE_MASK, value);
    } else if (type == BigInteger.class) {
      return new BigInteger(value);
    } else if (type == BigDecimal.class) {
      return new BigDecimal(value);
    } else {
      return value;
    }

  }

  private static Date format(String format, String datetime) {
    try {
      return new SimpleDateFormat(format).parse(datetime);
    } catch (ParseException e) {
      throw new SqlMapException("Error parsing default null value date.  Format must be '" + format + "'. Cause: " + e);
    }
  }

  /*
   * This method will add the proper index values to an indexed property
   * string if we are inside an iterate tag
   *
   * @param ctx
   * @param tag
   */
  protected String getResolvedProperty(SqlTagContext ctx, SqlTag tag) {
    String prop = tag.getPropertyAttr();
    IterateContext itCtx = ctx.peekIterateContext();

    if (prop != null) {

      if (null != itCtx && itCtx.isAllowNext()) {
        itCtx.next();
        itCtx.setAllowNext(false);
        if (!itCtx.hasNext()) {
          itCtx.setFinal(true);
        }
      }

      if (prop.indexOf(START_INDEX) > -1) {
        if (itCtx != null) {
          prop = itCtx.addIndexToTagProperty(prop);
        }
      }
    }

    return prop;
  }
}
