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

import java.util.Enumeration;
import java.util.Map;

/**
 * Implementation of PropertyAccessor that provides "property" reference to
 * "nextElement" (aliases to "next" also) and "hasMoreElements" (also aliased
 * to "hasNext").
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class EnumerationPropertyAccessor extends ObjectPropertyAccessor
    implements PropertyAccessor // This is here to make javadoc show this class as an implementor
{
  public Object getProperty(Map context, Object target, Object name) throws OgnlException {
    Object result;
    Enumeration e = (Enumeration) target;

    if (name instanceof String) {
      if (name.equals("next") || name.equals("nextElement")) {
        result = e.nextElement();
      } else {
        if (name.equals("hasNext") || name.equals("hasMoreElements")) {
          result = e.hasMoreElements() ? Boolean.TRUE : Boolean.FALSE;
        } else {
          result = super.getProperty(context, target, name);
        }
      }
    } else {
      result = super.getProperty(context, target, name);
    }
    return result;
  }

  public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
    throw new IllegalArgumentException("can't set property " + name + " on Enumeration");
  }
}
