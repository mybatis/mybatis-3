/*
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import org.junit.jupiter.api.Test;

class ExceptionUtilTest {

  @Test
  void shouldUnwrapThrowable() {
    Exception exception = new Exception();
    assertEquals(exception, ExceptionUtil.unwrapThrowable(exception));
    assertEquals(exception, ExceptionUtil.unwrapThrowable(new InvocationTargetException(exception, "test")));
    assertEquals(exception, ExceptionUtil.unwrapThrowable(new UndeclaredThrowableException(exception, "test")));
    assertEquals(exception, ExceptionUtil.unwrapThrowable(new InvocationTargetException(new InvocationTargetException(exception, "test"), "test")));
    assertEquals(exception, ExceptionUtil.unwrapThrowable(new InvocationTargetException(new UndeclaredThrowableException(exception, "test"), "test")));
  }

}
