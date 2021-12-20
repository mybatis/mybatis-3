/*
 *    Copyright 2009-2021 the original author or authors.
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

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.StringReader;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

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
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class XmlConfigBuilderTest {

  @Test
  void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
    String resource = "org/apache/ibatis/builder/MinimalMapperConfig.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
      Configuration config = builder.parse();
      assertNotNull(config);
      assertThat(config.getAutoMappingBehavior()).isEqualTo(AutoMappingBehavior.PARTIAL);
      assertThat(config.getAutoMappingUnknownColumnBehavior()).isEqualTo(AutoMappingUnknownColumnBehavior.NONE);
      assertThat(config.isCacheEnabled()).isTrue();
      assertThat(config.getProxyFactory()).isInstanceOf(JavassistProxyFactory.class);
      assertThat(config.isLazyLoadingEnabled()).isFalse();
      assertThat(config.isAggressiveLazyLoading()).isFalse();
      assertThat(config.isMultipleResultSetsEnabled()).isTrue();
      assertThat(config.isUseColumnLabel()).isTrue();
      assertThat(config.isUseGeneratedKeys()).isFalse();
      assertThat(config.getDefaultExecutorType()).isEqualTo(ExecutorType.SIMPLE);
      assertNull(config.getDefaultStatementTimeout());
      assertNull(config.getDefaultFetchSize());
      assertNull(config.getDefaultResultSetType());
      assertThat(config.isMapUnderscoreToCamelCase()).isFalse();
      assertThat(config.isSafeRowBoundsEnabled()).isFalse();
      assertThat(config.getLocalCacheScope()).isEqualTo(LocalCacheScope.SESSION);
      assertThat(config.getJdbcTypeForNull()).isEqualTo(JdbcType.OTHER);
      assertThat(config.getLazyLoadTriggerMethods()).isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString")));
      assertThat(config.isSafeResultHandlerEnabled()).isTrue();
      assertThat(config.getDefaultScriptingLanuageInstance()).isInstanceOf(XMLLanguageDriver.class);
      assertThat(config.isCallSettersOnNulls()).isFalse();
      assertNull(config.getLogPrefix());
      assertNull(config.getLogImpl());
      assertNull(config.getConfigurationFactory());
      assertThat(config.getTypeHandlerRegistry().getTypeHandler(RoundingMode.class)).isInstanceOf(EnumTypeHandler.class);
      assertThat(config.isShrinkWhitespacesInSql()).isFalse();
      assertThat(config.getDefaultSqlProviderType()).isNull();
      assertThat(config.isNullableOnForEach()).isFalse();
    }
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
  void registerJavaTypeInitializingTypeHandler() {
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
    assertArrayEquals(MyEnum.values(), ((EnumOrderTypeHandler<MyEnum>) typeHandler).constants);
  }

  @Tag("RequireIllegalAccess")
  @Test
  void shouldSuccessfullyLoadXMLConfigFile() throws Exception {
    String resource = "org/apache/ibatis/builder/CustomizedSettingsMapperConfig.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      Properties props = new Properties();
      props.put("prop2", "cccc");
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream, null, props);
      Configuration config = builder.parse();

      assertThat(config.getAutoMappingBehavior()).isEqualTo(AutoMappingBehavior.NONE);
      assertThat(config.getAutoMappingUnknownColumnBehavior()).isEqualTo(AutoMappingUnknownColumnBehavior.WARNING);
      assertThat(config.isCacheEnabled()).isFalse();
      assertThat(config.getProxyFactory()).isInstanceOf(CglibProxyFactory.class);
      assertThat(config.isLazyLoadingEnabled()).isTrue();
      assertThat(config.isAggressiveLazyLoading()).isTrue();
      assertThat(config.isMultipleResultSetsEnabled()).isFalse();
      assertThat(config.isUseColumnLabel()).isFalse();
      assertThat(config.isUseGeneratedKeys()).isTrue();
      assertThat(config.getDefaultExecutorType()).isEqualTo(ExecutorType.BATCH);
      assertThat(config.getDefaultStatementTimeout()).isEqualTo(10);
      assertThat(config.getDefaultFetchSize()).isEqualTo(100);
      assertThat(config.getDefaultResultSetType()).isEqualTo(ResultSetType.SCROLL_INSENSITIVE);
      assertThat(config.isMapUnderscoreToCamelCase()).isTrue();
      assertThat(config.isSafeRowBoundsEnabled()).isTrue();
      assertThat(config.getLocalCacheScope()).isEqualTo(LocalCacheScope.STATEMENT);
      assertThat(config.getJdbcTypeForNull()).isEqualTo(JdbcType.NULL);
      assertThat(config.getLazyLoadTriggerMethods()).isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString", "xxx")));
      assertThat(config.isSafeResultHandlerEnabled()).isFalse();
      assertThat(config.getDefaultScriptingLanuageInstance()).isInstanceOf(RawLanguageDriver.class);
      assertThat(config.isCallSettersOnNulls()).isTrue();
      assertThat(config.getLogPrefix()).isEqualTo("mybatis_");
      assertThat(config.getLogImpl().getName()).isEqualTo(Slf4jImpl.class.getName());
      assertThat(config.getVfsImpl().getName()).isEqualTo(JBoss6VFS.class.getName());
      assertThat(config.getConfigurationFactory().getName()).isEqualTo(String.class.getName());
      assertThat(config.isShrinkWhitespacesInSql()).isTrue();
      assertThat(config.getDefaultSqlProviderType().getName()).isEqualTo(MySqlProvider.class.getName());
      assertThat(config.isNullableOnForEach()).isTrue();

      assertThat(config.getTypeAliasRegistry().getTypeAliases().get("blogauthor")).isEqualTo(Author.class);
      assertThat(config.getTypeAliasRegistry().getTypeAliases().get("blog")).isEqualTo(Blog.class);
      assertThat(config.getTypeAliasRegistry().getTypeAliases().get("cart")).isEqualTo(Cart.class);

      assertThat(config.getTypeHandlerRegistry().getTypeHandler(Integer.class)).isInstanceOf(CustomIntegerTypeHandler.class);
      assertThat(config.getTypeHandlerRegistry().getTypeHandler(Long.class)).isInstanceOf(CustomLongTypeHandler.class);
      assertThat(config.getTypeHandlerRegistry().getTypeHandler(String.class)).isInstanceOf(CustomStringTypeHandler.class);
      assertThat(config.getTypeHandlerRegistry().getTypeHandler(String.class, JdbcType.VARCHAR)).isInstanceOf(CustomStringTypeHandler.class);
      assertThat(config.getTypeHandlerRegistry().getTypeHandler(RoundingMode.class)).isInstanceOf(EnumOrdinalTypeHandler.class);

      ExampleObjectFactory objectFactory = (ExampleObjectFactory) config.getObjectFactory();
      assertThat(objectFactory.getProperties().size()).isEqualTo(1);
      assertThat(objectFactory.getProperties().getProperty("objectFactoryProperty")).isEqualTo("100");

      assertThat(config.getObjectWrapperFactory()).isInstanceOf(CustomObjectWrapperFactory.class);

      assertThat(config.getReflectorFactory()).isInstanceOf(CustomReflectorFactory.class);

      ExamplePlugin plugin = (ExamplePlugin) config.getInterceptors().get(0);
      assertThat(plugin.getProperties().size()).isEqualTo(1);
      assertThat(plugin.getProperties().getProperty("pluginProperty")).isEqualTo("100");

      Environment environment = config.getEnvironment();
      assertThat(environment.getId()).isEqualTo("development");
      assertThat(environment.getDataSource()).isInstanceOf(UnpooledDataSource.class);
      assertThat(environment.getTransactionFactory()).isInstanceOf(JdbcTransactionFactory.class);

      assertThat(config.getDatabaseId()).isEqualTo("derby");

      assertThat(config.getMapperRegistry().getMappers().size()).isEqualTo(4);
      assertThat(config.getMapperRegistry().hasMapper(CachedAuthorMapper.class)).isTrue();
      assertThat(config.getMapperRegistry().hasMapper(CustomMapper.class)).isTrue();
      assertThat(config.getMapperRegistry().hasMapper(BlogMapper.class)).isTrue();
      assertThat(config.getMapperRegistry().hasMapper(NestedBlogMapper.class)).isTrue();
    }
  }

  @Test
  void shouldSuccessfullyLoadXMLConfigFileWithPropertiesUrl() throws Exception {
    String resource = "org/apache/ibatis/builder/PropertiesUrlMapperConfig.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
      Configuration config = builder.parse();
      assertThat(config.getVariables().get("driver").toString()).isEqualTo("org.apache.derby.jdbc.EmbeddedDriver");
      assertThat(config.getVariables().get("prop1").toString()).isEqualTo("bbbb");
    }
  }

  @Test
  void parseIsTwice() throws Exception {
    String resource = "org/apache/ibatis/builder/MinimalMapperConfig.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
      builder.parse();

      when(builder::parse);
      then(caughtException()).isInstanceOf(BuilderException.class)
              .hasMessage("Each XMLConfigBuilder can only be used once.");
    }
  }

  @Test
  void unknownSettings() {
    final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n"
            + "<configuration>\n"
            + "  <settings>\n"
            + "    <setting name=\"foo\" value=\"bar\"/>\n"
            + "  </settings>\n"
            + "</configuration>\n";

    XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(MAPPER_CONFIG));
    when(builder::parse);
    then(caughtException()).isInstanceOf(BuilderException.class)
      .hasMessageContaining("The setting foo is not known.  Make sure you spelled it correctly (case sensitive).");
  }

  @Test
  void unknownJavaTypeOnTypeHandler() {
    final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n"
            + "<configuration>\n"
            + "  <typeAliases>\n"
            + "    <typeAlias type=\"a.b.c.Foo\"/>\n"
            + "  </typeAliases>\n"
            + "</configuration>\n";

    XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(MAPPER_CONFIG));
    when(builder::parse);
    then(caughtException()).isInstanceOf(BuilderException.class)
      .hasMessageContaining("Error registering typeAlias for 'null'. Cause: ");
  }

  @Test
  void propertiesSpecifyResourceAndUrlAtSameTime() {
    final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n"
            + "<configuration>\n"
            + "  <properties resource=\"a/b/c/foo.properties\" url=\"file:./a/b/c/jdbc.properties\"/>\n"
            + "</configuration>\n";

    XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(MAPPER_CONFIG));
    when(builder::parse);
    then(caughtException()).isInstanceOf(BuilderException.class)
      .hasMessageContaining("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
  }

  static class MySqlProvider {
    @SuppressWarnings("unused")
    public static String provideSql() {
      return "SELECT 1";
    }
  }

}
