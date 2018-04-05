/**
 *    Copyright 2009-2018 the original author or authors.
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
import java.sql.ResultSet;

/**
 * The base {@link TypeHandler} for using nullable JDBC APIs (APIs that it return <code>null</code>,
 * if the value is SQL <code>NULL</code>).
 * <p>
 * Nullable JDBC APIs are follows on {@link ResultSet} or {@link CallableStatement}:
 * </p>
 * <ul>
 * <li><code>getAsciiStream()</code></li>
 * <li><code>getBigDecimal()</code></li>
 * <li><code>getBinaryStream()</code></li>
 * <li><code>getBytes()</code></li>
 * <li><code>getCharacterStream()</code></li>
 * <li><code>getDate()</code></li>
 * <li><code>getNCharacterStream()</code></li>
 * <li><code>getNString()</code></li>
 * <li><code>getRowId()</code></li>
 * <li><code>getString()</code></li>
 * <li><code>getTime()</code></li>
 * <li><code>getTimestamp()</code></li>
 * <li><code>getUnicodeStream()</code></li>
 * <li><code>getURL()</code></li>
 * </ul>
 * <p>
 * If you create a custom {@link TypeHandler} using above APIs, we propose to use this class instead
 * of the {@link BaseTypeHandler}. This class is efficient than the {@link BaseTypeHandler} because
 * it does not call the <code>wasNull()</code> on {@link ResultSet} or {@link CallableStatement} for
 * handling null value.
 * </p>
 *
 * @param <T> the referenced type
 * @author Kazuki Shimizu
 * @since 3.5.0
 */
public abstract class NullableApiBasedTypeHandler<T> extends BaseTypeHandler<T> {

  /**
   * {@inheritDoc}
   * 
   * @return Always return a fetched result
   */
  @Override
  protected T handleResult(ResultSet rs, T result) {
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @return Always return a fetched result
   */
  @Override
  protected T handleResult(CallableStatement cs, T result) {
    return result;
  }

}
