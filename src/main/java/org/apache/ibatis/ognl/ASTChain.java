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

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
class ASTChain extends SimpleNode {
  public ASTChain(int id) {
    super(id);
  }

  public ASTChain(OgnlParser p, int id) {
    super(p, id);
  }

  public void jjtClose() {
    flattenTree();
  }

  protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
    Object result = source;

    for (int i = 0, ilast = children.length - 1; i <= ilast; ++i) {
      boolean handled = false;

      if (i < ilast) {
        if (children[i] instanceof ASTProperty) {
          ASTProperty propertyNode = (ASTProperty) children[i];
          int indexType = propertyNode.getIndexedPropertyType(context, result);

          if ((indexType != OgnlRuntime.INDEXED_PROPERTY_NONE) && (children[i + 1] instanceof ASTProperty)) {
            ASTProperty indexNode = (ASTProperty) children[i + 1];

            if (indexNode.isIndexedAccess()) {
              Object index = indexNode.getProperty(context, result);

              if (index instanceof DynamicSubscript) {
                if (indexType == OgnlRuntime.INDEXED_PROPERTY_INT) {
                  Object array = propertyNode.getValue(context, result);
                  int len = Array.getLength(array);

                  switch (((DynamicSubscript) index).getFlag()) {
                    case DynamicSubscript.ALL:
                      result = Array.newInstance(array.getClass().getComponentType(), len);
                      System.arraycopy(array, 0, result, 0, len);
                      handled = true;
                      i++;
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
                } else {
                  if (indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT) {
                    throw new OgnlException("DynamicSubscript '" + indexNode + "' not allowed for object indexed property '" + propertyNode + "'");
                  }
                }
              }
              if (!handled) {
                result = OgnlRuntime.getIndexedProperty(context, result, propertyNode.getProperty(context, result).toString(), index);
                handled = true;
                i++;
              }
            }
          }
        }
      }
      if (!handled) {
        result = children[i].getValue(context, result);
      }
    }
    return result;
  }

  protected void setValueBody(OgnlContext context, Object target, Object value) throws OgnlException {
    boolean handled = false;

    for (int i = 0, ilast = children.length - 2; i <= ilast; ++i) {
      if (i == ilast) {
        if (children[i] instanceof ASTProperty) {
          ASTProperty propertyNode = (ASTProperty) children[i];
          int indexType = propertyNode.getIndexedPropertyType(context, target);

          if ((indexType != OgnlRuntime.INDEXED_PROPERTY_NONE) && (children[i + 1] instanceof ASTProperty)) {
            ASTProperty indexNode = (ASTProperty) children[i + 1];

            if (indexNode.isIndexedAccess()) {
              Object index = indexNode.getProperty(context, target);

              if (index instanceof DynamicSubscript) {
                if (indexType == OgnlRuntime.INDEXED_PROPERTY_INT) {
                  Object array = propertyNode.getValue(context, target);
                  int len = Array.getLength(array);

                  switch (((DynamicSubscript) index).getFlag()) {
                    case DynamicSubscript.ALL:
                      System.arraycopy(target, 0, value, 0, len);
                      handled = true;
                      i++;
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
                } else {
                  if (indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT) {
                    throw new OgnlException("DynamicSubscript '" + indexNode + "' not allowed for object indexed property '" + propertyNode + "'");
                  }
                }
              }
              if (!handled) {
                OgnlRuntime.setIndexedProperty(context, target, propertyNode.getProperty(context, target).toString(), index, value);
                handled = true;
                i++;
              }
            }
          }
        }
      }
      if (!handled) {
        target = children[i].getValue(context, target);
      }
    }
    if (!handled) {
      children[children.length - 1].setValue(context, target, value);
    }
  }

  public boolean isSimpleNavigationChain(OgnlContext context) throws OgnlException {
    boolean result = false;

    if ((children != null) && (children.length > 0)) {
      result = true;
      for (int i = 0; result && (i < children.length); i++) {
        if (children[i] instanceof SimpleNode) {
          result = ((SimpleNode) children[i]).isSimpleProperty(context);
        } else {
          result = false;
        }
      }
    }
    return result;
  }

  public String toString() {
    String result = "";

    if ((children != null) && (children.length > 0)) {
      for (int i = 0; i < children.length; i++) {
        if (i > 0) {
          if (!(children[i] instanceof ASTProperty) || !((ASTProperty) children[i]).isIndexedAccess()) {
            result = result + ".";
          }
        }
        result += children[i].toString();
      }
    }
    return result;
  }
}
