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
package org.apache.ibatis.scripting;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author Kazuki Shimizu
 */
public class LanguageDriverRegistryTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private LanguageDriverRegistry registry = new LanguageDriverRegistry();

  @Test
  public void registerByType() {
    registry.register(RawLanguageDriver.class);
    LanguageDriver driver = registry.getDriver(RawLanguageDriver.class);

    assertThat(driver, instanceOf(RawLanguageDriver.class));
  }

  @Test
  public void registerByTypeSameType() {
    registry.register(RawLanguageDriver.class);
    LanguageDriver driver = registry.getDriver(RawLanguageDriver.class);

    registry.register(RawLanguageDriver.class);

    assertThat(driver, sameInstance(registry.getDriver(RawLanguageDriver.class)));
  }

  @Test
  public void registerByTypeNull() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("null is not a valid Language Driver");
    registry.register((Class<?>) null);
  }

  @Test
  public void registerByTypeDoesNotCreateNewInstance() {
    expectedException.expect(ScriptingException.class);
    expectedException.expectMessage("Failed to load language driver for org.apache.ibatis.scripting.LanguageDriverRegistryTest$PrivateLanguageDriver");
    registry.register(PrivateLanguageDriver.class);
  }

  @Test
  public void registerByInstance() {
    registry.register(new PrivateLanguageDriver());
    LanguageDriver driver = registry.getDriver(PrivateLanguageDriver.class);

    assertThat(driver, instanceOf(PrivateLanguageDriver.class));
  }

  @Test
  public void registerByInstanceSameType() {
    registry.register(new PrivateLanguageDriver());
    LanguageDriver driver = registry.getDriver(PrivateLanguageDriver.class);

    registry.register(new PrivateLanguageDriver());

    assertThat(driver, sameInstance(registry.getDriver(PrivateLanguageDriver.class)));
  }

  @Test
  public void registerByInstanceNull() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("null is not a valid Language Driver");
    registry.register((LanguageDriver) null);
  }

  @Test
  public void setDefaultDriverClass() {
    registry.setDefaultDriverClass(RawLanguageDriver.class);
    assertThat(registry.getDefaultDriverClass() == RawLanguageDriver.class, is(true));
    assertThat(registry.getDefaultDriver(), instanceOf(RawLanguageDriver.class));
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
