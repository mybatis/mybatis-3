/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.subclass_type_handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Date;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

/**
 * A user type that simply extends {@code java.util.Date} inherits the handler map that is registered for
 * {@code java.util.Date}. Registering a handler for the subtype must not replace the mapping of {@code java.util.Date}
 * (or of any other subtype that already inherited it).
 */
class SubclassTypeHandlerTest {

  @Test
  void subclassTypeHandlerMustNotReplaceSuperclassMapping() throws Exception {
    Configuration configuration = new Configuration();
    configuration.setEnvironment(new Environment("subclass_type_handler", new JdbcTransactionFactory(),
        new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:subclass_type_handler", null)));

    // Parsing this mapper resolves a type handler for MyDate and JdbcType.DATE, i.e. it looks up the
    // handler map that is registered for the superclass java.util.Date.
    configuration.addMapper(MyDateMapper.class);

    MyDateTypeHandler myDateTypeHandler = new MyDateTypeHandler();
    configuration.getTypeHandlerRegistry().register(MyDate.class, JdbcType.DATE, myDateTypeHandler);

    configuration.addMapper(RecordMapper.class);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try (Statement statement = sqlSession.getConnection().createStatement()) {
        statement.execute("drop table if exists subclass_type_handler");
        statement.execute("create table subclass_type_handler (id int not null primary key, d date)");
      }
      RecordMapper mapper = sqlSession.getMapper(RecordMapper.class);
      Record record = new Record();
      record.setId(1);
      // created from a local date, so the DATE round trip below is exact
      record.setD(java.sql.Date.valueOf(LocalDate.of(2020, 2, 3)));
      // java.util.Date / JdbcType.DATE must still be bound with the built-in handler (used to bind the
      // subtype's handler instead, throwing ClassCastException).
      assertEquals(1, mapper.insert(record));
      sqlSession.commit();

      Record loaded = mapper.selectById(1);
      assertEquals(record.getD().getTime(), loaded.getD().getTime());
      assertSame(Date.class, loaded.getD().getClass());
    }
  }

  /** A user type that only extends {@code java.util.Date}. */
  static class MyDate extends Date {
    private static final long serialVersionUID = 1L;

    MyDate(long time) {
      super(time);
    }
  }

  static class Record {
    private int id;
    private Date d;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public Date getD() {
      return d;
    }

    public void setD(Date d) {
      this.d = d;
    }
  }

  static class MyDateRecord {
    private int id;
    private MyDate d;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public MyDate getD() {
      return d;
    }

    public void setD(MyDate d) {
      this.d = d;
    }
  }

  /** A handler that only knows how to bind and unbind {@link MyDate} instances. */
  static class MyDateTypeHandler extends BaseTypeHandler<MyDate> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, MyDate parameter, JdbcType jdbcType)
        throws SQLException {
      ps.setDate(i, new java.sql.Date(parameter.getTime()));
    }

    @Override
    public MyDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
      java.sql.Date date = rs.getDate(columnName);
      return date == null ? null : new MyDate(date.getTime());
    }

    @Override
    public MyDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      java.sql.Date date = rs.getDate(columnIndex);
      return date == null ? null : new MyDate(date.getTime());
    }

    @Override
    public MyDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      java.sql.Date date = cs.getDate(columnIndex);
      return date == null ? null : new MyDate(date.getTime());
    }
  }

  /**
   * Declares the JDBC type of a {@link MyDate} property, which forces the type handler registry to look up a handler
   * for the subtype (and therefore to inherit the handler map of {@code java.util.Date}).
   */
  interface MyDateMapper {
    // @formatter:off
    @Select("select id, d from subclass_type_handler where id = #{id}")
    @Results({
      @Result(column = "id", property = "id"),
      @Result(column = "d", property = "d", javaType = MyDate.class, jdbcType = JdbcType.DATE)
    })
    MyDateRecord selectById(int id);
    // @formatter:on
  }

  /** The mapper using the existing, untouched {@code java.util.Date} handling. */
  interface RecordMapper {
    // @formatter:off
    @Insert("insert into subclass_type_handler (id, d) values (#{id}, #{d,jdbcType=DATE})")
    int insert(Record record);

    @Select("select id, d from subclass_type_handler where id = #{id}")
    Record selectById(int id);
    // @formatter:on
  }
}
