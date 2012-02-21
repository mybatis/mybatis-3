/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.submitted.clearcache;

import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersonDAOTest {

  private static SqlSessionFactory sqlSessionFactory;
  private PersonDAO personDAO;
  private static Person person;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/clearcache/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/clearcache/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void find() {

    SqlSession session = sqlSessionFactory.openSession();
    try {
      personDAO = new PersonDAOImpl(session);

      person = new Person();
      person.setId(1);
      person.setFirstname("John");
      person.setLastname("Smith");

      Address home = new Address();
      home.setId(1);
      home.setStreet("1 somewhere lane");
      home.setCity("cary");
      home.setState("nc");
      home.setZip("27613");

      Address work = new Address();
      home.setId(2);
      work.setStreet("100 nowhere lane");
      work.setCity("raleigh");
      work.setState("nc");
      work.setZip("27614");

      List<Address> addresses = new ArrayList<Address>();
      addresses.add(home);
      addresses.add(work);
      person.setAddresses(addresses);
      personDAO.create(person);

      Person p = personDAO.findById(person.getId());
      Assert.assertEquals("John", p.getFirstname());
      Assert.assertEquals("Smith", p.getLastname());
      Assert.assertEquals(2, p.getAddresses().size());
      session.commit();
    } finally {
      if (session != null) {
        session.close();
      }
    }
  }

  @Test
  public void deleteAddress() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      personDAO = new PersonDAOImpl(session);
      Person p = personDAO.findById(person.getId());
      Assert.assertEquals(2, p.getAddresses().size());
      Address address = p.getAddresses().get(0);
      personDAO.deleteAddress(address.getId());
      p = personDAO.findById(person.getId());
      Assert.assertEquals(1, p.getAddresses().size());
      Assert.assertTrue(p.getAddresses().get(0).getId() != address.getId());
      session.commit();
    } finally {
      if (session != null) {
        session.close();
      }
    }
  }
}
