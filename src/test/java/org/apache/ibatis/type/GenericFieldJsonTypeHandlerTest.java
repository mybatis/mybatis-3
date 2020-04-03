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
package org.apache.ibatis.type;

import javax.sql.DataSource;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GenericFieldJsonTypeHandlerTest{

  private static SqlSessionFactory sqlSessionFactory;


  interface GenericFieldJsonTestMapper {
    @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "genericObj", column = "json", typeHandler = org.apache.ibatis.type.GenericTypeJsonTypeHandler.class),
    })
    @Select("SELECT ID, JSON FROM test_generic WHERE ID = #{id}")
    GenericFieldType findOne(int id);

    @Insert("INSERT INTO test_generic (ID, JSON) VALUES(#{id}, #{genericObj, typeHandler=org.apache.ibatis.type.GenericTypeJsonTypeHandler})")
    void insert(GenericFieldType blobContent);
  }

  static class RawType{
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "RawType{" +
        "name='" + name + '\'' +
        '}';
    }
  }

  static class GenericType<T>{
    private T genericObj;

    public T getGenericObj() {
      return genericObj;
    }

    public void setGenericObj(T genericObj) {
      this.genericObj = genericObj;
    }

    @Override
    public String toString() {
      return "GenericType{" +
        "genericObj=" + genericObj +
        '}';
    }
  }

  static class GenericFieldType{
    private int id;
    private GenericType<RawType> genericObj;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public GenericType<RawType> getGenericObj() {
      return genericObj;
    }

    public void setGenericObj(
      GenericType<RawType> genericObj) {
      this.genericObj = genericObj;
    }

    @Override
    public String toString() {
      return "GenericFieldType{" +
        "id=" + id +
        ", genericObj=" + genericObj +
        '}';
    }
  }

  @BeforeAll
  static void setupSqlSessionFactory() throws Exception {
    DataSource dataSource = BaseDataTest.createUnpooledDataSource("org/apache/ibatis/type/jdbc.properties");
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMapper(GenericFieldJsonTestMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/type/GenericFieldJsonTypeHandlerTest.sql");
  }

  @Test
  public void testTypeHandlerSupportGenericField() throws Exception {
    setupSqlSessionFactory();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    GenericFieldJsonTestMapper mapper = sqlSession.getMapper(GenericFieldJsonTestMapper.class);
    int id = 1;
    GenericType<RawType> genericObj = new GenericType<>();
    RawType rawObj = new RawType();
    rawObj.setName("Test");
    genericObj.setGenericObj(rawObj);
    GenericFieldType genericFieldObj = new GenericFieldType();
    genericFieldObj.setId(id);
    genericFieldObj.setGenericObj(genericObj);
    mapper.insert(genericFieldObj);

    genericFieldObj = mapper.findOne(id);
    rawObj = genericFieldObj.getGenericObj().getGenericObj();
    System.out.println(rawObj);
  }

}
