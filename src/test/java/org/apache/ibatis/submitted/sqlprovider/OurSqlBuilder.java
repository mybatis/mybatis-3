/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.sqlprovider;

import java.util.List;
import java.util.Map;

public class OurSqlBuilder {

  public String buildGetUsersQuery(Map<String, Object> parameter) {
    // MyBatis wraps a single List parameter in a Map with the key="list",
    // so need to pull it out
    @SuppressWarnings("unchecked")
    List<Integer> ids = (List<Integer>) parameter.get("list");
    StringBuilder sb = new StringBuilder();
    sb.append("select * from users where id in (");
    for (int i = 0; i < ids.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("#{list[");
      sb.append(i);
      sb.append("]}");
    }
    sb.append(") order by id");
    return sb.toString();
  }

  public String buildGetUserQuery(Integer parameter) {
    // parameter is not a single List or Array,
    // so it is passed as is from the mapper
    return "select * from users where id = #{value}";
  }
}
