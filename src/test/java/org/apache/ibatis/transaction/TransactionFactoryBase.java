/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.transaction;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see TransactionFactory
 */
@ExtendWith(MockitoExtension.class)
public abstract class TransactionFactoryBase {

  public abstract void shouldSetProperties() throws Exception;

  public abstract void shouldNewTransactionWithConnection() throws SQLException;

  public abstract void shouldNewTransactionWithDataSource() throws Exception;

  public static Object getValue(Field field, Object object) throws Exception {
    field.setAccessible(true);
    return field.get(object);
  }

}
