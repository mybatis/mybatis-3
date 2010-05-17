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

import java.util.Map;

/**
 * This interface defines methods for setting and getting a property from a target object.
 * A "property" in this case is a named data value that takes the generic form of an
 * Object---the same definition as is used by beans.  But the operational semantics of the
 * term will vary by implementation of this interface: a bean-style implementation will
 * get and set properties as beans do, by reflection on the target object's class, but
 * other implementations are possible, such as one that uses the property name as a key
 * into a map.
 * <p/>
 * <p> An implementation of this interface will often require that its target objects all
 * be of some particular type.  For example, the MapPropertyAccessor class requires that
 * its targets all implement the java.util.Map interface.
 * <p/>
 * <p> Note that the "name" of a property is represented by a generic Object.  Some
 * implementations may require properties' names to be Strings, while others may allow
 * them to be other types---for example, ArrayPropertyAccessor treats Number names as
 * indexes into the target object, which must be an array.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public interface PropertyAccessor {
  /**
   * Extracts and returns the property of the given name from the given target object.
   *
   * @param target the object to get the property from
   * @param name   the name of the property to get
   * @return the current value of the given property in the given object
   * @throws OgnlException if there is an error locating the property in the given object
   */
  Object getProperty(Map context, Object target, Object name) throws OgnlException;

  /**
   * Sets the value of the property of the given name in the given target object.
   *
   * @param target the object to set the property in
   * @param name   the name of the property to set
   * @param value  the new value for the property
   * @throws OgnlException if there is an error setting the property in the given object
   */
  void setProperty(Map context, Object target, Object name, Object value) throws OgnlException;
}
