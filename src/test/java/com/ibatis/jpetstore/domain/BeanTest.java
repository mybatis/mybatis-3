/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.ibatis.jpetstore.domain;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class BeanTest extends TestCase {

  private Class[] classes = {
      Account.class,
      Cart.class,
      CartItem.class,
      Category.class,
      Item.class,
      LineItem.class,
      Order.class,
      Product.class,
      Sequence.class
  };

  public void testAllReadWriteProperties() {
    try {
      for (int i = 0; i < classes.length; i++) {
        Object object = classes[i].newInstance();
        ClassIntrospector introspector = ClassIntrospector.getInstance(classes[i]);
        List writeables = Arrays.asList(introspector.getWriteablePropertyNames());
        List readables = Arrays.asList(introspector.getReadablePropertyNames());
        for (int j = 0; j < writeables.size(); j++) {
          String writeable = (String) writeables.get(j);
          if (readables.contains(writeable)) {
            Class type = introspector.getGetterType(writeable);
            Object sample = getSampleFor(type);
            BeanIntrospector probe = new BeanIntrospector();
            probe.setObject(object, writeable, sample);
            assertEquals(sample, probe.getObject(object, writeable));
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error. ", e);
    }
  }

  public Object getSampleFor(Class type) throws Exception {
    Map sampleMap = new HashMap();
    sampleMap.put(String.class, "Hello");
    sampleMap.put(Integer.class, new Integer(1));
    sampleMap.put(int.class, new Integer(1));
    sampleMap.put(Long.class, new Long(1));
    sampleMap.put(long.class, new Long(1));
    sampleMap.put(Double.class, new Double(1));
    sampleMap.put(double.class, new Double(1));
    sampleMap.put(Float.class, new Float(1));
    sampleMap.put(float.class, new Float(1));
    sampleMap.put(Short.class, new Short((short) 1));
    sampleMap.put(short.class, new Short((short) 1));
    sampleMap.put(Character.class, new Integer(1));
    sampleMap.put(char.class, new Integer(1));
    sampleMap.put(Date.class, new Date());
    sampleMap.put(boolean.class, new Boolean(true));
    sampleMap.put(Boolean.class, new Boolean(true));
    sampleMap.put(BigDecimal.class, new BigDecimal("1.00"));
    sampleMap.put(BigInteger.class, new BigInteger("1"));
    sampleMap.put(List.class, new ArrayList());
    sampleMap.put(List.class, new ArrayList());
    if (!sampleMap.containsKey(type)) {
      try {
        sampleMap.put(type, type.newInstance());
      } catch (Exception e) {
        // ignore on purpose...we don't care if this fails
      }
    }
    return sampleMap.get(type);
  }

}

