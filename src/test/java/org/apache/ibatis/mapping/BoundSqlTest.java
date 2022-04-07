/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.mapping;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class BoundSqlTest {

  @Test
  void testHasAdditionalParameter() {
    List<ParameterMapping> params = Collections.emptyList();
    BoundSql boundSql = new BoundSql(new Configuration(), "some sql", params, new Object());

    Map<String, String> map = new HashMap<>();
    map.put("key1", "value1");
    boundSql.setAdditionalParameter("map", map);

    Person bean = new Person();
    bean.id = 1;
    boundSql.setAdditionalParameter("person", bean);

    String[] array = new String[] {"User1", "User2"};
    boundSql.setAdditionalParameter("array", array);

    assertFalse(boundSql.hasAdditionalParameter("pet"));
    assertFalse(boundSql.hasAdditionalParameter("pet.name"));

    assertTrue(boundSql.hasAdditionalParameter("map"));
    assertTrue(boundSql.hasAdditionalParameter("map.key1"));
    assertTrue(boundSql.hasAdditionalParameter("map.key2"), "should return true even if the child property does not exists.");

    assertTrue(boundSql.hasAdditionalParameter("person"));
    assertTrue(boundSql.hasAdditionalParameter("person.id"));
    assertTrue(boundSql.hasAdditionalParameter("person.name"), "should return true even if the child property does not exists.");

    assertTrue(boundSql.hasAdditionalParameter("array[0]"));
    assertTrue(boundSql.hasAdditionalParameter("array[99]"), "should return true even if the element does not exists.");
  }

  public static class Person {
    public Integer id;
  }

}
