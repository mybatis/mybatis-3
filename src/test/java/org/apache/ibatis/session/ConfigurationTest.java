/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.session;

import org.apache.ibatis.executor.loader.cglib.CglibProxyFactory;
import org.apache.ibatis.io.JBoss6VFS;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTest {

  @Test
  public void testFrom() {
    Settings settings = new Settings();

    settings.setSafeRowBoundsEnabled(true);
    settings.setSafeResultHandlerEnabled(false);
    settings.setMapUnderscoreToCamelCase(true);
    settings.setAggressiveLazyLoading(true);
    settings.setMultipleResultSetsEnabled(false);
    settings.setUseGeneratedKeys(true);
    settings.setUseColumnLabel(false);
    settings.setCacheEnabled(false);
    settings.setCallSettersOnNulls(true);
    settings.setUseActualParamName(false);
    settings.setReturnInstanceForEmptyRow(true);
    settings.setLogPrefix("a");
    settings.setLogImpl(StdOutImpl.class);
    settings.setVfsImpl(JBoss6VFS.class);
    settings.setLocalCacheScope(LocalCacheScope.STATEMENT);
    settings.setJdbcTypeForNull(JdbcType.NULL);
    settings.setLazyLoadTriggerMethods(
        new HashSet<String>(Arrays.asList("equals", "clone", "hashCode", "toString", "xxx")));
    settings.setDefaultStatementTimeout(100);
    settings.setDefaultFetchSize(101);
    settings.setDefaultExecutorType(ExecutorType.BATCH);
    settings.setAutoMappingBehavior(AutoMappingBehavior.FULL);
    settings.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.FAILING);
    settings.setLazyLoadingEnabled(true);
    settings.setProxyFactory(new CglibProxyFactory());
    settings.setDefaultEnumTypeHandler(EnumOrdinalTypeHandler.class);
    settings.setDefaultScriptingLanguage(RawLanguageDriver.class);
    settings.setConfigurationFactory(CustomConfigurationFactory.class);

    Configuration configuration = Configuration.from(settings);

    assertThat(configuration.isSafeRowBoundsEnabled()).isTrue();
    assertThat(configuration.isSafeResultHandlerEnabled()).isFalse();
    assertThat(configuration.isMapUnderscoreToCamelCase()).isTrue();
    assertThat(configuration.isAggressiveLazyLoading()).isTrue();
    assertThat(configuration.isMultipleResultSetsEnabled()).isFalse();
    assertThat(configuration.isUseGeneratedKeys()).isTrue();
    assertThat(configuration.isUseColumnLabel()).isFalse();
    assertThat(configuration.isCacheEnabled()).isFalse();
    assertThat(configuration.isCallSettersOnNulls()).isTrue();
    assertThat(configuration.isUseActualParamName()).isFalse();
    assertThat(configuration.isReturnInstanceForEmptyRow()).isTrue();
    assertThat(configuration.getLogPrefix()).isEqualTo("a");
    assertThat(configuration.getLogImpl()).isEqualTo(StdOutImpl.class);
    assertThat(configuration.getVfsImpl()).isEqualTo(JBoss6VFS.class);
    assertThat(configuration.getLocalCacheScope()).isEqualTo(LocalCacheScope.STATEMENT);
    assertThat(configuration.getJdbcTypeForNull()).isEqualTo(JdbcType.NULL);
    assertThat(configuration.getLazyLoadTriggerMethods()).isEqualTo(
        new HashSet<String>(Arrays.asList("equals", "clone", "hashCode", "toString", "xxx")));
    assertThat(configuration.getDefaultStatementTimeout()).isEqualTo(100);
    assertThat(configuration.getDefaultFetchSize()).isEqualTo(101);
    assertThat(configuration.getDefaultExecutorType()).isEqualTo(ExecutorType.BATCH);
    assertThat(configuration.getAutoMappingBehavior()).isEqualTo(AutoMappingBehavior.FULL);
    assertThat(configuration.getAutoMappingUnknownColumnBehavior())
        .isEqualTo(AutoMappingUnknownColumnBehavior.FAILING);
    assertThat(configuration.isLazyLoadingEnabled()).isTrue();
    assertThat(configuration.getProxyFactory()).isInstanceOf(CglibProxyFactory.class);
    assertThat(configuration.getDefaultEnumTypeHandler()).isEqualTo(EnumOrdinalTypeHandler.class);
    assertThat(configuration.getDefaultScriptingLanguage()).isEqualTo(RawLanguageDriver.class);
    assertThat(configuration.getConfigurationFactory()).isEqualTo(CustomConfigurationFactory.class);
  }

  static class CustomConfigurationFactory {
    public Configuration getConfiguration() {
      return new MyConfiguration();
    }
  }

  static class MyConfiguration extends Configuration {
  }

}
