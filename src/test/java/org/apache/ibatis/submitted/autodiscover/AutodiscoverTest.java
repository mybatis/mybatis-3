/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.autodiscover;

import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.math.BigInteger;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.autodiscover.mappers.DummyMapper;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

public class AutodiscoverTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/autodiscover/MapperConfig.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();
  }

  @Test
  public void testTypeAlias() {
    TypeAliasRegistry typeAliasRegistry = sqlSessionFactory.getConfiguration().getTypeAliasRegistry();
    typeAliasRegistry.resolveAlias("testAlias");
  }

  @Test
  public void testTypeHandler() {
    TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
    assertTrue(typeHandlerRegistry.hasTypeHandler(BigInteger.class));
  }

  @Test
  public void testMapper() {
    assertTrue(sqlSessionFactory.getConfiguration().hasMapper(DummyMapper.class));
  }
}
