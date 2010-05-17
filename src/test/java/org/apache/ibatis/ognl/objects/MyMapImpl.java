//--------------------------------------------------------------------------
//	Copyright (c) 2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
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
package org.apache.ibatis.ognl.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This tests the interface inheritence test.  This test implements
 * MyMap->Map but extends Object, therefore should be coded using
 * MapPropertyAccessor instead of ObjectPropertyAccessor.
 */
public class MyMapImpl extends Object implements MyMap {
  private Map map = new HashMap();

  public void clear() {
    map.clear();
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public Set entrySet() {
    return map.entrySet();
  }

  public boolean equals(Object o) {
    return map.equals(o);
  }

  public Object get(Object key) {
    return map.get(key);
  }

  public int hashCode() {
    return map.hashCode();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set keySet() {
    return map.keySet();
  }

  public Object put(Object key, Object value) {
    return map.put(key, value);
  }

  public void putAll(Map t) {
    map.putAll(t);
  }

  public Object remove(Object key) {
    return map.remove(key);
  }

  public int size() {
    return map.size();
  }

  public Collection values() {
    return map.values();
  }

  public String getDescription() {
    return "MyMap implementation";
  }
}
