/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.primitive_result_type;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;

public class IbatisConfig {

  private static SqlSessionFactory sqlSessionFactory;

  private IbatisConfig() {
  }

  private static synchronized void init() {
    if (sqlSessionFactory == null)
      try {
        final String resource = "org/apache/ibatis/submitted/primitive_result_type/ibatis.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
  }

  public static SqlSession getSession() {
    if (sqlSessionFactory == null) {
      init();
    }
    return sqlSessionFactory.openSession();
  }
}