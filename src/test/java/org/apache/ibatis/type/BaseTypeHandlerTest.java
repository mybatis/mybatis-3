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

import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.sql.*;

public abstract class BaseTypeHandlerTest {

  protected Mockery mockery = new Mockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  protected final ResultSet rs = mockery.mock(ResultSet.class);
  protected final PreparedStatement ps = mockery.mock(PreparedStatement.class);
  protected final CallableStatement cs = mockery.mock(CallableStatement.class);
  protected final ResultSetMetaData rsmd = mockery.mock(ResultSetMetaData.class);

  public abstract void shouldSetParameter()
      throws Exception;

  public abstract void shouldGetResultFromResultSet()
      throws Exception;

  public abstract void shouldGetResultFromCallableStatement()
      throws Exception;

}
