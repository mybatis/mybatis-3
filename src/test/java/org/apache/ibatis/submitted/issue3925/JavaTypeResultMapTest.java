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
package org.apache.ibatis.submitted.issue3925;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.BooleanTypeHandler;
import org.junit.jupiter.api.Test;

class JavaTypeResultMapTest {

  @Test
  void honorsJavaTypeAttributeForMapResult() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/issue3925/mybatis-config.xml")) {
      SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
      ResultMap resultMap = factory.getConfiguration().getResultMap("org.apache.ibatis.submitted.issue3925.Mapper.foo");
      ResultMapping resultMapping = resultMap.getPropertyResultMappings().get(0);
      assertEquals(Boolean.class, resultMapping.getJavaType());
      assertEquals(BooleanTypeHandler.class, resultMapping.getTypeHandler().getClass());
      try (SqlSession session = factory.openSession()) {
        Map<String, Object> result = session.selectOne("org.apache.ibatis.submitted.issue3925.Mapper.select");
        assertEquals(Boolean.TRUE, result.get("bar"));
      }
    }
  }
}
