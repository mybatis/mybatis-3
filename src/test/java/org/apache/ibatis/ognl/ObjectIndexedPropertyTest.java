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
import org.apache.ibatis.ognl.objects.ObjectIndexed;

public class ObjectIndexedPropertyTest extends OgnlTestCase {
  private static ObjectIndexed OBJECT_INDEXED = new ObjectIndexed();

  private static Object[][] TESTS = {
      // Arbitrary indexed properties
      {OBJECT_INDEXED, "attributes[\"bar\"]", "baz"},                                 /* get non-indexed property through attributes Map */
      {OBJECT_INDEXED, "attribute[\"foo\"]", "bar"},                                  /* get indexed property */
      {OBJECT_INDEXED, "attribute[\"bar\"]", "baz", "newValue", "newValue"},          /* set indexed property */
      {OBJECT_INDEXED, "attribute[\"bar\"]", "newValue"},                             /* get indexed property back to confirm */
      {OBJECT_INDEXED, "attributes[\"bar\"]", "newValue"},                            /* get property back through Map to confirm */
      {OBJECT_INDEXED, "attribute[\"other\"].attribute[\"bar\"]", "baz"},             /* get indexed property from indexed, then through other */
      {OBJECT_INDEXED, "attribute[\"other\"].attributes[\"bar\"]", "baz"},            /* get property back through Map to confirm */
      {OBJECT_INDEXED, "attribute[$]", OgnlException.class},                          /* illegal DynamicSubscript access to object indexed property */
  };

  /*===================================================================
     Public static methods
     ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      if (TESTS[i].length == 3) {
        result.addTest(new ObjectIndexedPropertyTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
      } else {
        if (TESTS[i].length == 4) {
          result.addTest(new ObjectIndexedPropertyTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
        } else {
          if (TESTS[i].length == 5) {
            result.addTest(new ObjectIndexedPropertyTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
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
  public ObjectIndexedPropertyTest() {
    super();
  }

  public ObjectIndexedPropertyTest(String name) {
    super(name);
  }

  public ObjectIndexedPropertyTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
  }

  public ObjectIndexedPropertyTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    super(name, root, expressionString, expectedResult, setValue);
  }

  public ObjectIndexedPropertyTest(String name, Object root, String expressionString, Object expectedResult) {
    super(name, root, expressionString, expectedResult);
  }
}
