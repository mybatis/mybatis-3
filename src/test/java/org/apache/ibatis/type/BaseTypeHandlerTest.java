/*
 *    Copyright 2009-2021 the original author or authors.
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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
abstract class BaseTypeHandlerTest {

  @Mock
  protected ResultSet rs;
  @Mock
  protected PreparedStatement ps;
  @Mock
  protected CallableStatement cs;
  @Mock
  protected ResultSetMetaData rsmd;

  public abstract void shouldSetParameter() throws Exception;

  public abstract void shouldGetResultFromResultSetByName() throws Exception;

  public abstract void shouldGetResultNullFromResultSetByName() throws Exception;

  public abstract void shouldGetResultFromResultSetByPosition() throws Exception;

  public abstract void shouldGetResultNullFromResultSetByPosition() throws Exception;

  public abstract void shouldGetResultFromCallableStatement() throws Exception;

  public abstract void shouldGetResultNullFromCallableStatement() throws Exception;
}
