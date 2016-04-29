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
package org.apache.ibatis.type.usesjava8;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.type.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for auto-detect type handlers of mybatis-typehandlers-jsr310.
 *
 * @author Kazuki Shimizu
 */
public class Jsr310TypeHandlerRegistryTest {

  private TypeHandlerRegistry typeHandlerRegistry;

  @Before
  public void setup() {
    typeHandlerRegistry = new TypeHandlerRegistry();
  }

  @Test
  public void testFor_v1_0_0() throws ClassNotFoundException {
    assertThat(getTypeHandler("java.time.Instant")).isInstanceOf(InstantTypeHandler.class);
    assertThat(getTypeHandler("java.time.LocalDateTime")).isInstanceOf(LocalDateTimeTypeHandler.class);
    assertThat(getTypeHandler("java.time.LocalDate")).isInstanceOf(LocalDateTypeHandler.class);
    assertThat(getTypeHandler("java.time.LocalTime")).isInstanceOf(LocalTimeTypeHandler.class);
    assertThat(getTypeHandler("java.time.OffsetDateTime")).isInstanceOf(OffsetDateTimeTypeHandler.class);
    assertThat(getTypeHandler("java.time.OffsetTime")).isInstanceOf(OffsetTimeTypeHandler.class);
    assertThat(getTypeHandler("java.time.ZonedDateTime")).isInstanceOf(ZonedDateTimeTypeHandler.class);
  }

  @Test
  public void testFor_v1_0_1() throws ClassNotFoundException {
    assertThat(getTypeHandler("java.time.Month")).isInstanceOf(MonthTypeHandler.class);
    assertThat(getTypeHandler("java.time.Year")).isInstanceOf(YearTypeHandler.class);
  }

  @Test
  public void testFor_v1_0_2() throws ClassNotFoundException {
    assertThat(getTypeHandler("java.time.YearMonth")).isInstanceOf(YearMonthTypeHandler.class);
    assertThat(getTypeHandler("java.time.chrono.JapaneseDate")).isInstanceOf(JapaneseDateTypeHandler.class);
  }

  private TypeHandler<?> getTypeHandler(String fqcn) throws ClassNotFoundException {
    return typeHandlerRegistry.getTypeHandler(Resources.classForName(fqcn));
  }

}
