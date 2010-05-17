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

public class NumericConversionTest extends OgnlTestCase {
  private static Object[][] TESTS = {
      /* To Integer.class */
      {"55", Integer.class, new Integer(55)},
      {new Integer(55), Integer.class, new Integer(55)},
      {new Double(55), Integer.class, new Integer(55)},
      {Boolean.TRUE, Integer.class, new Integer(1)},
      {new Byte((byte) 55), Integer.class, new Integer(55)},
      {new Character((char) 55), Integer.class, new Integer(55)},
      {new Short((short) 55), Integer.class, new Integer(55)},
      {new Long(55), Integer.class, new Integer(55)},
      {new Float(55), Integer.class, new Integer(55)},
      {new BigInteger("55"), Integer.class, new Integer(55)},
      {new BigDecimal("55"), Integer.class, new Integer(55)},

      /* To Double.class */
      {"55.1234", Double.class, new Double(55.1234)},
      {new Integer(55), Double.class, new Double(55)},
      {new Double(55.1234), Double.class, new Double(55.1234)},
      {Boolean.TRUE, Double.class, new Double(1)},
      {new Byte((byte) 55), Double.class, new Double(55)},
      {new Character((char) 55), Double.class, new Double(55)},
      {new Short((short) 55), Double.class, new Double(55)},
      {new Long(55), Double.class, new Double(55)},
      {new Float(55.1234), Double.class, new Double(55.1234), new Integer(4)},
      {new BigInteger("55"), Double.class, new Double(55)},
      {new BigDecimal("55.1234"), Double.class, new Double(55.1234)},

      /* To Boolean.class */
      {"true", Boolean.class, Boolean.TRUE},
      {new Integer(55), Boolean.class, Boolean.TRUE},
      {new Double(55), Boolean.class, Boolean.TRUE},
      {Boolean.TRUE, Boolean.class, Boolean.TRUE},
      {new Byte((byte) 55), Boolean.class, Boolean.TRUE},
      {new Character((char) 55), Boolean.class, Boolean.TRUE},
      {new Short((short) 55), Boolean.class, Boolean.TRUE},
      {new Long(55), Boolean.class, Boolean.TRUE},
      {new Float(55), Boolean.class, Boolean.TRUE},
      {new BigInteger("55"), Boolean.class, Boolean.TRUE},
      {new BigDecimal("55"), Boolean.class, Boolean.TRUE},

      /* To Byte.class */
      {"55", Byte.class, new Byte((byte) 55)},
      {new Integer(55), Byte.class, new Byte((byte) 55)},
      {new Double(55), Byte.class, new Byte((byte) 55)},
      {Boolean.TRUE, Byte.class, new Byte((byte) 1)},
      {new Byte((byte) 55), Byte.class, new Byte((byte) 55)},
      {new Character((char) 55), Byte.class, new Byte((byte) 55)},
      {new Short((short) 55), Byte.class, new Byte((byte) 55)},
      {new Long(55), Byte.class, new Byte((byte) 55)},
      {new Float(55), Byte.class, new Byte((byte) 55)},
      {new BigInteger("55"), Byte.class, new Byte((byte) 55)},
      {new BigDecimal("55"), Byte.class, new Byte((byte) 55)},

      /* To Character.class */
      {"55", Character.class, new Character((char) 55)},
      {new Integer(55), Character.class, new Character((char) 55)},
      {new Double(55), Character.class, new Character((char) 55)},
      {Boolean.TRUE, Character.class, new Character((char) 1)},
      {new Byte((byte) 55), Character.class, new Character((char) 55)},
      {new Character((char) 55), Character.class, new Character((char) 55)},
      {new Short((short) 55), Character.class, new Character((char) 55)},
      {new Long(55), Character.class, new Character((char) 55)},
      {new Float(55), Character.class, new Character((char) 55)},
      {new BigInteger("55"), Character.class, new Character((char) 55)},
      {new BigDecimal("55"), Character.class, new Character((char) 55)},

      /* To Short.class */
      {"55", Short.class, new Short((short) 55)},
      {new Integer(55), Short.class, new Short((short) 55)},
      {new Double(55), Short.class, new Short((short) 55)},
      {Boolean.TRUE, Short.class, new Short((short) 1)},
      {new Byte((byte) 55), Short.class, new Short((short) 55)},
      {new Character((char) 55), Short.class, new Short((short) 55)},
      {new Short((short) 55), Short.class, new Short((short) 55)},
      {new Long(55), Short.class, new Short((short) 55)},
      {new Float(55), Short.class, new Short((short) 55)},
      {new BigInteger("55"), Short.class, new Short((short) 55)},
      {new BigDecimal("55"), Short.class, new Short((short) 55)},

      /* To Long.class */
      {"55", Long.class, new Long(55)},
      {new Integer(55), Long.class, new Long(55)},
      {new Double(55), Long.class, new Long(55)},
      {Boolean.TRUE, Long.class, new Long(1)},
      {new Byte((byte) 55), Long.class, new Long(55)},
      {new Character((char) 55), Long.class, new Long(55)},
      {new Short((short) 55), Long.class, new Long(55)},
      {new Long(55), Long.class, new Long(55)},
      {new Float(55), Long.class, new Long(55)},
      {new BigInteger("55"), Long.class, new Long(55)},
      {new BigDecimal("55"), Long.class, new Long(55)},

      /* To Float.class */
      {"55.1234", Float.class, new Float(55.1234)},
      {new Integer(55), Float.class, new Float(55)},
      {new Double(55.1234), Float.class, new Float(55.1234)},
      {Boolean.TRUE, Float.class, new Float(1)},
      {new Byte((byte) 55), Float.class, new Float(55)},
      {new Character((char) 55), Float.class, new Float(55)},
      {new Short((short) 55), Float.class, new Float(55)},
      {new Long(55), Float.class, new Float(55)},
      {new Float(55.1234), Float.class, new Float(55.1234)},
      {new BigInteger("55"), Float.class, new Float(55)},
      {new BigDecimal("55.1234"), Float.class, new Float(55.1234)},

      /* To BigInteger.class */
      {"55", BigInteger.class, new BigInteger("55")},
      {new Integer(55), BigInteger.class, new BigInteger("55")},
      {new Double(55), BigInteger.class, new BigInteger("55")},
      {Boolean.TRUE, BigInteger.class, new BigInteger("1")},
      {new Byte((byte) 55), BigInteger.class, new BigInteger("55")},
      {new Character((char) 55), BigInteger.class, new BigInteger("55")},
      {new Short((short) 55), BigInteger.class, new BigInteger("55")},
      {new Long(55), BigInteger.class, new BigInteger("55")},
      {new Float(55), BigInteger.class, new BigInteger("55")},
      {new BigInteger("55"), BigInteger.class, new BigInteger("55")},
      {new BigDecimal("55"), BigInteger.class, new BigInteger("55")},

      /* To BigDecimal.class */
      {"55.1234", BigDecimal.class, new BigDecimal("55.1234")},
      {new Integer(55), BigDecimal.class, new BigDecimal("55")},
      {new Double(55.1234), BigDecimal.class, new BigDecimal("55.1234"), new Integer(4)},
      {Boolean.TRUE, BigDecimal.class, new BigDecimal("1")},
      {new Byte((byte) 55), BigDecimal.class, new BigDecimal("55")},
      {new Character((char) 55), BigDecimal.class, new BigDecimal("55")},
      {new Short((short) 55), BigDecimal.class, new BigDecimal("55")},
      {new Long(55), BigDecimal.class, new BigDecimal("55")},
      {new Float(55.1234), BigDecimal.class, new BigDecimal("55.1234"), new Integer(4)},
      {new BigInteger("55"), BigDecimal.class, new BigDecimal("55")},
      {new BigDecimal("55.1234"), BigDecimal.class, new BigDecimal("55.1234")},
  };

  private Object value;
  private Class toClass;
  private Object expectedValue;
  private int scale;

  /*===================================================================
    Public static methods
  ===================================================================*/
  public static TestSuite suite() {
    TestSuite result = new TestSuite();

    for (int i = 0; i < TESTS.length; i++) {
      result.addTest(new NumericConversionTest(TESTS[i][0],
          (Class) TESTS[i][1],
          TESTS[i][2],
          (TESTS[i].length > 3) ? ((Integer) TESTS[i][3]).intValue() : -1));
    }
    return result;
  }

  /*===================================================================
    Constructors
  ===================================================================*/
  public NumericConversionTest(Object value, Class toClass, Object expectedValue, int scale) {
    super(value + " [" + value.getClass().getName() + "] -> " + toClass.getName() + " == " + expectedValue + " [" + expectedValue.getClass().getName() + "]" + ((scale >= 0) ? (" (to within " + scale + " decimal places)") : ""));
    this.value = value;
    this.toClass = toClass;
    this.expectedValue = expectedValue;
    this.scale = scale;
  }

  /*===================================================================
    Overridden methods
  ===================================================================*/
  protected void runTest() throws OgnlException {
    Object result;

    result = OgnlOps.convertValue(value, toClass);
    if (!isEqual(result, expectedValue)) {
      if (scale >= 0) {
        double scalingFactor = Math.pow(10, scale),
            v1 = ((Number) value).doubleValue() * scalingFactor,
            v2 = ((Number) expectedValue).doubleValue() * scalingFactor;

        assertTrue((int) v1 == (int) v2);
      } else {
        fail();
      }
    }
  }
}
