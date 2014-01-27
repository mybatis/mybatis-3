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
package org.apache.ibatis.builder;

import java.io.InputStream;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class IncludeMapperXmlBuilderTest {

  @Test
  public void shouldSuccessfullyLoadIncludeXMLMapperFile() throws Exception {
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/builder/IncludeMapper.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
    builder.parse();

    String sqlOutput = configuration.getMappedStatement("com.domain.IncludeMapper.selectInclude").getSqlSource().getBoundSql(null).getSql();

    String[] lines = sqlOutput.split("\n");
    Assert.assertEquals("x.a as x_a, x.b as x_b, x.c as x_c ,", lines[1].trim());
    Assert.assertEquals("y.a as q_a, y.b as q_b, y.c as q_c ,", lines[2].trim());
    Assert.assertEquals("z.p as z, z.q as y, z.r as z_r", lines[3].trim());
  }

}
