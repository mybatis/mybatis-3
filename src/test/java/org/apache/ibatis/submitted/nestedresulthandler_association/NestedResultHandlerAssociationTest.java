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
package org.apache.ibatis.submitted.nestedresulthandler_association;

import static org.junit.Assert.*;

import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class NestedResultHandlerAssociationTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nestedresulthandler_association/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/nestedresulthandler_association/CreateDB.sql");
  }

  @Test
  public void shouldHandleRowBounds() throws Exception {
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    Date targetMonth = fmt.parse("2014-01-01");
    final List<Account> accounts = new ArrayList<Account>();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      sqlSession.select("collectPageByBirthMonth", targetMonth, new RowBounds(1, 2), new ResultHandler() {
        @Override
        public void handleResult(ResultContext context) {
          Account account = (Account) context.getResultObject();
          accounts.add(account);
        }
      });
    }
    assertEquals(2, accounts.size());
    assertEquals("Bob2", accounts.get(0).getAccountName());
    assertEquals("Bob3", accounts.get(1).getAccountName());
  }

  @Test
  public void shouldHandleStop() throws Exception {
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    final List<Account> accounts = new ArrayList<Account>();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Date targetMonth = fmt.parse("2014-01-01");
      sqlSession.select("collectPageByBirthMonth", targetMonth, new ResultHandler() {
        @Override
        public void handleResult(ResultContext context) {
          Account account = (Account) context.getResultObject();
          accounts.add(account);
          if (accounts.size() > 1)
            context.stop();
        }
      });
    }
    assertEquals(2, accounts.size());
    assertEquals("Bob1", accounts.get(0).getAccountName());
    assertEquals("Bob2", accounts.get(1).getAccountName());
  }

}
