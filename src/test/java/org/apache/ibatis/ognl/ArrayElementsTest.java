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
import org.apache.ibatis.ognl.objects.Root;

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.Map;

public class ArrayElementsTest extends OgnlTestCase {
  private static String[] STRING_ARRAY = new String[]{"hello", "world"};
  private static int[] INT_ARRAY = new int[]{10, 20};
  private static Root ROOT = new Root();

  private static Object[][] TESTS = {
      // Array elements test
      {STRING_ARRAY, "length", new Integer(2)},
      {STRING_ARRAY, "#root[1]", "world"},
      {INT_ARRAY, "#root[1]", new Integer(20)},
      {INT_ARRAY, "#root[1]", new Integer(20), "50", new Integer(50)},
      {INT_ARRAY, "#root[1]", new Integer(50), new String[]{"50", "100"}, new Integer(50)},
      {ROOT, "intValue", new Integer(0), new String[]{"50", "100"}, new Integer(50)},
      {ROOT, "array", ROOT.getArray(), new String[]{"50", "100"}, new int[]{50, 100}},
  };

  /*===================================================================
     Private static methods
     ===================================================================*/
  /*===================================================================
     Public static methods
     ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      if (TESTS[i].length == 3) {
        result.addTest(new ArrayElementsTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
      } else {
        if (TESTS[i].length == 4) {
          result.addTest(new ArrayElementsTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
        } else {
          if (TESTS[i].length == 5) {
            result.addTest(new ArrayElementsTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
          } else {
            throw new RuntimeException("don't understand TEST format");
          }
        }
      }
    }
    return result;
  }

  /*===================================================================
     Constructors
     ===================================================================*/
  public ArrayElementsTest() {
    super();
  }

  public ArrayElementsTest(String name) {
    super(name);
  }

  public ArrayElementsTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
  }

  public ArrayElementsTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    super(name, root, expressionString, expectedResult, setValue);
  }

  public ArrayElementsTest(String name, Object root, String expressionString, Object expectedResult) {
    super(name, root, expressionString, expectedResult);
  }

  /*===================================================================
     Overridden methods
     ===================================================================*/
  protected void setUp() {
    TypeConverter arrayConverter;

    super.setUp();
    arrayConverter = new DefaultTypeConverter() {
      public Object convertValue(Map context, Object target, Member member, String propertyName, Object value, Class toType) {
        if (value.getClass().isArray()) {
          if (!toType.isArray()) {
            value = Array.get(value, 0);
          }
        }
        return super.convertValue(context, target, member, propertyName, value, toType);
      }
    };
    context.setTypeConverter(arrayConverter);
  }
}
