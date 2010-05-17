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
 * This class has predefined instances that stand for OGNL's special "dynamic subscripts"
 * for getting at the first, middle, or last elements of a list.  In OGNL expressions,
 * these subscripts look like special kinds of array indexes: [^] means the first element,
 * [$] means the last, [|] means the middle, and [*] means the whole list.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class DynamicSubscript {
  public static final int FIRST = 0;
  public static final int MID = 1;
  public static final int LAST = 2;
  public static final int ALL = 3;

  public static final DynamicSubscript first = new DynamicSubscript(FIRST);
  public static final DynamicSubscript mid = new DynamicSubscript(MID);
  public static final DynamicSubscript last = new DynamicSubscript(LAST);
  public static final DynamicSubscript all = new DynamicSubscript(ALL);

  private int flag;

  private DynamicSubscript(int flag) {
    this.flag = flag;
  }

  public int getFlag() {
    return flag;
  }

  public String toString() {
    switch (flag) {
      case FIRST:
        return "^";
      case MID:
        return "|";
      case LAST:
        return "$";
      case ALL:
        return "*";
      default:
        return "?"; // Won't happen
    }
  }
}
