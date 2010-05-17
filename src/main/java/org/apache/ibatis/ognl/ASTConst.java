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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
class ASTConst extends SimpleNode {
  private Object value;

  public ASTConst(int id) {
    super(id);
  }

  public ASTConst(OgnlParser p, int id) {
    super(p, id);
  }

  /**
   * Called from parser actions.
   */
  void setValue(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
    return this.value;
  }

  public boolean isNodeConstant(OgnlContext context) throws OgnlException {
    return true;
  }

  public String getEscapedChar(char ch) {
    String result;

    switch (ch) {
      case '\b':
        result = "\b";
        break;
      case '\t':
        result = "\\t";
        break;
      case '\n':
        result = "\\n";
        break;
      case '\f':
        result = "\\f";
        break;
      case '\r':
        result = "\\r";
        break;
      case '\"':
        result = "\\\"";
        break;
      case '\'':
        result = "\\\'";
        break;
      case '\\':
        result = "\\\\";
        break;
      default:
        if (Character.isISOControl(ch) || (ch > 255)) {
          String hc = Integer.toString((int) ch, 16);
          int hcl = hc.length();

          result = "\\u";
          if (hcl < 4) {
            if (hcl == 3) {
              result = result + "0";
            } else {
              if (hcl == 2) {
                result = result + "00";
              } else {
                result = result + "000";
              }
            }
          }

          result = result + hc;
        } else {
          result = new String(ch + "");
        }
        break;
    }
    return result;
  }

  public String getEscapedString(String value) {
    StringBuffer result = new StringBuffer();

    for (int i = 0, icount = value.length(); i < icount; i++) {
      result.append(getEscapedChar(value.charAt(i)));
    }
    return new String(result);
  }

  public String toString() {
    String result;

    if (value == null) {
      result = "null";
    } else {
      if (value instanceof String) {
        result = '\"' + getEscapedString(value.toString()) + '\"';
      } else {
        if (value instanceof Character) {
          result = '\'' + getEscapedChar(((Character) value).charValue()) + '\'';
        } else {
          result = value.toString();
          if (value instanceof Long) {
            result = result + "L";
          } else {
            if (value instanceof BigDecimal) {
              result = result + "B";
            } else {
              if (value instanceof BigInteger) {
                result = result + "H";
              } else {
                if (value instanceof Node) {
                  result = ":[ " + result + " ]";
                }
              }
            }
          }
        }
      }
    }
    return result;
  }
}
