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
package org.apache.ibatis.submitted.record_type;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RecordTypeTest {

    static record Property(int id, String value) {
    }

    static record Item(int id, List<Property> properties) {
    }

    static record ItemId(Long get) implements Supplier<Long> {
    }
    public static class ItemIdTypeHandler extends IdTypeHandler<Long, ItemId> {
        public ItemIdTypeHandler() {
            super(Long.class, ItemId::new);
        }
    }

    static record PropId(Integer get) implements Supplier<Integer> {
    }
    public static class PropIdTypeHandler extends IdTypeHandler<Integer, PropId> {
        public PropIdTypeHandler() {
            super(Integer.class, PropId::new);
        }
    }

    static record Property2(PropId id, String value) {
    }

    static record Item2(ItemId id, List<Property2> properties) {
        public String idStr() {
            return String.valueOf(id);
        }
    }

    static interface Mapper {

        @Update("""
        <script>
            insert into properties (item_id, property_id, value) values
            <foreach collection="properties" item="prop" close=";" separator=",">
                (#{id},
                 #{prop.id},
                 #{prop.value})
            </foreach>
        </script>
        """)
        void updateProps(Item item);

        @Update("""
        <script>
            insert into properties (item_id, property_id, value) values
            <foreach collection="properties" item="prop" close=";" separator=",">
                (#{id, jdbcType=NUMERIC, typeHandler=org.apache.ibatis.submitted.record_type.RecordTypeTest$ItemIdTypeHandler},
                 #{prop.id, jdbcType=NUMERIC, typeHandler=org.apache.ibatis.submitted.record_type.RecordTypeTest$PropIdTypeHandler},
                 #{prop.value})
            </foreach>
        </script>
        """)
        void updateProps2(Item2 item);

        @Select("select * from properties")
        @ResultType(HashMap.class)
        List<Map<String, Object>> selectAsMap();

        @Select("select property_id, value from properties")
        @ConstructorArgs(value = {
            @Arg(column = "property_id",  javaType = int.class),
            @Arg(column = "value",        javaType = String.class),
        })
        List<Property> allProps();

        @Select("select property_id, value from properties")
        @ConstructorArgs(value = {
            @Arg(column="property_id",  javaType=PropId.class, typeHandler=PropIdTypeHandler.class),
            @Arg(column="value",        javaType=String.class),
        })
        List<Property2> allProps2();
    }

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/record_type/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/record_type/CreateDB.sql");
  }

  @Test
  void test1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      mapper.updateProps(new Item(10, List.of(
              new Property(11, "value11"),
              new Property(12, "value12"))));

      mapper.updateProps2(new Item2(new ItemId(20L), List.of(
              new Property2(new PropId(21), "value21"),
              new Property2(new PropId(22), "value22"))));

      mapper.selectAsMap().forEach(row -> {
          System.out.println(row);
      });
      System.out.println();

      mapper.allProps().forEach(prop -> {
          System.out.println(prop);
      });

      mapper.allProps2().forEach(prop -> {
          System.out.println(prop);
      });
    }
    catch (Exception e) {
        e.printStackTrace();
    }
  }

}
