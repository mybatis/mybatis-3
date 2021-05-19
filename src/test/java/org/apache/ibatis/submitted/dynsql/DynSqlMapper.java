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
package org.apache.ibatis.submitted.dynsql;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface DynSqlMapper {
  String selectDescription(@Param("p") String p);

  List<String> selectDescriptionById(Integer id);
  List<String> selectDescriptionByConditions(Conditions conditions);
  List<String> selectDescriptionByConditions2(Conditions conditions);
  List<String> selectDescriptionByConditions3(Conditions conditions);

  class Conditions {
    private Integer id;

    public void setId(Integer id) {
      this.id = id;
    }

    public Integer getId() {
      return id;
    }
  }

}
