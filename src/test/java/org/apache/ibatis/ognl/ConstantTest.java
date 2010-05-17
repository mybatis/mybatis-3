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

import java.util.Arrays;

public class ConstantTest extends OgnlTestCase {
  private static Object[][] TESTS = {
      {"12345", new Integer(12345)},
      {"0x100", new Integer(256)},
      {"0xfE", new Integer(254)},
      {"01000", new Integer(512)},
      {"1234L", new Long(1234)},
      {"12.34", new Double(12.34)},
      {".1234", new Double(.12340000000000)},
      {"12.34f", new Float(12.34f)},
      {"12.", new Double(12)},
      {"12e+1d", new Double(120)},
      {"'x'", new Character('x')},
      {"'\\n'", new Character('\n')},
      {"'\\u048c'", new Character('\u048c')},
      {"'\\47'", new Character('\47')},
      {"'\\367'", new Character('\367')},
      {"'\\367", ExpressionSyntaxException.class},
      {"'\\x'", ExpressionSyntaxException.class},
      {"\"hello world\"", "hello world"},
      {"\"\\u00a0\\u0068ell\\'o\\\\\\n\\r\\f\\t\\b\\\"\\167orld\\\"\"", "\u00a0hell'o\\\n\r\f\t\b\"world\""},
      {"\"hello world", ExpressionSyntaxException.class},
      {"\"hello\\x world\"", ExpressionSyntaxException.class},
      {"null", null},
      {"true", Boolean.TRUE},
      {"false", Boolean.FALSE},
      {"{ false, true, null, 0, 1. }", Arrays.asList(new Object[]{Boolean.FALSE, Boolean.TRUE, null, new Integer(0), new Double(1)})},
      {"'HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"'", "HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\""},
  };

  /*===================================================================
     Public static methods
     ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      result.addTest(new ConstantTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", null, (String) TESTS[i][0], TESTS[i][1]));
    }
    return result;
  }

  /*===================================================================
     Constructors
     ===================================================================*/
  public ConstantTest() {
    super();
  }

  public ConstantTest(String name) {
    super(name);
  }

  public ConstantTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
  }

  public ConstantTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    super(name, root, expressionString, expectedResult, setValue);
  }

  public ConstantTest(String name, Object root, String expressionString, Object expectedResult) {
    super(name, root, expressionString, expectedResult);
  }
}
