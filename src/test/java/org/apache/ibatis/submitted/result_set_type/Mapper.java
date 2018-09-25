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
package org.apache.ibatis.submitted.result_set_type;

import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface Mapper {

  List<User> getUserWithStatementAndUnset(RowBounds rowBounds);
  List<User> getUserWithStatementAndDefault(RowBounds rowBounds);
  List<User> getUserWithStatementAndForwardOnly(RowBounds rowBounds);
  List<User> getUserWithStatementAndScrollInsensitive(RowBounds rowBounds);
  List<User> getUserWithStatementAndScrollSensitive(RowBounds rowBounds);

  List<User> getUserWithPreparedAndUnset(RowBounds rowBounds);
  List<User> getUserWithPreparedAndDefault(RowBounds rowBounds);
  List<User> getUserWithPreparedAndForwardOnly(RowBounds rowBounds);
  List<User> getUserWithPreparedAndScrollInsensitive(RowBounds rowBounds);
  List<User> getUserWithPreparedAndScrollSensitive(RowBounds rowBounds);

  List<User> getUserWithCallableAndUnset(RowBounds rowBounds);
  List<User> getUserWithCallableAndDefault(RowBounds rowBounds);
  List<User> getUserWithCallableAndForwardOnly(RowBounds rowBounds);
  List<User> getUserWithCallableAndScrollInsensitive(RowBounds rowBounds);
  List<User> getUserWithCallableAndScrollSensitive(RowBounds rowBounds);

}
