/**
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
package org.apache.ibatis.submitted.orphan_result_maps;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class OrphanResultMapTest {

  private static String RESULT_MAP_BLOG = "BlogResultMap";
  private static String RESULT_MAP_POST = "PostResultMap";
  private static String RESULT_MAP_INNER = "mapper_resultMap[BlogResultMap]_collection[posts]";

  @Test
  void testSeparateResultMaps() {
    // given
    Configuration configuration = new Configuration();
    configuration.getTypeAliasRegistry().registerAlias(Blog.class);
    configuration.getTypeAliasRegistry().registerAlias(Post.class);
    configuration.addMapper(SeparateCollectionMapper.class);

    // there should be two result maps declared, with two name variants each
    assertEquals(4, configuration.getResultMaps().size());

    // test short names
    assertNotNull(configuration.getResultMap(RESULT_MAP_BLOG));
    assertNotNull(configuration.getResultMap(RESULT_MAP_POST));
    assertThrows(IllegalArgumentException.class, () -> configuration.getResultMap(RESULT_MAP_INNER));

    // test long names
    String prefix = SeparateCollectionMapper.class.getName() + ".";
    assertNotNull(configuration.getResultMap(prefix + RESULT_MAP_BLOG));
    assertNotNull(configuration.getResultMap(prefix + RESULT_MAP_POST));
    assertThrows(IllegalArgumentException.class, () -> configuration.getResultMap(prefix + RESULT_MAP_INNER));
  }

  @Test
  void testNestedResultMap() {
    // given
    Configuration configuration = new Configuration();
    configuration.getTypeAliasRegistry().registerAlias(Blog.class);
    configuration.getTypeAliasRegistry().registerAlias(Post.class);
    configuration.addMapper(NestedCollectionMapper.class);

    // there should be two result maps declared, with two name variants each
    assertEquals(4, configuration.getResultMaps().size());

    // test short names
    assertNotNull(configuration.getResultMap(RESULT_MAP_BLOG));
    assertNotNull(configuration.getResultMap(RESULT_MAP_INNER));

    // test long names
    String prefix = NestedCollectionMapper.class.getName() + ".";
    assertNotNull(configuration.getResultMap(prefix + RESULT_MAP_BLOG));
    assertNotNull(configuration.getResultMap(prefix + RESULT_MAP_INNER));
  }

}
