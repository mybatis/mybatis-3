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

import java.util.Map;

/**
 * Implementation of PropertyAccessor that sets and gets properties by storing and looking
 * up values in Maps.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class MapPropertyAccessor implements PropertyAccessor {
  public Object getProperty(Map context, Object target, Object name) throws OgnlException {
    Object result;
    Map map = (Map) target;
    Node currentNode = ((OgnlContext) context).getCurrentNode().jjtGetParent();
    boolean indexedAccess = false;

    if (currentNode == null) {
      throw new OgnlException("node is null for '" + name + "'");
    }
    if (!(currentNode instanceof ASTProperty)) {
      currentNode = currentNode.jjtGetParent();
    }
    if (currentNode instanceof ASTProperty) {
      indexedAccess = ((ASTProperty) currentNode).isIndexedAccess();
    }
    if ((name instanceof String) && !indexedAccess) {
      if (name.equals("size")) {
        result = new Integer(map.size());
      } else {
        if (name.equals("keys")) {
          result = map.keySet();
        } else {
          if (name.equals("values")) {
            result = map.values();
          } else {
            if (name.equals("isEmpty")) {
              result = map.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
            } else {
              result = map.get(name);
            }
          }
        }
      }
    } else {
      result = map.get(name);
    }
    return result;
  }

  public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
    Map map = (Map) target;
    map.put(name, value);
  }
}
