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

import org.apache.ibatis.builder.CustomLongTypeHandler;
import org.apache.ibatis.builder.CustomObjectWrapperFactory;
import org.apache.ibatis.builder.CustomReflectorFactory;
import org.apache.ibatis.builder.CustomStringTypeHandler;
import org.apache.ibatis.builder.ExampleObjectFactory;
import org.apache.ibatis.builder.ExamplePlugin;
import org.apache.ibatis.builder.mapper.CustomMapper;
import org.apache.ibatis.builder.typehandler.CustomIntegerTypeHandler;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Blog;
import org.apache.ibatis.domain.blog.mappers.BlogMapper;
import org.apache.ibatis.domain.blog.mappers.NestedBlogMapper;
import org.apache.ibatis.domain.jpetstore.Cart;
import org.apache.ibatis.executor.loader.cglib.CglibProxyFactory;
import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.apache.ibatis.io.JBoss6VFS;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

public class XmlConfigBuilderTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
    System.setProperty(XPathParser.KEY_USE_XSD, "true");
    String resource = "org/apache/ibatis/builder/xsd/MinimalMapperConfig.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
      Configuration config = builder.parse();
      assertNotNull(config);
      assertThat(config.getAutoMappingBehavior(), is(AutoMappingBehavior.PARTIAL));
      assertThat(config.getAutoMappingUnknownColumnBehavior(), is(AutoMappingUnknownColumnBehavior.NONE));
      assertThat(config.isCacheEnabled(), is(true));
      assertThat(config.getProxyFactory(), is(instanceOf(JavassistProxyFactory.class)));
      assertThat(config.isLazyLoadingEnabled(), is(false));
      assertThat(config.isAggressiveLazyLoading(), is(false));
      assertThat(config.isMultipleResultSetsEnabled(), is(true));
      assertThat(config.isUseColumnLabel(), is(true));
      assertThat(config.isUseGeneratedKeys(), is(false));
      assertThat(config.getDefaultExecutorType(), is(ExecutorType.SIMPLE));
      assertNull(config.getDefaultStatementTimeout());
      assertNull(config.getDefaultFetchSize());
      assertThat(config.isMapUnderscoreToCamelCase(), is(false));
      assertThat(config.isSafeRowBoundsEnabled(), is(false));
      assertThat(config.getLocalCacheScope(), is(LocalCacheScope.SESSION));
      assertThat(config.getJdbcTypeForNull(), is(JdbcType.OTHER));
      assertThat(config.getLazyLoadTriggerMethods(), is((Set<String>) new HashSet<String>(Arrays.asList("equals", "clone", "hashCode", "toString"))));
      assertThat(config.isSafeResultHandlerEnabled(), is(true));
      assertThat(config.getDefaultScriptingLanguageInstance(), is(instanceOf(XMLLanguageDriver.class)));
      assertThat(config.isCallSettersOnNulls(), is(false));
      assertNull(config.getLogPrefix());
      assertNull(config.getLogImpl());
      assertNull(config.getConfigurationFactory());
    } finally {
      System.clearProperty(XPathParser.KEY_USE_XSD);
    }
  }

  @Test
  public void shouldSuccessfullyLoadXMLConfigFitle() throws Exception {
    System.setProperty(XPathParser.KEY_USE_XSD, "true");
    String resource = "org/apache/ibatis/builder/xsd/CustomizedSettingsMapperConfig.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
      Configuration config = builder.parse();

      assertThat(config.getAutoMappingBehavior(), is(AutoMappingBehavior.NONE));
      assertThat(config.getAutoMappingUnknownColumnBehavior(), is(AutoMappingUnknownColumnBehavior.WARNING));
      assertThat(config.isCacheEnabled(), is(false));
      assertThat(config.getProxyFactory(), is(instanceOf(CglibProxyFactory.class)));
      assertThat(config.isLazyLoadingEnabled(), is(true));
      assertThat(config.isAggressiveLazyLoading(), is(true));
      assertThat(config.isMultipleResultSetsEnabled(), is(false));
      assertThat(config.isUseColumnLabel(), is(false));
      assertThat(config.isUseGeneratedKeys(), is(true));
      assertThat(config.getDefaultExecutorType(), is(ExecutorType.BATCH));
      assertThat(config.getDefaultStatementTimeout(), is(10));
      assertThat(config.getDefaultFetchSize(), is(100));
      assertThat(config.isMapUnderscoreToCamelCase(), is(true));
      assertThat(config.isSafeRowBoundsEnabled(), is(true));
      assertThat(config.getLocalCacheScope(), is(LocalCacheScope.STATEMENT));
      assertThat(config.getJdbcTypeForNull(), is(JdbcType.NULL));
      assertThat(config.getLazyLoadTriggerMethods(), is((Set<String>) new HashSet<String>(Arrays.asList("equals", "clone", "hashCode", "toString", "xxx"))));
      assertThat(config.isSafeResultHandlerEnabled(), is(false));
      assertThat(config.getDefaultScriptingLanguageInstance(), is(instanceOf(RawLanguageDriver.class)));
      assertThat(config.isCallSettersOnNulls(), is(true));
      assertThat(config.getLogPrefix(), is("mybatis_"));
      assertThat(config.getLogImpl().getName(), is(Slf4jImpl.class.getName()));
      assertThat(config.getVfsImpl().getName(), is(JBoss6VFS.class.getName()));
      assertThat(config.getConfigurationFactory().getName(), is(String.class.getName()));

      assertTrue(config.getTypeAliasRegistry().getTypeAliases().get("blogauthor").equals(Author.class));
      assertTrue(config.getTypeAliasRegistry().getTypeAliases().get("blog").equals(Blog.class));
      assertTrue(config.getTypeAliasRegistry().getTypeAliases().get("cart").equals(Cart.class));

      assertThat(config.getTypeHandlerRegistry().getTypeHandler(Integer.class), is(instanceOf(CustomIntegerTypeHandler.class)));
      assertThat(config.getTypeHandlerRegistry().getTypeHandler(Long.class), is(instanceOf(CustomLongTypeHandler.class)));
      assertThat(config.getTypeHandlerRegistry().getTypeHandler(String.class), is(instanceOf(CustomStringTypeHandler.class)));
      assertThat(config.getTypeHandlerRegistry().getTypeHandler(String.class, JdbcType.VARCHAR), is(instanceOf(CustomStringTypeHandler.class)));

      ExampleObjectFactory objectFactory = (ExampleObjectFactory)config.getObjectFactory();
      assertThat(objectFactory.getProperties().size(), is(1));
      assertThat(objectFactory.getProperties().getProperty("objectFactoryProperty"), is("100"));

      assertThat(config.getObjectWrapperFactory(), is(instanceOf(CustomObjectWrapperFactory.class)));

      assertThat(config.getReflectorFactory(), is(instanceOf(CustomReflectorFactory.class)));

      ExamplePlugin plugin = (ExamplePlugin)config.getInterceptors().get(0);
      assertThat(plugin.getProperties().size(), is(1));
      assertThat(plugin.getProperties().getProperty("pluginProperty"), is("100"));

      Environment environment = config.getEnvironment();
      assertThat(environment.getId(), is("development"));
      assertThat(environment.getDataSource(), is(instanceOf(UnpooledDataSource.class)));
      assertThat(environment.getTransactionFactory(), is(instanceOf(JdbcTransactionFactory.class)));

      assertThat(config.getDatabaseId(), is("derby"));

      assertThat(config.getMapperRegistry().getMappers().size(), is(4));
      assertThat(config.getMapperRegistry().hasMapper(CachedAuthorMapper.class), is(true));
      assertThat(config.getMapperRegistry().hasMapper(CustomMapper.class), is(true));
      assertThat(config.getMapperRegistry().hasMapper(BlogMapper.class), is(true));
      assertThat(config.getMapperRegistry().hasMapper(NestedBlogMapper.class), is(true));
    } finally {
      System.clearProperty(XPathParser.KEY_USE_XSD);
    }
  }

}
