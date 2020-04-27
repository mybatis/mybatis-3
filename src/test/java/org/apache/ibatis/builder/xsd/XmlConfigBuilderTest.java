/**
 *    Copyright 2009-2020 the original author or authors.
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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

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
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("We'll try a different approach. See #1393")
class XmlConfigBuilderTest {

  @Test
  void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
    // System.setProperty(XPathParser.KEY_USE_XSD, "true");
    String resource = "org/apache/ibatis/builder/xsd/MinimalMapperConfig.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
      Configuration config = builder.parse();
      assertNotNull(config);
      assertEquals(AutoMappingBehavior.PARTIAL, config.getAutoMappingBehavior());
      assertEquals(AutoMappingUnknownColumnBehavior.NONE, config.getAutoMappingUnknownColumnBehavior());
      assertTrue(config.isCacheEnabled());
      assertTrue(config.getProxyFactory() instanceof JavassistProxyFactory);
      assertFalse(config.isLazyLoadingEnabled());
      assertFalse(config.isAggressiveLazyLoading());
      assertTrue(config.isMultipleResultSetsEnabled());
      assertTrue(config.isUseColumnLabel());
      assertFalse(config.isUseGeneratedKeys());
      assertEquals(ExecutorType.SIMPLE, config.getDefaultExecutorType());
      assertNull(config.getDefaultStatementTimeout());
      assertNull(config.getDefaultFetchSize());
      assertFalse(config.isMapUnderscoreToCamelCase());
      assertFalse(config.isSafeRowBoundsEnabled());
      assertEquals(LocalCacheScope.SESSION, config.getLocalCacheScope());
      assertEquals(JdbcType.OTHER, config.getJdbcTypeForNull());
      assertEquals(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString")), config.getLazyLoadTriggerMethods());
      assertTrue(config.isSafeResultHandlerEnabled());
      assertTrue(config.getDefaultScriptingLanguageInstance() instanceof XMLLanguageDriver);
      assertFalse(config.isCallSettersOnNulls());
      assertNull(config.getLogPrefix());
      assertNull(config.getLogImpl());
      assertNull(config.getConfigurationFactory());
    } finally {
      // System.clearProperty(XPathParser.KEY_USE_XSD);
    }
  }

  @Test
  void shouldSuccessfullyLoadXMLConfigFile() throws Exception {
    // System.setProperty(XPathParser.KEY_USE_XSD, "true");
    String resource = "org/apache/ibatis/builder/xsd/CustomizedSettingsMapperConfig.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
      Configuration config = builder.parse();

      assertEquals(AutoMappingBehavior.NONE, config.getAutoMappingBehavior());
      assertEquals(AutoMappingUnknownColumnBehavior.WARNING, config.getAutoMappingUnknownColumnBehavior());
      assertFalse(config.isCacheEnabled());
      assertTrue(config.getProxyFactory() instanceof CglibProxyFactory);
      assertTrue(config.isLazyLoadingEnabled());
      assertTrue(config.isAggressiveLazyLoading());
      assertFalse(config.isMultipleResultSetsEnabled());
      assertFalse(config.isUseColumnLabel());
      assertTrue(config.isUseGeneratedKeys());
      assertEquals(ExecutorType.BATCH, config.getDefaultExecutorType());
      assertEquals(Integer.valueOf(10), config.getDefaultStatementTimeout());
      assertEquals(Integer.valueOf(100), config.getDefaultFetchSize());
      assertTrue(config.isMapUnderscoreToCamelCase());
      assertTrue(config.isSafeRowBoundsEnabled());
      assertEquals(LocalCacheScope.STATEMENT, config.getLocalCacheScope());
      assertEquals(JdbcType.NULL, config.getJdbcTypeForNull());
      assertEquals(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString", "xxx")), config.getLazyLoadTriggerMethods());
      assertFalse(config.isSafeResultHandlerEnabled());
      assertTrue(config.getDefaultScriptingLanguageInstance() instanceof RawLanguageDriver);
      assertTrue(config.isCallSettersOnNulls());
      assertEquals("mybatis_", config.getLogPrefix());
      assertEquals(Slf4jImpl.class.getName(), config.getLogImpl().getName());
      assertEquals(JBoss6VFS.class.getName(), config.getVfsImpl().getName());
      assertEquals(String.class.getName(), config.getConfigurationFactory().getName());

      assertEquals(Author.class, config.getTypeAliasRegistry().getTypeAliases().get("blogauthor"));
      assertEquals(Blog.class, config.getTypeAliasRegistry().getTypeAliases().get("blog"));
      assertEquals(Cart.class, config.getTypeAliasRegistry().getTypeAliases().get("cart"));

      assertTrue(config.getTypeHandlerRegistry().getTypeHandler(Integer.class) instanceof CustomIntegerTypeHandler);
      assertTrue(config.getTypeHandlerRegistry().getTypeHandler(Long.class) instanceof CustomLongTypeHandler);
      assertTrue(config.getTypeHandlerRegistry().getTypeHandler(String.class) instanceof CustomStringTypeHandler);
      assertTrue(config.getTypeHandlerRegistry().getTypeHandler(String.class, JdbcType.VARCHAR) instanceof CustomStringTypeHandler);

      ExampleObjectFactory objectFactory = (ExampleObjectFactory)config.getObjectFactory();
      assertEquals(1, objectFactory.getProperties().size());
      assertEquals("100", objectFactory.getProperties().getProperty("objectFactoryProperty"));

      assertTrue(config.getObjectWrapperFactory() instanceof CustomObjectWrapperFactory);

      assertTrue(config.getReflectorFactory() instanceof CustomReflectorFactory);

      ExamplePlugin plugin = (ExamplePlugin)config.getInterceptors().get(0);
      assertEquals(1, plugin.getProperties().size());
      assertEquals("100", plugin.getProperties().getProperty("pluginProperty"));

      Environment environment = config.getEnvironment();
      assertEquals("development", environment.getId());
      assertTrue(environment.getDataSource() instanceof UnpooledDataSource);
      assertTrue(environment.getTransactionFactory() instanceof JdbcTransactionFactory);

      assertEquals("derby", config.getDatabaseId());

      assertEquals(4, config.getMapperRegistry().getMappers().size());
      assertTrue(config.getMapperRegistry().hasMapper(CachedAuthorMapper.class));
      assertTrue(config.getMapperRegistry().hasMapper(CustomMapper.class));
      assertTrue(config.getMapperRegistry().hasMapper(BlogMapper.class));
      assertTrue(config.getMapperRegistry().hasMapper(NestedBlogMapper.class));
    } finally {
      // System.clearProperty(XPathParser.KEY_USE_XSD);
    }
  }

}
