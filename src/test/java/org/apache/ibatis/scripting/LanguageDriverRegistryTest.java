/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.scripting;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/**
 * @author Kazuki Shimizu
 */
class LanguageDriverRegistryTest {

  private LanguageDriverRegistry registry = new LanguageDriverRegistry();

  @Test
  void registerByType() {
    registry.register(RawLanguageDriver.class);
    LanguageDriver driver = registry.getDriver(RawLanguageDriver.class);

    assertThat(driver).isInstanceOf(RawLanguageDriver.class);
  }

  @Test
  void registerByTypeSameType() {
    registry.register(RawLanguageDriver.class);
    LanguageDriver driver = registry.getDriver(RawLanguageDriver.class);

    registry.register(RawLanguageDriver.class);

    assertThat(driver).isSameAs(registry.getDriver(RawLanguageDriver.class));
  }

  @Test
  void registerByTypeNull() {
    when(() -> registry.register((Class<? extends LanguageDriver>) null));
    then(caughtException()).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("null is not a valid Language Driver");
  }

  @Test
  void registerByTypeDoesNotCreateNewInstance() {
    when(() -> registry.register(PrivateLanguageDriver.class));
    then(caughtException()).isInstanceOf(ScriptingException.class)
      .hasMessage("Failed to load language driver for org.apache.ibatis.scripting.LanguageDriverRegistryTest$PrivateLanguageDriver");
  }

  @Test
  void registerByInstance() {
    registry.register(new PrivateLanguageDriver());
    LanguageDriver driver = registry.getDriver(PrivateLanguageDriver.class);

    assertThat(driver).isInstanceOf(PrivateLanguageDriver.class);
  }

  @Test
  void registerByInstanceSameType() {
    registry.register(new PrivateLanguageDriver());
    LanguageDriver driver = registry.getDriver(PrivateLanguageDriver.class);

    registry.register(new PrivateLanguageDriver());

    assertThat(driver).isSameAs(registry.getDriver(PrivateLanguageDriver.class));
  }

  @Test
  void registerByInstanceNull() {
    when(() -> registry.register((LanguageDriver) null));
    then(caughtException()).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("null is not a valid Language Driver");
  }

  @Test
  void setDefaultDriverClass() {
    registry.setDefaultDriverClass(RawLanguageDriver.class);
    assertThat(registry.getDefaultDriverClass() == RawLanguageDriver.class).isTrue();
    assertThat(registry.getDefaultDriver()).isInstanceOf(RawLanguageDriver.class);
  }

  static private class PrivateLanguageDriver implements LanguageDriver {

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
      return null;
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
      return null;
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
      return null;
    }
  }

}
