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

import java.util.HashMap;

public class SetterTest extends OgnlTestCase {
  private static Root ROOT = new Root();

  private static Object[][] TESTS = {
      // Setting values
      {ROOT.getMap(), "newValue", null, new Integer(101)},
      {ROOT, "settableList[0]", "foo", "quux"},     /* absolute indexes */
      {ROOT, "settableList[0]", "quux"},
      {ROOT, "settableList[2]", "baz", "quux"},
      {ROOT, "settableList[2]", "quux"},
      {ROOT, "settableList[$]", "quux", "oompa"},   /* special indexes */
      {ROOT, "settableList[$]", "oompa"},
      {ROOT, "settableList[^]", "quux", "oompa"},
      {ROOT, "settableList[^]", "oompa"},
      {ROOT, "settableList[|]", "bar", "oompa"},
      {ROOT, "settableList[|]", "oompa"},
      {ROOT, "map.newValue", new Integer(101), new Integer(555)},
      {ROOT, "map", ROOT.getMap(), new HashMap(), NoSuchPropertyException.class},
      {ROOT.getMap(), "newValue2 || put(\"newValue2\",987), newValue2", new Integer(987), new Integer(1002)},
      {ROOT, "map.(someMissingKey || newValue)", new Integer(555), new Integer(666)},
      {ROOT.getMap(), "newValue || someMissingKey", new Integer(666), new Integer(666)}, // no setting happens!
      {ROOT, "map.(newValue && aKey)", null, new Integer(54321)},
      {ROOT, "map.(someMissingKey && newValue)", null, null}, // again, no setting
      {null, "0", new Integer(0), null, InappropriateExpressionException.class}, // illegal for setting, no property
      {ROOT, "map[0]=\"map.newValue\", map[0](#this)", new Integer(666), new Integer(888)},
  };

  /*===================================================================
     Public static methods
     ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      if (TESTS[i].length == 3) {
        result.addTest(new SetterTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
      } else {
        if (TESTS[i].length == 4) {
          result.addTest(new SetterTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
        } else {
          if (TESTS[i].length == 5) {
            result.addTest(new SetterTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
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
  public SetterTest() {
    super();
  }

  public SetterTest(String name) {
    super(name);
  }

  public SetterTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
  }

  public SetterTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    super(name, root, expressionString, expectedResult, setValue);
  }

  public SetterTest(String name, Object root, String expressionString, Object expectedResult) {
    super(name, root, expressionString, expectedResult);
  }
}
