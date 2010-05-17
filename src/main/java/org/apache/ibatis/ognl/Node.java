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
 * JJTree interface for AST nodes, as modified to handle the OGNL operations getValue and
 * setValue.  JJTree's original comment:
 * <p/>
 * All AST nodes must implement this interface.  It provides basic
 * machinery for constructing the parent and child relationships
 * between nodes.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public interface Node {

  /**
   * This method is called after the node has been made the current
   * node.  It indicates that child nodes can now be added to it.
   */
  public void jjtOpen();

  /**
   * This method is called after all the child nodes have been
   * added.
   */
  public void jjtClose();

  /**
   * This pair of methods are used to inform the node of its
   * parent.
   */
  public void jjtSetParent(Node n);

  public Node jjtGetParent();

  /**
   * This method tells the node to add its argument to the node's
   * list of children.
   */
  public void jjtAddChild(Node n, int i);

  /**
   * This method returns a child node.  The children are numbered
   * from zero, left to right.
   */
  public Node jjtGetChild(int i);

  /**
   * Return the number of children the node has.
   */
  public int jjtGetNumChildren();

// OGNL additions to Node:

  /**
   * Extracts the value from the given source object that is appropriate for this node
   * within the given context.
   */
  public Object getValue(OgnlContext context, Object source) throws OgnlException;

  /**
   * Sets the given value in the given target as appropriate for this node within the
   * given context.
   */
  public void setValue(OgnlContext context, Object target, Object value) throws OgnlException;
}
