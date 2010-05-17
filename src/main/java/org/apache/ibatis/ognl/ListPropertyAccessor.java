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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PropertyAccessor that uses numbers and dynamic subscripts as
 * properties to index into Lists.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ListPropertyAccessor extends ObjectPropertyAccessor
    implements PropertyAccessor // This is here to make javadoc show this class as an implementor
{
  public Object getProperty(Map context, Object target, Object name) throws OgnlException {
    List list = (List) target;

    if (name instanceof String) {
      Object result;

      if (name.equals("size")) {
        result = new Integer(list.size());
      } else {
        if (name.equals("iterator")) {
          result = list.iterator();
        } else {
          if (name.equals("isEmpty")) {
            result = list.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
          } else {
            result = super.getProperty(context, target, name);
          }
        }
      }
      return result;
    }

    if (name instanceof Number)
      return list.get(((Number) name).intValue());

    if (name instanceof DynamicSubscript) {
      int len = list.size();
      switch (((DynamicSubscript) name).getFlag()) {
        case DynamicSubscript.FIRST:
          return len > 0 ? list.get(0) : null;
        case DynamicSubscript.MID:
          return len > 0 ? list.get(len / 2) : null;
        case DynamicSubscript.LAST:
          return len > 0 ? list.get(len - 1) : null;
        case DynamicSubscript.ALL:
          return new ArrayList(list);
      }
    }

    throw new NoSuchPropertyException(target, name);
  }

  public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
    if (name instanceof String) {
      super.setProperty(context, target, name, value);
      return;
    }

    List list = (List) target;

    if (name instanceof Number) {
      list.set(((Number) name).intValue(), value);
      return;
    }

    if (name instanceof DynamicSubscript) {
      int len = list.size();
      switch (((DynamicSubscript) name).getFlag()) {
        case DynamicSubscript.FIRST:
          if (len > 0) list.set(0, value);
          return;
        case DynamicSubscript.MID:
          if (len > 0) list.set(len / 2, value);
          return;
        case DynamicSubscript.LAST:
          if (len > 0) list.set(len - 1, value);
          return;
        case DynamicSubscript.ALL: {
          if (!(value instanceof Collection))
            throw new OgnlException("Value must be a collection");
          list.clear();
          list.addAll((Collection) value);
          return;
        }
      }
    }

    throw new NoSuchPropertyException(target, name);
  }
}
