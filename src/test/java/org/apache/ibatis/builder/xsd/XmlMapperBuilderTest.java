/**
 *    Copyright 2009-2018 the original author or authors.
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

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class XmlMapperBuilderTest {

  @Test
  public void mappedStatementWithOptions() throws Exception {
    System.setProperty(XPathParser.KEY_USE_XSD, "true");
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/builder/xsd/AuthorMapper.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
      builder.parse();

      MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptions");
      assertThat(mappedStatement.getFetchSize(), is(200));
      assertThat(mappedStatement.getTimeout(), is(10));
      assertThat(mappedStatement.getStatementType(), is(StatementType.PREPARED));
      assertThat(mappedStatement.getResultSetType(), is(ResultSetType.SCROLL_SENSITIVE));
      assertThat(mappedStatement.isFlushCacheRequired(), is(false));
      assertThat(mappedStatement.isUseCache(), is(false));
    } finally {
      System.clearProperty(XPathParser.KEY_USE_XSD);
    }
  }

}
