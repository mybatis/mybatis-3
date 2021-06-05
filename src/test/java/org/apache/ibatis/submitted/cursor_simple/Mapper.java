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
package org.apache.ibatis.submitted.cursor_simple;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.RowBounds;

public interface Mapper {

  Cursor<User> getAllUsers();

  @Select({
    "select null id, null name from (values (0))",
    "union all",
    "select 99 id, 'Kate' name from (values (0))",
    "union all",
    "select null id, null name from (values (0))",
    "union all",
    "select null id, null name from (values (0))"
  })
  Cursor<User> getNullUsers(RowBounds rowBounds);

  @Select("select * from users")
  @Options(fetchSize = Integer.MIN_VALUE)
  Cursor<User> getUsersMysqlStream();
}
