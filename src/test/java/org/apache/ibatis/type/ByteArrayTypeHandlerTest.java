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
package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public class ByteArrayTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new ByteArrayTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setBytes(with(any(int.class)), with(any(byte[].class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, new byte[]{1, 2, 3}, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getBytes(with(any(String.class)));
        will(returnValue(new byte[]{1, 2, 3}));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getBytes(with(any(int.class)));
        will(returnValue(new byte[]{1, 2, 3}));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}