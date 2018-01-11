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
package org.apache.ibatis.builder;

import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.junit.Rule;
import org.apache.ibatis.type.TypeHandler;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlMapperBuilderTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void shouldSuccessfullyLoadXMLMapperFile() throws Exception {
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/builder/AuthorMapper.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
    builder.parse();
    inputStream.close();
  }

  @Test
  public void mappedStatementWithOptions() throws Exception {
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/builder/AuthorMapper.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptions");
    assertThat(mappedStatement.getFetchSize()).isEqualTo(200);
    assertThat(mappedStatement.getTimeout()).isEqualTo(10);
    assertThat(mappedStatement.getStatementType()).isEqualTo(StatementType.PREPARED);
    assertThat(mappedStatement.getResultSetType()).isEqualTo(ResultSetType.SCROLL_SENSITIVE);
    assertThat(mappedStatement.isFlushCacheRequired()).isFalse();
    assertThat(mappedStatement.isUseCache()).isFalse();
    inputStream.close();
  }

  @Test
  public void parseExpression() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    {
      Pattern pattern = builder.parseExpression("[0-9]", "[a-z]");
      assertThat(pattern.matcher("0").find()).isTrue();
      assertThat(pattern.matcher("a").find()).isFalse();
    }
    {
      Pattern pattern = builder.parseExpression(null, "[a-z]");
      assertThat(pattern.matcher("0").find()).isFalse();
      assertThat(pattern.matcher("a").find()).isTrue();
    }
  }

  @Test
  public void resolveJdbcTypeWithUndefinedValue() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    when(builder).resolveJdbcType("aaa");
    then(caughtException())
      .isInstanceOf(BuilderException.class)
      .hasMessageStartingWith("Error resolving JdbcType. Cause: java.lang.IllegalArgumentException: No enum")
      .hasMessageEndingWith("org.apache.ibatis.type.JdbcType.aaa");
  }

  @Test
  public void resolveResultSetTypeWithUndefinedValue() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    when(builder).resolveResultSetType("bbb");
    then(caughtException())
      .isInstanceOf(BuilderException.class)
      .hasMessageStartingWith("Error resolving ResultSetType. Cause: java.lang.IllegalArgumentException: No enum")
      .hasMessageEndingWith("org.apache.ibatis.mapping.ResultSetType.bbb");
  }

  @Test
  public void resolveParameterModeWithUndefinedValue() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    when(builder).resolveParameterMode("ccc");
    then(caughtException())
      .isInstanceOf(BuilderException.class)
      .hasMessageStartingWith("Error resolving ParameterMode. Cause: java.lang.IllegalArgumentException: No enum")
      .hasMessageEndingWith("org.apache.ibatis.mapping.ParameterMode.ccc");
  }

  @Test
  public void createInstanceWithAbstractClass() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    when(builder).createInstance("org.apache.ibatis.builder.BaseBuilder");
    then(caughtException())
      .isInstanceOf(BuilderException.class)
      .hasMessage("Error creating instance. Cause: java.lang.InstantiationException: org.apache.ibatis.builder.BaseBuilder");
  }

  @Test
  public void resolveClassWithNotFound() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    when(builder).resolveClass("ddd");
    then(caughtException())
      .isInstanceOf(BuilderException.class)
      .hasMessage("Error resolving class. Cause: org.apache.ibatis.type.TypeException: Could not resolve type alias 'ddd'.  Cause: java.lang.ClassNotFoundException: Cannot find class: ddd");
  }

  @Test
  public void resolveTypeHandlerTypeHandlerAliasIsNull() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    TypeHandler<?> typeHandler = builder.resolveTypeHandler(String.class, (String)null);
    assertThat(typeHandler).isNull();
  }

  @Test
  public void resolveTypeHandlerNoAssignable() {
    BaseBuilder builder = new BaseBuilder(new Configuration()){{}};
    when(builder).resolveTypeHandler(String.class, "integer");
    then(caughtException())
      .isInstanceOf(BuilderException.class)
      .hasMessage("Type java.lang.Integer is not a valid TypeHandler because it does not implement TypeHandler interface");
  }

  @Test
  public void setCurrentNamespaceValueIsNull() {
    MapperBuilderAssistant builder = new MapperBuilderAssistant(new Configuration(), "resource");
    when(builder).setCurrentNamespace(null);
    then(caughtException())
      .isInstanceOf(BuilderException.class)
      .hasMessage("The mapper element requires a namespace attribute to be specified.");
  }

  @Test
  public void useCacheRefNamespaceIsNull() {
    MapperBuilderAssistant builder = new MapperBuilderAssistant(new Configuration(), "resource");
    when(builder).useCacheRef(null);
    then(caughtException())
      .isInstanceOf(BuilderException.class)
      .hasMessage("cache-ref element requires a namespace attribute.");
  }

  @Test
  public void useCacheRefNamespaceIsUndefined() {
    MapperBuilderAssistant builder = new MapperBuilderAssistant(new Configuration(), "resource");
    when(builder).useCacheRef("eee");
    then(caughtException())
      .hasMessage("No cache for namespace 'eee' could be found.");
  }

  @Test
  public void shouldFailedLoadXMLMapperFile() throws Exception {
    expectedEx.expect(BuilderException.class);
    expectedEx.expectMessage("Error parsing Mapper XML. The XML location is 'org/apache/ibatis/builder/ProblemMapper.xml'");
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/builder/ProblemMapper.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
    builder.parse();
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
