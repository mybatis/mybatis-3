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

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;

import org.apache.ibatis.type.InstantTypeHandler;
import org.apache.ibatis.type.JapaneseDateTypeHandler;
import org.apache.ibatis.type.LocalDateTimeTypeHandler;
import org.apache.ibatis.type.LocalDateTypeHandler;
import org.apache.ibatis.type.LocalTimeTypeHandler;
import org.apache.ibatis.type.MonthTypeHandler;
import org.apache.ibatis.type.OffsetDateTimeTypeHandler;
import org.apache.ibatis.type.OffsetTimeTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.YearMonthTypeHandler;
import org.apache.ibatis.type.YearTypeHandler;
import org.apache.ibatis.type.ZonedDateTimeTypeHandler;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kazuki Shimizu
 */
public class Jsr310TypeHandlerRegistryTest {

  private TypeHandlerRegistry typeHandlerRegistry;

  @Before
  public void setup() {
    typeHandlerRegistry = new TypeHandlerRegistry();
  }

  @Test
  public void shouldRegisterJsr310TypeHandlers() throws ClassNotFoundException {
    assertThat(typeHandlerRegistry.getTypeHandler(Instant.class))
        .isInstanceOf(InstantTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(LocalDateTime.class))
        .isInstanceOf(LocalDateTimeTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(LocalDate.class))
        .isInstanceOf(LocalDateTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(LocalTime.class))
        .isInstanceOf(LocalTimeTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(OffsetDateTime.class))
        .isInstanceOf(OffsetDateTimeTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(OffsetTime.class))
        .isInstanceOf(OffsetTimeTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(ZonedDateTime.class))
        .isInstanceOf(ZonedDateTimeTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(Month.class))
        .isInstanceOf(MonthTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(Year.class))
        .isInstanceOf(YearTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(YearMonth.class))
        .isInstanceOf(YearMonthTypeHandler.class);
    assertThat(typeHandlerRegistry.getTypeHandler(JapaneseDate.class))
        .isInstanceOf(JapaneseDateTypeHandler.class);
  }
}
