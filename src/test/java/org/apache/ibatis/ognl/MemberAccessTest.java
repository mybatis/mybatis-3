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
import org.apache.ibatis.ognl.objects.Simple;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;

public class MemberAccessTest extends OgnlTestCase {
  private static Simple ROOT = new Simple();
  private static Object[][] TESTS = {
      {"@Runtime@getRuntime()", OgnlException.class},
      {"@System@getProperty('java.specification.version')", System.getProperty("java.specification.version")},
      {"bigIntValue", OgnlException.class},
      {"bigIntValue", OgnlException.class, new Integer(25), OgnlException.class},
      {"getBigIntValue()", OgnlException.class},
      {"stringValue", ROOT.getStringValue()},
  };

  /*===================================================================
     Public static methods
     ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      result.addTest(new MemberAccessTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", ROOT, (String) TESTS[i][0], TESTS[i][1]));
    }
    return result;
  }

  /*===================================================================
     Constructors
     ===================================================================*/
  public MemberAccessTest() {
    super();
  }

  public MemberAccessTest(String name) {
    super(name);
  }

  public MemberAccessTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
  }

  public MemberAccessTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    super(name, root, expressionString, expectedResult, setValue);
  }

  public MemberAccessTest(String name, Object root, String expressionString, Object expectedResult) {
    super(name, root, expressionString, expectedResult);
  }

  /*===================================================================
     Overridden methods
     ===================================================================*/
  public void setUp() {
    super.setUp();
    /* Should allow access at all to the Simple class except for the bigIntValue property */
    context.setMemberAccess(new DefaultMemberAccess(false) {
      public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
        if (target == Runtime.class) {
          return false;
        }
        if (target instanceof Simple) {
          if (propertyName != null) {
            return !propertyName.equals("bigIntValue") &&
                super.isAccessible(context, target, member, propertyName);
          } else {
            if (member instanceof Method) {
              return !member.getName().equals("getBigIntValue") &&
                  !member.getName().equals("setBigIntValue") &&
                  super.isAccessible(context, target, member, propertyName);
            }
          }
        }
        return super.isAccessible(context, target, member, propertyName);
      }
    });
  }
}
