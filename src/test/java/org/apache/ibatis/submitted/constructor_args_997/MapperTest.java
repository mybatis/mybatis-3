/*
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.constructor_args_997;

import org.apache.ibatis.BaseMapperTest;
import org.junit.Assert;
import org.junit.Test;

public class MapperTest extends BaseMapperTest {
  @Test
  public void testFindById() {
    final Mapper mapper = sqlSession.getMapper(Mapper.class);
    Child expected = new Child("C10", "Child Name", new Parent("P11", "Parent Name"));
    Assert.assertEquals(expected, mapper.findById("C10"));
  }
}
