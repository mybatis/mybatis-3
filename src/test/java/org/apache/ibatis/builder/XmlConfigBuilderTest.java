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
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.builder.mapper.CustomMapper;
import org.apache.ibatis.builder.typehandler.CustomIntegerTypeHandler;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
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
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.junit.Assert.*;

public class XmlConfigBuilderTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
    String resource = "org/apache/ibatis/builder/MinimalMapperConfig.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
    Configuration config = builder.parse();
    assertNotNull(config);
    assertThat(config.getAutoMappingBehavior(), is(AutoMappingBehavior.PARTIAL));
    assertThat(config.getAutoMappingUnknownColumnBehavior(), is(AutoMappingUnknownColumnBehavior.NONE));
    assertThat(config.isCacheEnabled(), is(true));
    assertThat(config.getProxyFactory(), is(instanceOf(JavassistProxyFactory.class)));
    assertThat(config.isLazyLoadingEnabled(), is(false));
    assertThat(config.isAggressiveLazyLoading(), is(true));
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
      assertThat(config.getDefaultScriptingLanuageInstance(), is(instanceOf(XMLLanguageDriver.class)));
    assertThat(config.isCallSettersOnNulls(), is(false));
    assertNull(config.getLogPrefix());
    assertNull(config.getLogImpl());
    assertNull(config.getConfigurationFactory());
  }

  enum MyEnum {
    ONE, TWO
  }

  public static class EnumOrderTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private E[] constants;

    public EnumOrderTypeHandler(Class<E> javaType) {
      constants = javaType.getEnumConstants();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
      ps.setInt(i, parameter.ordinal() + 1); // 0 means NULL so add +1
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
      int index = rs.getInt(columnName) - 1;
      return index < 0 ? null : constants[index];
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      int index = rs.getInt(rs.getInt(columnIndex)) - 1;
      return index < 0 ? null : constants[index];
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      int index = cs.getInt(columnIndex) - 1;
      return index < 0 ? null : constants[index];
    }
  }

  @Test
  public void registerJavaTypeInitializingTypeHandler() {
    final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
        + "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n" 
        + "<configuration>\n" 
        + "  <typeHandlers>\n"
        + "    <typeHandler javaType=\"org.apache.ibatis.builder.XmlConfigBuilderTest$MyEnum\"\n"
        + "      handler=\"org.apache.ibatis.builder.XmlConfigBuilderTest$EnumOrderTypeHandler\"/>\n" 
        + "  </typeHandlers>\n" 
        + "</configuration>\n";

    XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(MAPPER_CONFIG));
    builder.parse();

    TypeHandlerRegistry typeHandlerRegistry = builder.getConfiguration().getTypeHandlerRegistry();
    TypeHandler<MyEnum> typeHandler = typeHandlerRegistry.getTypeHandler(MyEnum.class);

    assertTrue(typeHandler instanceof EnumOrderTypeHandler);
    assertArrayEquals(MyEnum.values(), ((EnumOrderTypeHandler) typeHandler).constants);
  }

    @Test
    public void shouldSuccessfullyLoadXMLConfigFile() throws Exception {
      String resource = "org/apache/ibatis/builder/CustomizedSettingsMapperConfig.xml";
      InputStream inputStream = Resources.getResourceAsStream(resource);
      Properties props = new Properties();
      props.put("prop2", "cccc");
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream, null, props);
      Configuration config = builder.parse();

      assertThat(config.getAutoMappingBehavior(), is(AutoMappingBehavior.NONE));
      assertThat(config.getAutoMappingUnknownColumnBehavior(), is(AutoMappingUnknownColumnBehavior.WARNING));
      assertThat(config.isCacheEnabled(), is(false));
      assertThat(config.getProxyFactory(), is(instanceOf(CglibProxyFactory.class)));
      assertThat(config.isLazyLoadingEnabled(), is(true));
      assertThat(config.isAggressiveLazyLoading(), is(false));
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
      assertThat(config.getDefaultScriptingLanuageInstance(), is(instanceOf(RawLanguageDriver.class)));
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

    }

  @Test
  public void shouldSuccessfullyLoadXMLConfigFileWithPropertiesUrl() throws Exception {
    String resource = "org/apache/ibatis/builder/PropertiesUrlMapperConfig.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
    Configuration config = builder.parse();
    assertThat(config.getVariables().get("driver").toString(), is("org.apache.derby.jdbc.EmbeddedDriver"));
    assertThat(config.getVariables().get("prop1").toString(), is("bbbb"));

  }

  @Test
  public void parseIsTwice() throws Exception {
    String resource = "org/apache/ibatis/builder/MinimalMapperConfig.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
    builder.parse();

    expectedException.expect(BuilderException.class);
    expectedException.expectMessage("Each XMLConfigBuilder can only be used once.");
    builder.parse();
  }

  @Test
  public void unknownSettings() {
    final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n"
            + "<configuration>\n"
            + "  <settings>\n"
            + "    <setting name=\"foo\" value=\"bar\"/>\n"
            + "  </settings>\n"
            + "</configuration>\n";

    expectedException.expect(BuilderException.class);
    expectedException.expectMessage("The setting foo is not known.  Make sure you spelled it correctly (case sensitive).");

    XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(MAPPER_CONFIG));
    builder.parse();
  }

  @Test
  public void unknownJavaTypeOnTypeHandler() {
    final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n"
            + "<configuration>\n"
            + "  <typeAliases>\n"
            + "    <typeAlias type=\"a.b.c.Foo\"/>\n"
            + "  </typeAliases>\n"
            + "</configuration>\n";

    expectedException.expect(BuilderException.class);
    expectedException.expectMessage("Error registering typeAlias for 'null'. Cause: ");

    XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(MAPPER_CONFIG));
    builder.parse();
  }

  @Test
  public void propertiesSpecifyResourceAndUrlAtSameTime() {
    final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n"
            + "<configuration>\n"
            + "  <properties resource=\"a/b/c/foo.properties\" url=\"file:./a/b/c/jdbc.properties\"/>\n"
            + "</configuration>\n";

    expectedException.expect(BuilderException.class);
    expectedException.expectMessage("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");

    XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(MAPPER_CONFIG));
    builder.parse();
  }

}
