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
package org.apache.ibatis.builder.xsd;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.ClassLoaderResource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class XmlMapperBuilderTest {

  @Test
  void mappedStatementWithOptions() throws Exception {
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/builder/xsd/AuthorMapper.xml";
    XMLMapperBuilder builder = new XMLMapperBuilder(new ClassLoaderResource(resource, null), configuration,
        configuration.getSqlFragments());
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptions");
    assertEquals(Integer.valueOf(200), mappedStatement.getFetchSize());
    assertEquals(Integer.valueOf(10), mappedStatement.getTimeout());
    assertEquals(StatementType.PREPARED, mappedStatement.getStatementType());
    assertEquals(ResultSetType.SCROLL_SENSITIVE, mappedStatement.getResultSetType());
    assertFalse(mappedStatement.isFlushCacheRequired());
    assertFalse(mappedStatement.isUseCache());
  }

}
