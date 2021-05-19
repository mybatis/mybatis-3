/*
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
package org.apache.ibatis.submitted.timestamp_with_timezone;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TimestampWithTimezoneTypeHandlerTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/timestamp_with_timezone/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/timestamp_with_timezone/CreateDB.sql");
  }

  @Test
  void shouldSelectOffsetDateTime() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = mapper.selectById(1);
      assertEquals(OffsetDateTime.of(2018, 1, 2, 11, 22, 33, 123456000, ZoneOffset.ofHoursMinutes(1, 23)),
          record.getOdt());
      // HSQLDB 2.4.1 truncates nano seconds.
      assertEquals(OffsetTime.of(11, 22, 33, 0, ZoneOffset.ofHoursMinutes(1, 23)), record.getOt());
    }
  }

  @Test
  void shouldInsertOffsetDateTime() {
    OffsetDateTime odt = OffsetDateTime.of(2018, 1, 2, 11, 22, 33, 123456000, ZoneOffset.ofHoursMinutes(1, 23));
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = new Record();
      record.setId(2);
      record.setOdt(odt);
      int result = mapper.insertOffsetDateTime(record);
      assertEquals(1, result);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = mapper.selectById(2);
      assertEquals(odt, record.getOdt());
    }
  }

  @Disabled("HSQLDB 2.4.1 does not support this.")
  @Test
  void shouldInsertOffsetTime() {
    OffsetTime ot = OffsetTime.of(11, 22, 33, 123456000, ZoneOffset.ofHoursMinutes(1, 23));
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = new Record();
      record.setId(3);
      record.setOt(ot);
      int result = mapper.insertOffsetTime(record);
      assertEquals(1, result);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = mapper.selectById(3);
      assertEquals(ot, record.getOt());
    }
  }

}
