/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.submitted.collection_injection.immutable;

public class ImmutableHouseSqlProvider {
  public String getHouseSql() {
    return "select" + " 1 as portfolioId" + " , h.*" + " , r.id as room_id" + " , r.name as room_name"
        + " , r.size_m2 as room_size_m2" + " , r.wall_type as room_wall_type" + " , r.wall_height as room_wall_height"
        + " , f.id as room_furniture_id" + " , f.description as room_furniture_description"
        + " , d.id as room_furniture_defect_id" + " , d.defect as room_furniture_defect_defect" + " from house h"
        + " left join room r on r.house_id = h.id" + " left join furniture f on f.room_id = r.id"
        + " left join defect d on d.furniture_id = f.id" + " where h.id = #{id}";
  }
}
