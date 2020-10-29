/**
 *    Copyright 2009-2020 the original author or authors.
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GenericFieldJsonTypeHandlerTest{

  private static SqlSessionFactory sqlSessionFactory;


  interface GenericFieldJsonTestMapper {
    @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "simpleFieldObj", column = "simple", typeHandler = org.apache.ibatis.type.GenericTypeJsonTypeHandler.class),
      @Result(property = "genericFieldObj", column = "json", typeHandler = org.apache.ibatis.type.GenericTypeJsonTypeHandler.class)
    })
    @Select("SELECT ID, SIMPLE, JSON FROM test_generic WHERE ID = #{id}")
    HadGenericFieldType findOne(int id);

    @Insert("INSERT INTO test_generic (ID, SIMPLE, JSON) VALUES(#{id}, #{simpleFieldObj, typeHandler=org.apache.ibatis.type.GenericTypeJsonTypeHandler}, #{genericFieldObj, typeHandler=org.apache.ibatis.type.GenericTypeJsonTypeHandler})")
    void insert(HadGenericFieldType blobContent);
  }

  static class GenerciArgumentType{
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "GenerciArgumentType{" +
        "name='" + name + '\'' +
        '}';
    }
  }

  static class GenericFieldType<T>{
    private T genericArgumentTypeObj;

    public T getGenericArgumentTypeObj() {
      return genericArgumentTypeObj;
    }

    public void setGenericArgumentTypeObj(T genericArgumentTypeObj) {
      this.genericArgumentTypeObj = genericArgumentTypeObj;
    }

    @Override
    public String toString() {
      return "GenericFieldType{" +
        "genericArgumentTypeObj=" + genericArgumentTypeObj +
        '}';
    }
  }

  static class HadGenericFieldType{
    private int id;
    private LocalDateTime simpleFieldObj;
    private GenericFieldType<GenerciArgumentType> genericFieldObj;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public GenericFieldType<GenerciArgumentType> getGenericFieldObj() {
      return genericFieldObj;
    }

    public void setGenericFieldObj(GenericFieldType<GenerciArgumentType> genericFieldObj) {
      this.genericFieldObj = genericFieldObj;
    }

    public LocalDateTime getSimpleFieldObj() {
      return simpleFieldObj;
    }

    public void setSimpleFieldObj(LocalDateTime simpleFieldObj) {
      this.simpleFieldObj = simpleFieldObj;
    }

    @Override
    public String toString() {
      return "HadGenericFieldType{" +
        "id=" + id +
        ", simpleFieldObj=" + simpleFieldObj +
        ", genericFieldObj=" + genericFieldObj +
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
    GenericFieldType<GenerciArgumentType> genericFieldObj = new GenericFieldType<>();
    GenerciArgumentType argumentTypeObj = new GenerciArgumentType();
    argumentTypeObj.setName("Test");
    genericFieldObj.setGenericArgumentTypeObj(argumentTypeObj);
    HadGenericFieldType hadGenericFieldObj = new HadGenericFieldType();
    hadGenericFieldObj.setId(id);
    hadGenericFieldObj.setSimpleFieldObj(LocalDateTime.now());
    hadGenericFieldObj.setGenericFieldObj(genericFieldObj);
    mapper.insert(hadGenericFieldObj);

    hadGenericFieldObj = mapper.findOne(id);
    Class<?> argumentTypeClazz = hadGenericFieldObj.getGenericFieldObj().getGenericArgumentTypeObj().getClass();
    Assert.assertEquals(GenerciArgumentType.class, argumentTypeClazz);
    Class<?> simpleFieldClazz = hadGenericFieldObj.getSimpleFieldObj().getClass();
    Assert.assertEquals(LocalDateTime.class, simpleFieldClazz);
  }
}
