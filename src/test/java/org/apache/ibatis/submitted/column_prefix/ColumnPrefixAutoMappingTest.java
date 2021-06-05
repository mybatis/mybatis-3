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
package org.apache.ibatis.submitted.column_prefix;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

class ColumnPrefixAutoMappingTest extends ColumnPrefixTest {
  @Override
  protected List<Pet> getPetAndRoom(SqlSession sqlSession) {
    List<Pet> pets = sqlSession.selectList("org.apache.ibatis.submitted.column_prefix.MapperAutoMapping.selectPets");
    return pets;
  }

  @Override
  protected List<Person> getPersons(SqlSession sqlSession) {
    List<Person> list = sqlSession
        .selectList("org.apache.ibatis.submitted.column_prefix.MapperAutoMapping.selectPersons");
    return list;
  }

  @Override
  protected String getConfigPath() {
    return "org/apache/ibatis/submitted/column_prefix/ConfigAutoMapping.xml";
  }

  @Test
  void testCaseInsensitivity() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Brand brand = sqlSession.selectOne("org.apache.ibatis.submitted.column_prefix.MapperAutoMapping.selectBrandWithProducts", 1);
      assertEquals(Integer.valueOf(1), brand.getId());
      assertEquals(2, brand.getProducts().size());
      assertEquals(Integer.valueOf(10), brand.getProducts().get(0).getId());
      assertEquals("alpha", brand.getProducts().get(0).getName());
    }
  }
}
