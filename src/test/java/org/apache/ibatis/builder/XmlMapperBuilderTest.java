/**
 *    Copyright 2009-2016 the original author or authors.
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
import java.util.regex.Pattern;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class XmlMapperBuilderTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void shouldSuccessfullyLoadXMLMapperFile() throws Exception {
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/builder/AuthorMapper.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
    builder.parse();
  }

  @Test
  public void mappedStatementWithOptions() throws Exception {
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/builder/AuthorMapper.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptions");
    assertThat(mappedStatement.getFetchSize(), is(200));
    assertThat(mappedStatement.getTimeout(), is(10));
    assertThat(mappedStatement.getStatementType(), is(StatementType.PREPARED));
    assertThat(mappedStatement.getResultSetType(), is(ResultSetType.SCROLL_SENSITIVE));
    assertThat(mappedStatement.isFlushCacheRequired(), is(false));
    assertThat(mappedStatement.isUseCache(), is(false));

  }

  @Test
  public void parseExpression() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    {
      Pattern pattern = builder.parseExpression("[0-9]", "[a-z]");
      assertThat(pattern.matcher("0").find(), is(true));
      assertThat(pattern.matcher("a").find(), is(false));
    }
    {
      Pattern pattern = builder.parseExpression(null, "[a-z]");
      assertThat(pattern.matcher("0").find(), is(false));
      assertThat(pattern.matcher("a").find(), is(true));
    }
  }

  @Test
  public void resolveJdbcTypeWithUndefinedValue() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(startsWith("Error resolving JdbcType. Cause: java.lang.IllegalArgumentException: No enum"));
    expectedException.expectMessage(endsWith("org.apache.ibatis.type.JdbcType.aaa"));
    builder.resolveJdbcType("aaa");
  }

  @Test
  public void resolveResultSetTypeWithUndefinedValue() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(startsWith("Error resolving ResultSetType. Cause: java.lang.IllegalArgumentException: No enum"));
    expectedException.expectMessage(endsWith("org.apache.ibatis.mapping.ResultSetType.bbb"));
    builder.resolveResultSetType("bbb");
  }

  @Test
  public void resolveParameterModeWithUndefinedValue() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(startsWith("Error resolving ParameterMode. Cause: java.lang.IllegalArgumentException: No enum"));
    expectedException.expectMessage(endsWith("org.apache.ibatis.mapping.ParameterMode.ccc"));
    builder.resolveParameterMode("ccc");
  }

  @Test
  public void createInstanceWithAbstractClass() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Error creating instance. Cause: java.lang.InstantiationException: org.apache.ibatis.builder.BaseBuilder"));
    builder.createInstance("org.apache.ibatis.builder.BaseBuilder");
  }

  @Test
  public void resolveClassWithNotFound() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Error resolving class. Cause: org.apache.ibatis.type.TypeException: Could not resolve type alias 'ddd'.  Cause: java.lang.ClassNotFoundException: Cannot find class: ddd"));
    builder.resolveClass("ddd");
  }

  @Test
  public void resolveTypeHandlerTypeHandlerAliasIsNull() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    TypeHandler<?> typeHandler = builder.resolveTypeHandler(String.class, (String)null);
    assertThat(typeHandler, nullValue());
  }

  @Test
  public void resolveTypeHandlerNoAssignable() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Type java.lang.Integer is not a valid TypeHandler because it does not implement TypeHandler interface"));
    builder.resolveTypeHandler(String.class, "integer");
  }

  @Test
  public void setCurrentNamespaceValueIsNull() {
    MapperBuilderAssistant builder = new MapperBuilderAssistant(new Configuration(), "resource");
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("The mapper element requires a namespace attribute to be specified."));
    builder.setCurrentNamespace(null);
  }

  @Test
  public void useCacheRefNamespaceIsNull() {
    MapperBuilderAssistant builder = new MapperBuilderAssistant(new Configuration(), "resource");
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("cache-ref element requires a namespace attribute."));
    builder.useCacheRef(null);
  }

  @Test
  public void useCacheRefNamespaceIsUndefined() {
    MapperBuilderAssistant builder = new MapperBuilderAssistant(new Configuration(), "resource");
    expectedException.expect(IncompleteElementException.class);
    expectedException.expectMessage(is("No cache for namespace 'eee' could be found."));
    builder.useCacheRef("eee");
  }

//  @Test
//  public void shouldNotLoadTheSameNamespaceFromTwoResourcesWithDifferentNames() throws Exception {
//    Configuration configuration = new Configuration();
//    String resource = "org/apache/ibatis/builder/AuthorMapper.xml";
//    InputStream inputStream = Resources.getResourceAsStream(resource);
//    XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, "name1", configuration.getSqlFragments());
//    builder.parse();
//    InputStream inputStream2 = Resources.getResourceAsStream(resource);
//    XMLMapperBuilder builder2 = new XMLMapperBuilder(inputStream2, configuration, "name2", configuration.getSqlFragments());
//    builder2.parse();
//  }

}
