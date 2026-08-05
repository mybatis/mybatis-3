/**
 *    Copyright 2009-2019 the original author or authors.
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

package org.apache.ibatis.type.legacy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LegacyJsr310TypeHandlersTest {
  private static SqlSessionFactory sqlSessionFactory;
  private TimeZone timeZoneBackup;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/type/legacy/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/type/legacy/CreateDB.sql");
  }

  @BeforeEach
  void saveTimeZone() {
    timeZoneBackup = TimeZone.getDefault();
  }

  @AfterEach
  void restoreTimeZone() {
    TimeZone.setDefault(timeZoneBackup);
  }

  @Test
  void testSelectNonExistentDateTime() throws Exception {
    TimeZone timeZoneLA = TimeZone.getTimeZone("America/Los_Angeles");
    TimeZone.setDefault(timeZoneLA);
    // In L.A., 2019-03-10 3:30 does not exist because of DST
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      LegacyJsr310Bean bean = mapper.select(1);
      assertEquals(LocalDateTime.of(2019, 3, 10, 3, 30, 0, 123456789), bean.getLocalDateTime(), "hour gets shifted");
      assertEquals(OffsetDateTime.of(2019, 3, 10, 3, 30, 0, 123456000, ZoneOffset.ofHours(-7)),
          bean.getOffsetDateTime(), "hour gets shifted, offset is lost");
      assertEquals(ZonedDateTime.of(2019, 3, 10, 3, 30, 0, 123456000, timeZoneLA.toZoneId()),
          bean.getZonedDateTime(), "hour gets shifted, the default timezone is assigned");
      // Retrieving time is not affected by DST, but...
      assertEquals(LocalTime.of(2, 30, 0, 0), bean.getLocalTime(), "nanosecs is lost");
      assertEquals(OffsetTime.of(2, 30, 0, 0, ZoneOffset.ofHours(-8)),
          bean.getOffsetTime(), "nanosecs is lost, offset is lost");
    }
  }

  @Test
  void testSelectNonExistentDate() throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      LegacyJsr310Bean bean = mapper.select(1);
      assertEquals(LocalDate.of(2011, 12, 31), bean.getLocalDate(), "the date gets shifted");
    }
  }
}
