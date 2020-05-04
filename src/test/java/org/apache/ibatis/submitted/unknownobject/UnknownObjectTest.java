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
package org.apache.ibatis.submitted.unknownobject;

import java.io.Reader;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnknownObjectTest {

  @Test
  void shouldFailBecauseThereIsAPropertyWithoutTypeHandler() throws Exception {
    // create a SqlSessionFactory
    try (
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/unknownobject/mybatis-config.xml")) {
      Assertions.assertThrows(PersistenceException.class, () -> new SqlSessionFactoryBuilder().build(reader));
    }
  }

}
