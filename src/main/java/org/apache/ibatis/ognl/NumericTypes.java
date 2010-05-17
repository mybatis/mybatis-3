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
 * This interface defines some useful constants for describing the various possible
 * numeric types of OGNL.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public interface NumericTypes {
  // Order does matter here... see the getNumericType methods in ognl.g.

  /**
   * Type tag meaning boolean.
   */
  int BOOL = 0;
  /**
   * Type tag meaning byte.
   */
  int BYTE = 1;
  /**
   * Type tag meaning char.
   */
  int CHAR = 2;
  /**
   * Type tag meaning short.
   */
  int SHORT = 3;
  /**
   * Type tag meaning int.
   */
  int INT = 4;
  /**
   * Type tag meaning long.
   */
  int LONG = 5;
  /**
   * Type tag meaning java.math.BigInteger.
   */
  int BIGINT = 6;
  /**
   * Type tag meaning float.
   */
  int FLOAT = 7;
  /**
   * Type tag meaning double.
   */
  int DOUBLE = 8;
  /**
   * Type tag meaning java.math.BigDecimal.
   */
  int BIGDEC = 9;
  /**
   * Type tag meaning something other than a number.
   */
  int NONNUMERIC = 10;

  /**
   * The smallest type tag that represents reals as opposed to integers.  You can see
   * whether a type tag represents reals or integers by comparing the tag to this
   * constant: all tags less than this constant represent integers, and all tags
   * greater than or equal to this constant represent reals.  Of course, you must also
   * check for NONNUMERIC, which means it is not a number at all.
   */
  int MIN_REAL_TYPE = FLOAT;
}
