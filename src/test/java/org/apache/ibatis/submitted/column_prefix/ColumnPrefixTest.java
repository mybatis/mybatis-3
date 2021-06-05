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
package org.apache.ibatis.submitted.column_prefix;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ColumnPrefixTest {

  protected SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader(getConfigPath())) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/column_prefix/CreateDB.sql");
  }

  @Test
  void testSelectPetAndRoom() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Pet> pets = getPetAndRoom(sqlSession);
      assertEquals(3, pets.size());
      assertEquals("Ume", pets.get(0).getRoom().getRoomName());
      assertNull(pets.get(1).getRoom());
      assertEquals("Sakura", pets.get(2).getRoom().getRoomName());
    }
  }

  @Test
  void testComplexPerson() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Person> list = getPersons(sqlSession);
      Person person1 = list.get(0);
      assertEquals(Integer.valueOf(1), person1.getId());
      assertEquals(Address.class, person1.getBillingAddress().getClass());
      assertEquals(Integer.valueOf(10), person1.getBillingAddress().getId());
      assertEquals("IL", person1.getBillingAddress().getState());
      assertEquals("Chicago", person1.getBillingAddress().getCity());
      assertEquals("Cardinal", person1.getBillingAddress().getStateBird());
      assertEquals("IL", person1.getBillingAddress().getZip().getState());
      assertEquals("Chicago", person1.getBillingAddress().getZip().getCity());
      assertEquals(81, person1.getBillingAddress().getZip().getZipCode());
      assertEquals("0123", person1.getBillingAddress().getPhone1().getPhone());
      assertEquals("4567", person1.getBillingAddress().getPhone2().getPhone());
      assertEquals(AddressWithCaution.class, person1.getShippingAddress().getClass());
      assertEquals("Has a big dog.", ((AddressWithCaution) person1.getShippingAddress()).getCaution());
      assertEquals(Integer.valueOf(11), person1.getShippingAddress().getId());
      assertEquals("CA", person1.getShippingAddress().getState());
      assertEquals("San Francisco", person1.getShippingAddress().getCity());
      assertEquals("California Valley Quail", person1.getShippingAddress().getStateBird());
      assertEquals("CA", person1.getShippingAddress().getZip().getState());
      assertEquals(82, person1.getShippingAddress().getZip().getZipCode());
      assertEquals("8888", person1.getShippingAddress().getPhone1().getPhone());
      assertNull(person1.getShippingAddress().getPhone2());
      assertEquals("Tsubaki", person1.getRoom().getRoomName());
      assertEquals(2, person1.getPets().size());
      assertEquals("Kotetsu", person1.getPets().get(0).getName());
      assertEquals("Ume", person1.getPets().get(0).getRoom().getRoomName());
      assertNull(person1.getPets().get(1).getRoom());
      assertEquals("Chien", person1.getPets().get(1).getName());
      Person person2 = list.get(1);
      assertEquals(Integer.valueOf(2), person2.getId());
      assertEquals(AddressWithCaution.class, person2.getBillingAddress().getClass());
      assertEquals(Integer.valueOf(12), person2.getBillingAddress().getId());
      assertEquals("No door bell.", ((AddressWithCaution) person2.getBillingAddress()).getCaution());
      assertEquals("Los Angeles", person2.getBillingAddress().getCity());
      assertEquals("California Valley Quail", person2.getBillingAddress().getStateBird());
      assertEquals("Los Angeles", person2.getBillingAddress().getZip().getCity());
      assertEquals(83, person2.getBillingAddress().getZip().getZipCode());
      assertNull(person2.getBillingAddress().getPhone1());
      assertNull(person2.getBillingAddress().getPhone2());
      assertNull(person2.getShippingAddress());
      assertEquals(0, person2.getPets().size());
      Person person3 = list.get(2);
      assertEquals(Integer.valueOf(3), person3.getId());
      assertNull(person3.getBillingAddress());
      assertEquals(Address.class, person3.getShippingAddress().getClass());
      assertEquals(Integer.valueOf(13), person3.getShippingAddress().getId());
      assertEquals("Dallas", person3.getShippingAddress().getCity());
      assertEquals("Mockingbird", person3.getShippingAddress().getStateBird());
      assertEquals("Dallas", person3.getShippingAddress().getZip().getCity());
      assertEquals("9999", person3.getShippingAddress().getPhone1().getPhone());
      assertEquals("4567", person3.getShippingAddress().getPhone2().getPhone());
      assertEquals(1, person3.getPets().size());
      assertEquals("Dodo", person3.getPets().get(0).getName());
      assertEquals("Sakura", person3.getPets().get(0).getRoom().getRoomName());
    }
  }

  protected List<Pet> getPetAndRoom(SqlSession sqlSession) {
    List<Pet> pets = sqlSession.selectList("org.apache.ibatis.submitted.column_prefix.Mapper.selectPets");
    return pets;
  }

  protected List<Person> getPersons(SqlSession sqlSession) {
    List<Person> list = sqlSession.selectList("org.apache.ibatis.submitted.column_prefix.Mapper.selectPersons");
    return list;
  }

  protected String getConfigPath() {
    return "org/apache/ibatis/submitted/column_prefix/Config.xml";
  }
}
