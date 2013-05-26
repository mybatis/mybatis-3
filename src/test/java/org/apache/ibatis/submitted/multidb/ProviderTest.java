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
package org.apache.ibatis.submitted.multidb;

import static org.junit.Assert.assertEquals;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.Test;

public class ProviderTest {

  @Test
  public void shouldUseDefaultId() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multidb/MultiDbConfig.xml");
    DefaultSqlSessionFactory sqlSessionFactory = (DefaultSqlSessionFactory) new SqlSessionFactoryBuilder().build(reader);
    Configuration c = sqlSessionFactory.getConfiguration();
    assertEquals("hsql", c.getDatabaseId());
  }

  @Test
  public void shouldUseProvider() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multidb/ProviderConfig.xml");
    DefaultSqlSessionFactory sqlSessionFactory = (DefaultSqlSessionFactory) new SqlSessionFactoryBuilder().build(reader);
    Configuration c = sqlSessionFactory.getConfiguration();
    assertEquals("translated", c.getDatabaseId());
  }
}