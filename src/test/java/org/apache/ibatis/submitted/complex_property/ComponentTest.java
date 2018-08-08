/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.submitted.complex_property;

import static org.junit.Assert.assertNotNull;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.util.Calendar;

public class ComponentTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    String resource = "org/apache/ibatis/submitted/complex_property/Configuration.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/complex_property/db.sql");
  }


  @Test
  public void shouldInsertNestedPasswordFieldOfComplexType() throws Exception {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      //Create User
      User user = new User();
      user.setId(500000L);
      user.setPassword(new EncryptedString("secret"));
      user.setUsername("johnny" + Calendar.getInstance().getTimeInMillis());//random
      user.setAdministrator(true);

      sqlSession.insert("User.insert", user);

      //Retrieve User
      user = (User) sqlSession.selectOne("User.find", user.getId());

      assertNotNull(user.getId());

      sqlSession.rollback();
    }
  }

}
