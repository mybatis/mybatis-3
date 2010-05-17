//--------------------------------------------------------------------------
//	Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
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
package org.apache.ibatis.ognl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * <p>PropertyDescriptor subclass that describes an indexed set of read/write
 * methods to get a property. Unlike IndexedPropertyDescriptor this allows
 * the "key" to be an arbitrary object rather than just an int.  Consequently
 * it does not have a "readMethod" or "writeMethod" because it only expects
 * a pattern like:</p>
 * <pre>
 *    public void set<i>Property</i>(<i>KeyType</i>, <i>ValueType</i>);
 *    public <i>ValueType</i> get<i>Property</i>(<i>KeyType</i>);
 * </pre>
 * <p>and does not require the methods that access it as an array.  OGNL can
 * get away with this without losing functionality because if the object
 * does expose the properties they are most probably in a Map and that case
 * is handled by the normal OGNL property accessors.
 * </p>
 * <p>For example, if an object were to have methods that accessed and "attributes"
 * property it would be natural to index them by String rather than by integer
 * and expose the attributes as a map with a different property name:
 * <pre>
 *    public void setAttribute(String name, Object value);
 *    public Object getAttribute(String name);
 *    public Map getAttributes();
 * </pre>
 * <p>Note that the index get/set is called get/set <code>Attribute</code>
 * whereas the collection getter is called <code>Attributes</code>.  This
 * case is handled unambiguously by the OGNL property accessors because the
 * set/get<code>Attribute</code> methods are detected by this object and the
 * "attributes" case is handled by the <code>MapPropertyAccessor</code>.
 * Therefore OGNL expressions calling this code would be handled in the
 * following way:
 * </p>
 * <table>
 * <tr><th>OGNL Expression</th>
 * <th>Handling</th>
 * </tr>
 * <tr>
 * <td><code>attribute["name"]</code></td>
 * <td>Handled by an index getter, like <code>getAttribute(String)</code>.</td>
 * </tr>
 * <tr>
 * <td><code>attribute["name"] = value</code></td>
 * <td>Handled by an index setter, like <code>setAttribute(String, Object)</code>.</td>
 * </tr>
 * <tr>
 * <td><code>attributes["name"]</code></td>
 * <td>Handled by <code>MapPropertyAccessor</code> via a <code>Map.get()</code>.  This
 * will <b>not</b> go through the index get accessor.
 * </td>
 * </tr>
 * <tr>
 * <td><code>attributes["name"] = value</code></td>
 * <td>Handled by <code>MapPropertyAccessor</code> via a <code>Map.put()</code>.  This
 * will <b>not</b> go through the index set accessor.
 * </td>
 * </tr>
 * </table>
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ObjectIndexedPropertyDescriptor extends PropertyDescriptor {
  private Method indexedReadMethod;
  private Method indexedWriteMethod;
  private Class propertyType;

  public ObjectIndexedPropertyDescriptor(String propertyName, Class propertyType, Method indexedReadMethod, Method indexedWriteMethod) throws IntrospectionException {
    super(propertyName, null, null);
    this.propertyType = propertyType;
    this.indexedReadMethod = indexedReadMethod;
    this.indexedWriteMethod = indexedWriteMethod;
  }

  public Method getIndexedReadMethod() {
    return indexedReadMethod;
  }

  public Method getIndexedWriteMethod() {
    return indexedWriteMethod;
  }

  public Class getPropertyType() {
    return propertyType;
  }
}
