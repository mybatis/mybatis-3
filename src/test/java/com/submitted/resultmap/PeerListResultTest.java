/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.submitted.resultmap;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/*
 * @author Jeff Butler
 */
public class PeerListResultTest extends TestCase {
  private SqlMapClient sqlMapClient = null;

  protected void setUp() throws Exception {
    Connection conn = null;
    Statement st = null;
    boolean dbCreated = true;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:aname", "sa", "");
      st = conn.createStatement();
      st.execute("drop table Person if exists");
      st.execute("create table Person(personId int not null, name varchar(50), primary key (personId))");

      st.execute("drop table PhoneNumber if exists");
      st.execute("create table PhoneNumber(personId int not null, phoneId int not null, phoneNumber varchar(50), primary key (personId, phoneId))");

      st.execute("drop table Address if exists");
      st.execute("create table Address(personId int not null, addressId int not null, address varchar(50), primary key (personId, addressId))");

      // setup Fred
      st.execute("insert into Person (personId, name) values(1, 'Fred')");
      st.execute("insert into PhoneNumber (personId, phoneId, phoneNumber) values(1, 1, '111-2222')");
      st.execute("insert into PhoneNumber (personId, phoneId, phoneNumber) values(1, 2, '333-4444')");
      st.execute("insert into Address (personId, addressId, address) values(1, 1, 'Main Street')");

      // setup Wilma
      st.execute("insert into Person (personId, name) values(2, 'Wilma')");
      st.execute("insert into PhoneNumber (personId, phoneId, phoneNumber) values(2, 1, '555-6666')");
      st.execute("insert into Address (personId, addressId, address) values(2, 1, 'Elm Street')");
      st.execute("insert into Address (personId, addressId, address) values(2, 2, 'Maple Lane')");

    } catch (Exception e) {
      fail(e.getMessage());
      dbCreated = false;
    } finally {
      try {
        if (st != null) {
          st.close();
        }

        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        // ignore
        ;
      }
    }

    if (dbCreated) {
      String resource = "com/submitted/resultmap/TestSqlMapConfig.xml";
      try {
        Reader reader = Resources.getResourceAsReader(resource);
        sqlMapClient = SqlMapClientBuilder
            .buildSqlMapClient(reader);
      } catch (IOException e) {
        fail(e.getMessage());
        sqlMapClient = null;
      }
    }
  }

  public void test01() {

    try {
      List list = sqlMapClient.queryForList("TestSqlMap.test01", null);

      assertEquals(2, list.size());
      Person person = (Person) list.get(0);
      assertEquals("Fred", person.getName());
      assertEquals(1, person.getAddresses().size());
      assertEquals(2, person.getPhoneNumbers().size());

      person = (Person) list.get(1);
      assertEquals("Wilma", person.getName());
      assertEquals(2, person.getAddresses().size());
      assertEquals(1, person.getPhoneNumbers().size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
