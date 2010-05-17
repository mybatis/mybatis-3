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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;

public class OgnlTestCase extends TestCase {
  protected OgnlContext context;
  private String expressionString;
  private SimpleNode expression;
  private Object expectedResult;
  private Object root;
  private boolean hasSetValue;
  private Object setValue;
  private boolean hasExpectedAfterSetResult;
  private Object expectedAfterSetResult;

  /*===================================================================
    Public static methods
  ===================================================================*/
  /**
   * Returns true if object1 is equal to object2 in either the
   * sense that they are the same object or, if both are non-null
   * if they are equal in the <CODE>equals()</CODE> sense.
   */
  public static boolean isEqual(Object object1, Object object2) {
    boolean result = false;

    if (object1 == object2) {
      result = true;
    } else {
      if ((object1 != null) && object1.getClass().isArray()) {
        if ((object2 != null) && object2.getClass().isArray() && (object2.getClass() == object1.getClass())) {
          result = (Array.getLength(object1) == Array.getLength(object2));
          if (result) {
            for (int i = 0, icount = Array.getLength(object1); result && (i < icount); i++) {
              result = isEqual(Array.get(object1, i), Array.get(object2, i));
            }
          }
        }
      } else {
        result = (object1 != null) && (object2 != null) && object1.equals(object2);
      }
    }
    return result;
  }

  /*===================================================================
    Constructors
  ===================================================================*/
  public OgnlTestCase() {
    super();
  }

  public OgnlTestCase(String name) {
    super(name);
  }

  public OgnlTestCase(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    this(name, root, expressionString, expectedResult, setValue);
    this.hasExpectedAfterSetResult = true;
    this.expectedAfterSetResult = expectedAfterSetResult;
  }

  public OgnlTestCase(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    this(name, root, expressionString, expectedResult);
    this.hasSetValue = true;
    this.setValue = setValue;
  }

  public OgnlTestCase(String name, Object root, String expressionString, Object expectedResult) {
    this(name);
    this.root = root;
    this.expressionString = expressionString;
    this.expectedResult = expectedResult;
  }

  /*===================================================================
    Public methods
  ===================================================================*/
  public String getExpressionDump(SimpleNode node) {
    StringWriter writer = new StringWriter();

    node.dump(new PrintWriter(writer), "   ");
    return writer.toString();
  }

  public String getExpressionString() {
    return expressionString;
  }

  public SimpleNode getExpression() throws OgnlException {
    if (expression == null) {
      expression = (SimpleNode) Ognl.parseExpression(expressionString);
    }
    return expression;
  }

  public Object getExpectedResult() {
    return expectedResult;
  }

  /*===================================================================
    Overridden methods
  ===================================================================*/
  protected void runTest() throws Exception {
    Object testedResult = null;

    try {
      SimpleNode expr;

      testedResult = expectedResult;
      expr = getExpression();
      /*
      PrintWriter writer = new PrintWriter(System.err);
      System.err.println(expr.toString());
      expr.dump(writer, "");
      writer.flush();
      */
      assertTrue(isEqual(Ognl.getValue(expr, context, root), expectedResult));
      if (hasSetValue) {
        testedResult = hasExpectedAfterSetResult ? expectedAfterSetResult : setValue;
        Ognl.setValue(expr, context, root, setValue);
        assertTrue(isEqual(Ognl.getValue(expr, context, root), testedResult));
      }
    } catch (Exception ex) {
      if (testedResult instanceof Class) {
        assertTrue(((Class) testedResult).isAssignableFrom(ex.getClass()));
      } else {
        throw ex;
      }
    }
  }

  private static Object[][] TESTS = {
      // Quoting
      {null, "`c`", new Character('c')},
      {null, "'s'", new Character('s')},
      {null, "'string'", "string"},
      {null, "\"string\"", "string"},
  };

  /*===================================================================
     Public static methods
     ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      if (TESTS[i].length == 3) {
        result.addTest(new QuotingTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
      } else {
        if (TESTS[i].length == 4) {
          result.addTest(new QuotingTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
        } else {
          if (TESTS[i].length == 5) {
            result.addTest(new QuotingTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
          } else {
            throw new RuntimeException("don't understand TEST format");
          }
        }
      }
    }
    return result;
  }

  protected void setUp() {
    context = (OgnlContext) Ognl.createDefaultContext(null);
  }
}
