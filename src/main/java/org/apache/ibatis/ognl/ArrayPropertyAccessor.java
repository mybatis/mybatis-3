//--------------------------------------------------------------------------
//	Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.apache.ibatis.ognl;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * Implementation of PropertyAccessor that uses numbers and dynamic subscripts as
 * properties to index into Java arrays.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ArrayPropertyAccessor extends ObjectPropertyAccessor
    implements PropertyAccessor // This is here to make javadoc show this class as an implementor
{
  public Object getProperty(Map context, Object target, Object name) throws OgnlException {
    Object result = null;

    if (name instanceof String) {
      if (name.equals("length")) {
        result = new Integer(Array.getLength(target));
      } else {
        result = super.getProperty(context, target, name);
      }
    } else {
      Object index = name;

      if (index instanceof DynamicSubscript) {
        int len = Array.getLength(target);

        switch (((DynamicSubscript) index).getFlag()) {
          case DynamicSubscript.ALL:
            result = Array.newInstance(target.getClass().getComponentType(), len);
            System.arraycopy(target, 0, result, 0, len);
            break;
          case DynamicSubscript.FIRST:
            index = new Integer((len > 0) ? 0 : -1);
            break;
          case DynamicSubscript.MID:
            index = new Integer((len > 0) ? (len / 2) : -1);
            break;
          case DynamicSubscript.LAST:
            index = new Integer((len > 0) ? (len - 1) : -1);
            break;
        }
      }
      if (result == null) {
        if (index instanceof Number) {
          int i = ((Number) index).intValue();

          result = (i >= 0) ? Array.get(target, i) : null;
        } else {
          throw new NoSuchPropertyException(target, index);
        }
      }
    }
    return result;
  }

  public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
    Object index = name;
    boolean isNumber = (index instanceof Number);

    if (isNumber || (index instanceof DynamicSubscript)) {
      TypeConverter converter = ((OgnlContext) context).getTypeConverter();
      Object convertedValue;

      convertedValue = converter.convertValue(context, target, null, name.toString(), value, target.getClass().getComponentType());
      if (isNumber) {
        int i = ((Number) index).intValue();

        if (i >= 0) {
          Array.set(target, i, convertedValue);
        }
      } else {
        int len = Array.getLength(target);

        switch (((DynamicSubscript) index).getFlag()) {
          case DynamicSubscript.ALL:
            System.arraycopy(target, 0, convertedValue, 0, len);
            return;
          case DynamicSubscript.FIRST:
            index = new Integer((len > 0) ? 0 : -1);
            break;
          case DynamicSubscript.MID:
            index = new Integer((len > 0) ? (len / 2) : -1);
            break;
          case DynamicSubscript.LAST:
            index = new Integer((len > 0) ? (len - 1) : -1);
            break;
        }
      }
    } else {
      if (name instanceof String) {
        super.setProperty(context, target, name, value);
      } else {
        throw new NoSuchPropertyException(target, index);
      }
    }
  }
}
