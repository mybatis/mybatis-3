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
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UnknownTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new UnknownTypeHandler(new TypeHandlerRegistry());

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setString(with(any(int.class)), with(any(String.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getMetaData();
        will(returnValue(rsmd));
        one(rsmd).getColumnCount();
        will(returnValue(1));
        one(rsmd).getColumnName(with(any(int.class)));
        will(returnValue("column"));
        one(rsmd).getColumnClassName(with(any(int.class)));
        will(returnValue(String.class));
        one(rsmd).getColumnType(with(any(int.class)));
        will(returnValue(JdbcType.VARCHAR.TYPE_CODE));
        one(rs).getString(with(any(String.class)));
        will(returnValue("Hello"));
        allowing(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getObject(with(any(int.class)));
        will(returnValue("Hello"));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}