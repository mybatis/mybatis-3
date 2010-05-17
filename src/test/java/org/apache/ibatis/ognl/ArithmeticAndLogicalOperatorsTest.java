//--------------------------------------------------------------------------
//  Copyright (c) 2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//  Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//  Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
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

import junit.framework.TestSuite;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ArithmeticAndLogicalOperatorsTest extends OgnlTestCase {
  private static Object[][] TESTS = {
      // Double-valued arithmetic expressions
      {"-1d", new Double(-1)},
      {"+1d", new Double(1)},
      {"--1f", new Float(1)},
      {"2*2.0", new Double(4)},
      {"5/2.", new Double(2.5)},
      {"5+2D", new Double(7)},
      {"5f-2F", new Float(3)},
      {"5.+2*3", new Double(11)},
      {"(5.+2)*3", new Double(21)},

      // BigDecimal-valued arithmetic expressions
      {"-1b", new BigDecimal(-1)},
      {"+1b", new BigDecimal(1)},
      {"--1b", new BigDecimal(1)},
      {"2*2.0b", new BigDecimal("4.0")},
      {"5/2.B", new BigDecimal(2)},
      {"5.0B/2", new BigDecimal(2.5)},
      {"5+2b", new BigDecimal(7)},
      {"5-2B", new BigDecimal(3)},
      {"5.+2b*3", new BigDecimal(11)},
      {"(5.+2b)*3", new BigDecimal(21)},

      // Integer-valued arithmetic expressions
      {"-1", new Integer(-1)},
      {"+1", new Integer(1)},
      {"--1", new Integer(1)},
      {"2*2", new Integer(4)},
      {"5/2", new Integer(2)},
      {"5+2", new Integer(7)},
      {"5-2", new Integer(3)},
      {"5+2*3", new Integer(11)},
      {"(5+2)*3", new Integer(21)},
      {"~1", new Integer(~1)},
      {"5%2", new Integer(1)},
      {"5<<2", new Integer(20)},
      {"5>>2", new Integer(1)},
      {"5>>1+1", new Integer(1)},
      {"-5>>>2", new Integer(-5 >>> 2)},
      {"-5L>>>2", new Long(-5L >>> 2)},
      {"5. & 3", new Double(1)},
      {"5 ^3", new Integer(6)},
      {"5l&3|5^3", new Long(7)},
      {"5&(3|5^3)", new Integer(5)},

      // BigInteger-valued arithmetic expressions
      {"-1h", BigInteger.valueOf(-1)},
      {"+1H", BigInteger.valueOf(1)},
      {"--1h", BigInteger.valueOf(1)},
      {"2h*2", BigInteger.valueOf(4)},
      {"5/2h", BigInteger.valueOf(2)},
      {"5h+2", BigInteger.valueOf(7)},
      {"5-2h", BigInteger.valueOf(3)},
      {"5+2H*3", BigInteger.valueOf(11)},
      {"(5+2H)*3", BigInteger.valueOf(21)},
      {"~1h", BigInteger.valueOf(~1)},
      {"5h%2", BigInteger.valueOf(1)},
      {"5h<<2", BigInteger.valueOf(20)},
      {"5h>>2", BigInteger.valueOf(1)},
      {"5h>>1+1", BigInteger.valueOf(1)},
      {"-5h>>>2", BigInteger.valueOf(-2)},
      {"5.b & 3", BigInteger.valueOf(1)},
      {"5h ^3", BigInteger.valueOf(6)},
      {"5h&3|5^3", BigInteger.valueOf(7)},
      {"5H&(3|5^3)", BigInteger.valueOf(5)},

      // Logical expressions
      {"!1", Boolean.FALSE},
      {"!null", Boolean.TRUE},
      {"5<2", Boolean.FALSE},
      {"5>2", Boolean.TRUE},
      {"5<=5", Boolean.TRUE},
      {"5>=3", Boolean.TRUE},
      {"5<-5>>>2", Boolean.TRUE},
      {"5==5.0", Boolean.TRUE},
      {"5!=5.0", Boolean.FALSE},
      {"null in {true,false,null}", Boolean.TRUE},
      {"null not in {true,false,null}", Boolean.FALSE},
      {"null in {true,false,null}.toArray()", Boolean.TRUE},
      {"5 in {true,false,null}", Boolean.FALSE},
      {"5 not in {true,false,null}", Boolean.TRUE},
      {"5 instanceof java.lang.Integer", Boolean.TRUE},
      {"5. instanceof java.lang.Integer", Boolean.FALSE},

      // Logical expressions (string versions)
      {"2 or 0", new Integer(2)},
      {"1 and 0", new Integer(0)},
      {"1 bor 0", new Integer(1)},
      {"1 xor 0", new Integer(1)},
      {"1 band 0", new Integer(0)},
      {"1 eq 1", Boolean.TRUE},
      {"1 neq 1", Boolean.FALSE},
      {"1 lt 5", Boolean.TRUE},
      {"1 lte 5", Boolean.TRUE},
      {"1 gt 5", Boolean.FALSE},
      {"1 gte 5", Boolean.FALSE},
      {"1 lt 5", Boolean.TRUE},
      {"1 shl 2", new Integer(4)},
      {"4 shr 2", new Integer(1)},
      {"4 ushr 2", new Integer(1)},
      {"not null", Boolean.TRUE},
      {"not 1", Boolean.FALSE},

      {"#x > 0", Boolean.TRUE},
      {"#x < 0", Boolean.FALSE},
      {"#x == 0", Boolean.FALSE},
      {"#x == 1", Boolean.TRUE},
      {"0 > #x", Boolean.FALSE},
      {"0 < #x", Boolean.TRUE},
      {"0 == #x", Boolean.FALSE},
      {"1 == #x", Boolean.TRUE},
      {"\"1\" > 0", Boolean.TRUE},
      {"\"1\" < 0", Boolean.FALSE},
      {"\"1\" == 0", Boolean.FALSE},
      {"\"1\" == 1", Boolean.TRUE},
      {"0 > \"1\"", Boolean.FALSE},
      {"0 < \"1\"", Boolean.TRUE},
      {"0 == \"1\"", Boolean.FALSE},
      {"1 == \"1\"", Boolean.TRUE},
      {"#x + 1", "11"},
      {"1 + #x", "11"},
      {"#y == 1", Boolean.TRUE},
      {"#y == \"1\"", Boolean.TRUE},
      {"#y + \"1\"", "11"},
      {"\"1\" + #y", "11"}
  };

  /*===================================================================
    Public static methods
  ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      result.addTest(new ArithmeticAndLogicalOperatorsTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", null, (String) TESTS[i][0], TESTS[i][1]));
    }
    return result;
  }

  /*===================================================================
    Constructors
  ===================================================================*/
  public ArithmeticAndLogicalOperatorsTest() {
    super();
  }

  public ArithmeticAndLogicalOperatorsTest(String name) {
    super(name);
  }

  public ArithmeticAndLogicalOperatorsTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
  }

  public ArithmeticAndLogicalOperatorsTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    super(name, root, expressionString, expectedResult, setValue);
  }

  public ArithmeticAndLogicalOperatorsTest(String name, Object root, String expressionString, Object expectedResult) {
    super(name, root, expressionString, expectedResult);
  }

  /*===================================================================
    Overridden methods
  ===================================================================*/
  protected void setUp() {
    super.setUp();
    context.put("x", "1");
    context.put("y", new BigDecimal(1));
  }
}
