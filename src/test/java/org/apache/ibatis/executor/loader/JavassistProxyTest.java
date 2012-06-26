/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.executor.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.junit.Test;

import domain.blog.Author;

public class JavassistProxyTest extends SerializableProxyTest {

  public JavassistProxyTest() {
    proxyFactory = new JavassistProxyFactory();
  }


  @Test(expected = ExecutorException.class)
  public void shouldFailCallingAnUnloadedProperty() throws Exception {
    // yes, it must go in uppercase
    HashSet<String> unloadedProperties = new HashSet<String>();
    unloadedProperties.add("ID");
    Author author2 = (Author) ((JavassistProxyFactory)proxyFactory).createDeserializationProxy(author, unloadedProperties, new DefaultObjectFactory(), new ArrayList<Class<?>>(), new ArrayList<Object>());
    author2.getId();
  }

  @Test
  public void shouldLetCallALoadedProperty() throws Exception {
    Author author2 = (Author) ((JavassistProxyFactory)proxyFactory).createDeserializationProxy(author, new HashSet<String>(), new DefaultObjectFactory(), new ArrayList<Class<?>>(), new ArrayList<Object>());
    assertEquals(999, author2.getId());
  }

  @Test
  public void shouldSerizalizeADeserlizaliedProxy() throws Exception {
    Object proxy = ((JavassistProxyFactory)proxyFactory).createDeserializationProxy(author, new HashSet<String>(), new DefaultObjectFactory(), new ArrayList<Class<?>>(), new ArrayList<Object>());
    Author author2 = (Author) deserialize(serialize((Serializable) proxy));
    assertEquals(author, author2);
    assertFalse(author.getClass().equals(author2.getClass()));
  }

}
