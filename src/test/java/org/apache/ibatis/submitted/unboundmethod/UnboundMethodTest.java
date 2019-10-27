/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.unboundmethod;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

class UnboundMethodTest {

  @Test
  void shouldReportUnboundMethod() throws Exception {
    try (
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/unboundmethod/mybatis-config.xml")) {
      SqlSessionFactory ssFactory = new SqlSessionFactoryBuilder().build(reader);
      BuilderException e = assertThrows(BuilderException.class,
          () -> ssFactory.getConfiguration().hasStatement("org.apache.ibatis.submitted.unboundmethod.Mapper.getUser"));
      String message = e.getMessage();
      assertTrue(message.startsWith("No SQL statement bound to the methods: "));
      assertTrue(message.contains("org.apache.ibatis.submitted.unboundmethod.Mapper.countUser"));
      assertTrue(message.contains("org.apache.ibatis.submitted.unboundmethod.Mapper.insertUser"));
    }
  }

}
