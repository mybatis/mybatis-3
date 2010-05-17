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

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public abstract class ExpressionNode extends SimpleNode {
  public ExpressionNode(int i) {
    super(i);
  }

  public ExpressionNode(OgnlParser p, int i) {
    super(p, i);
  }

  /**
   * Returns true iff this node is constant without respect to the children.
   */
  public boolean isNodeConstant(OgnlContext context) throws OgnlException {
    return false;
  }

  public boolean isConstant(OgnlContext context) throws OgnlException {
    boolean result = isNodeConstant(context);

    if ((children != null) && (children.length > 0)) {
      result = true;
      for (int i = 0; result && (i < children.length); ++i) {
        if (children[i] instanceof SimpleNode) {
          result = ((SimpleNode) children[i]).isConstant(context);
        } else {
          result = false;
        }
      }
    }
    return result;
  }

  public String getExpressionOperator(int index) {
    throw new RuntimeException("unknown operator for " + OgnlParserTreeConstants.jjtNodeName[id]);
  }

  public String toString() {
    String result;

    result = (parent == null) ? "" : "(";
    if ((children != null) && (children.length > 0)) {
      for (int i = 0; i < children.length; ++i) {
        if (i > 0) {
          result += " " + getExpressionOperator(i) + " ";
        }
        result += children[i].toString();
      }
    }
    if (parent != null) {
      result = result + ")";
    }
    return result;
  }
}
