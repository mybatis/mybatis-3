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

public class IndexAccessTest extends OgnlTestCase {
  private static Root ROOT = new Root();

  private static Object[][] TESTS = {
      // indexed access of with navigation chain (should start back at root)
      {ROOT, "list[index]", ROOT.getList().get(ROOT.getIndex())},
      {ROOT, "list[size() - 1]", MethodFailedException.class},
  };

  /*===================================================================
     Public static methods
     ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      result.addTest(new IndexAccessTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
    }
    return result;
  }

  /*===================================================================
     Constructors
     ===================================================================*/
  public IndexAccessTest() {
    super();
  }

  public IndexAccessTest(String name) {
    super(name);
  }

  public IndexAccessTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
    super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
  }

  public IndexAccessTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
    super(name, root, expressionString, expectedResult, setValue);
  }

  public IndexAccessTest(String name, Object root, String expressionString, Object expectedResult) {
    super(name, root, expressionString, expectedResult);
  }
}
