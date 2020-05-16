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
package org.apache.ibatis.submitted.named_constructor_args;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InvalidNamedConstructorArgsTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/named_constructor_args/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/named_constructor_args/CreateDB.sql");
  }

  interface NoMatchingConstructorMapper {
    @ConstructorArgs({
        @Arg(column = "id", name = "noSuchConstructorArg"),
    })
    @Select("select * from users ")
    User select();
  }

  @Test
  void noMatchingConstructorArgName() {
    Configuration configuration = sqlSessionFactory.getConfiguration();
    when(() -> configuration.addMapper(NoMatchingConstructorMapper.class));

    then(caughtException()).isInstanceOf(BuilderException.class)
      .hasMessageContaining(
          "'org.apache.ibatis.submitted.named_constructor_args.InvalidNamedConstructorArgsTest$NoMatchingConstructorMapper.select-void'")
      .hasMessageContaining("'org.apache.ibatis.submitted.named_constructor_args.User'")
      .hasMessageContaining("[noSuchConstructorArg]");
  }

  interface ConstructorWithWrongJavaType {
    // There is a constructor with arg name 'id', but
    // its type is different from the specified javaType.
    @ConstructorArgs({
        @Arg(column = "id", name = "id", javaType = Integer.class),
    })
    @Select("select * from users ")
    User select();
  }

  @Test
  void wrongJavaType() {
    Configuration configuration = sqlSessionFactory.getConfiguration();
    when(() -> configuration.addMapper(ConstructorWithWrongJavaType.class));
    then(caughtException()).isInstanceOf(BuilderException.class)
      .hasMessageContaining(
          "'org.apache.ibatis.submitted.named_constructor_args.InvalidNamedConstructorArgsTest$ConstructorWithWrongJavaType.select-void'")
      .hasMessageContaining("'org.apache.ibatis.submitted.named_constructor_args.User'")
      .hasMessageContaining("[id]");
  }

  interface ConstructorMissingRequiresJavaType {
    // There is a constructor with arg name 'id', but its type
    // is different from the type of a property with the same name.
    // javaType is required in this case.
    // Debug log shows the detail of the matching error.
    @ConstructorArgs({
        @Arg(column = "id", name = "id"),
    })
    @Select("select * from users ")
    User select();
  }

  @Test
  void missingRequiredJavaType() {
    Configuration configuration = sqlSessionFactory.getConfiguration();
    when(() -> configuration.addMapper(ConstructorMissingRequiresJavaType.class));
    then(caughtException()).isInstanceOf(BuilderException.class)
      .hasMessageContaining(
            "'org.apache.ibatis.submitted.named_constructor_args.InvalidNamedConstructorArgsTest$ConstructorMissingRequiresJavaType.select-void'")
      .hasMessageContaining("'org.apache.ibatis.submitted.named_constructor_args.User'")
      .hasMessageContaining("[id]");
  }
}
