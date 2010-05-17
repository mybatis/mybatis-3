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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class LambdaExpressionTest extends OgnlTestCase {
  private static Object[][] TESTS = {
      // Lambda expressions
      {null, "#a=:[33](20).longValue().{0}.toArray().length", new Integer(33)},
      {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30)", new Integer(1409286144)},
      {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30L)", new Long(-8764578968847253504L)},
      {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30h)", new BigInteger("265252859812191058636308480000000")},
      {null, "#bump = :[ #this.{ #this + 1 } ], (#bump)({ 1, 2, 3 })", new ArrayList(Arrays.asList(new Integer[]{new Integer(2), new Integer(3), new Integer(4)}))},
      {null, "#call = :[ \"calling \" + [0] + \" on \" + [1] ], (#call)({ \"x\", \"y\" })", "calling x on y"},
  };

  /*===================================================================
    Public static methods
  ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      result.addTest(new LambdaExpressionTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
    }
    return result;
  }

  /*===================================================================
    Constructors
  ===================================================================*/
  public LambdaExpressionTest() {
    super();
  }

  public LambdaExpressionTest(String name) {
    super(name);
  }

  public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
  }

  public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    super(name, root, expressionString, expectedResult, setValue);
  }

  public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult) {
    super(name, root, expressionString, expectedResult);
  }
}
