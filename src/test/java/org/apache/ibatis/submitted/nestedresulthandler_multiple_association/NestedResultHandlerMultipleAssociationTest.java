/*
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
package org.apache.ibatis.submitted.nestedresulthandler_multiple_association;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NestedResultHandlerMultipleAssociationTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader(
        "org/apache/ibatis/submitted/nestedresulthandler_multiple_association/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/nestedresulthandler_multiple_association/CreateDB.sql");
  }

  @Test
  void failure() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      // Parents have child going from somewhere to somewhere, they are stored in
      // a Binome object
      // In this test we have 2 parents:
      // Parent1 is going from Child1 to Child2
      // Parent2 is going from Child2 to Child3 and from Child1 to Child2
      // You'll see a NULL entry in the list instead of the Binome Child1/Child2
      List<ParentBean> list = sqlSession.selectList("selectParentBeans");
      for (ParentBean pb : list) {
        for (Binome<ChildBean, ChildBean> childs : pb.getChilds()) {
          Assertions.assertNotNull(childs);
          Assertions.assertNotNull(childs.getOne());
          Assertions.assertNotNull(childs.getTwo());
        }
      }
    }
  }

  @Test
  void success() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      ParentBean parent = sqlSession.selectOne("selectParentBeanById", 2);

      // If you only select the Parent2 it works
      for (Binome<ChildBean, ChildBean> childs : parent.getChilds()) {
        Assertions.assertNotNull(childs);
        Assertions.assertNotNull(childs.getOne());
        Assertions.assertNotNull(childs.getTwo());
      }
    }
  }

}
