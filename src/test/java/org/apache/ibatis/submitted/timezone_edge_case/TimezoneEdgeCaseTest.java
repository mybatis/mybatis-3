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
package org.apache.ibatis.submitted.timezone_edge_case;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

public class TimezoneEdgeCaseTest {

  private static SqlSessionFactory sqlSessionFactory;
  private TimeZone timeZone;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/timezone_edge_case/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/timezone_edge_case/CreateDB.sql");
  }

  @BeforeEach
  void saveTimeZone() {
    timeZone = TimeZone.getDefault();
  }

  @AfterEach
  void restoreTimeZone() {
    TimeZone.setDefault(timeZone);
  }

  @Test
  void shouldSelectNonExistentLocalTimestampAsIs() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = mapper.selectById(1);
      assertEquals(LocalDateTime.of(LocalDate.of(2019, 3, 10), LocalTime.of(2, 30)), record.getTs());
    }
  }

  @Test
  void shouldInsertNonExistentLocalTimestampAsIs() throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2019, 3, 10), LocalTime.of(2, 30));
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = new Record();
      record.setId(2);
      record.setTs(localDateTime);
      mapper.insert(record);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession();
        Connection con = sqlSession.getConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) from records where id = 2 and ts = '2019-03-10 02:30:00'")) {
      rs.next();
      assertEquals(1, rs.getInt(1));
    }
  }

  @Test
  void shouldSelectNonExistentLocalDateAsIs() {
    TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = mapper.selectById(1);
      assertEquals(LocalDate.of(2011, 12, 30), record.getD());
    }
  }

  @Test
  void shouldInsertNonExistentLocalDateAsIs() throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));
    LocalDate localDate = LocalDate.of(2011, 12, 30);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Record record = new Record();
      record.setId(3);
      record.setD(localDate);
      mapper.insert(record);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession();
        Connection con = sqlSession.getConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) from records where id = 3 and d = '2011-12-30'")) {
      rs.next();
      assertEquals(1, rs.getInt(1));
    }
  }
}
